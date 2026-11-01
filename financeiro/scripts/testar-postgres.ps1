param(
    [string]$PostgresBin = 'C:\Program Files\PostgreSQL\17\bin',
    [string]$MavenRepository = (Join-Path $env:USERPROFILE '.m2\repository')
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
foreach ($binary in @('initdb.exe', 'pg_ctl.exe', 'createdb.exe')) {
    if (-not (Test-Path -LiteralPath (Join-Path $PostgresBin $binary))) {
        throw "Binário PostgreSQL não encontrado: $binary. Nenhuma ferramenta será instalada."
    }
}

# Pasta sempre nova: não reutiliza PGDATA, serviços ou bancos do usuário.
$testRoot = Join-Path $projectRoot ('target\postgres-fase2-' + [guid]::NewGuid().ToString('N'))
$dataPath = Join-Path $testRoot 'data'
$logPath = Join-Path $testRoot 'postgres.log'
New-Item -ItemType Directory -Path $testRoot | Out-Null
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
$listener.Start()
$port = $listener.LocalEndpoint.Port
$listener.Stop()
$started = $false
Push-Location $projectRoot
try {
    Write-Host "Cluster exclusivo de testes: $testRoot; porta: $port"
    # Trust restrito ao cluster efêmero local, contendo somente dados fictícios.
    & (Join-Path $PostgresBin 'initdb.exe') -D $dataPath -U aera_test -A trust --encoding=UTF8 --locale=C
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao inicializar cluster isolado.' }
    & (Join-Path $PostgresBin 'pg_ctl.exe') -D $dataPath -l $logPath -o "-h 127.0.0.1 -p $port" -w -t 30 start
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar cluster isolado. Consulte postgres.log.' }
    $started = $true
    & (Join-Path $PostgresBin 'createdb.exe') -h 127.0.0.1 -p $port -U aera_test aera_fase2_test
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar banco exclusivo de testes.' }
    & mvn -B -ntp "-Dmaven.repo.local=$MavenRepository" '-Daera.postgres.integration=true' "-Daera.postgres.port=$port" test
    if ($LASTEXITCODE -ne 0) { throw 'Os testes falharam. Consulte target/surefire-reports.' }
}
finally {
    if ($started) {
        & (Join-Path $PostgresBin 'pg_ctl.exe') -D $dataPath -w -t 30 -m fast stop
        if ($LASTEXITCODE -ne 0) { Write-Warning "Verifique o cluster de teste em $dataPath; a parada falhou." }
    }
    Pop-Location
    # Não apaga arquivos. Cluster e logs permanecem para inspeção em target/.
}
