# Fase 2 — módulo financeiro

Verificação em 01/11/2026. O pedido recebido listava os tópicos da fase, sem o prompt completo mencionado. Foi usado o financeiro existente como escopo, preservando Java 21, Spring Boot, MVC, JPA, Flyway e PostgreSQL. Não foram introduzidos módulos de saldo, estorno, edição, exclusão, auditoria imutável ou novas regras financeiras sem especificação.

## Implementação e organização

Fluxo de gravação: `MovimentacaoController → MovimentacaoService → repositórios JPA → PostgreSQL`.

- O controller mantém binding HTTP, apresentação dos erros e redirecionamento. A transação e a gravação saíram do controller.
- `MovimentacaoService.registrar` executa autorização, validação, verificação de referências ativas e gravação em uma transação Spring. O usuário é obtido da autenticação, nunca de um identificador enviado no formulário.
- `MovimentacaoService.listar` usa transação somente de leitura e a consulta existente com fetch dos relacionamentos, compatível com open-in-view desabilitado.
- `RegistrarMovimentacao` é um record de entrada independente da camada web. O serviço executa Jakarta Validation explicitamente, inclusive para chamadas sem controller.
- `MovimentacaoInvalidaException` informa o campo de conta/categoria inválida. O controller transforma esse erro em erro do formulário e mantém os dados preenchidos.
- Nenhuma dependência Maven ou migration foi alterada nesta fase.

## Regras aplicadas

- ENTRADA e SAIDA usam valores positivos. O tipo define a natureza do lançamento; não há inversão automática do sinal.
- Valor mínimo de 0,01; até 13 dígitos inteiros e 2 decimais, conforme NUMERIC(15,2) existente. Não há arredondamento silencioso.
- Conta, categoria, tipo, valor e data são obrigatórios; descrição não pode ser vazia e tem até 500 caracteres. A descrição é aparada antes de persistir.
- Conta e categoria precisam existir e estar ativas.
- Não foi presumida proibição de saldo negativo, data futura ou data retroativa: essas regras não foram fornecidas.

## Segurança

- `@EnableMethodSecurity` ativa `@PreAuthorize` no serviço, além das regras HTTP e CSRF já existentes.
- ADMIN, TESOUREIRO e OPERADOR podem registrar; CONSULTA também pode listar.
- O serviço reconsulta o usuário ativo e seu perfil persistido. Desativação ou mudança para CONSULTA impede novos lançamentos mesmo quando a sessão ainda contém o perfil antigo.
- Usuário sem acesso recebe 403 no fluxo HTTP. Credenciais inválidas continuam recusadas pelo login real do Spring Security com BCrypt.
- A revalidação do perfil descrita acima aplica-se às operações deste serviço; não representa uma implementação de revogação global de todas as sessões ou de todas as operações do sistema.

## Testes e resultados

| Suíte | Execuções com PostgreSQL habilitado | Falhas / erros / ignorados |
| --- | ---: | --- |
| MovimentacaoServiceTest | 24 | 0 / 0 / 0 |
| SegurancaMovimentacaoTest | 23 | 0 / 0 / 0 |
| PostgreSqlMovimentacaoTest | 6 | 0 / 0 / 0 |
| Total | **53** | **0 / 0 / 0** |

Execução completa: **BUILD SUCCESS**, Maven em 42,724 s. As suítes incluem os testes da Fase 1 adaptados ao serviço real, autorização por proxy sem controller, sessões com usuário desativado/perfil revogado, entradas e saídas, limites monetários, campos obrigatórios, descrição, referências inválidas e falha de persistência.

Integração real com PostgreSQL **17.5**:

- Aplicação Spring Boot completa em contexto web de teste, sem publicar servidor HTTP.
- Flyway aplicou a V1 original em banco vazio e validou a migration; Hibernate iniciou com `ddl-auto=validate`.
- ENTRADA e SAIDA foram gravadas, descarregadas do contexto JPA e consultadas novamente com seus relacionamentos, valor, descrição e data de criação.
- Conta e usuário desativados no banco impediram gravação.
- Uma inserção SQL deliberadamente inválida foi recusada pela constraint `movimentacoes_valor_check`. O ERROR correspondente no postgres.log é esperado nesse teste.
- Cada teste de integração usa transação com rollback. O bootstrap cria apenas o administrador fictício no cluster isolado.

Execução padrão posterior, com recompilação final dos 19 arquivos de produção para `release 21`: **BUILD SUCCESS**, 47 testes aprovados, 5 registros ignorados, 0 falhas, 0 erros; tempo Maven 26,485 s. A classe de integração fica desabilitada sem a opção explícita. O JUnit não expande as duas variantes do teste parametrizado desabilitado, por isso registra 5 ignorados em vez das 6 execuções da suíte habilitada. Esse comando não conectou a banco algum.

## Isolamento do PostgreSQL

`scripts/testar-postgres.ps1` usa os binários já instalados em `C:\Program Files\PostgreSQL\17\bin`. O script não instala ferramentas e não reutiliza serviços, PGDATA ou bancos existentes.

Nesta execução criou:

- Pasta: `target/postgres-fase2-20b4df6db8214038a0561c6ee1f100c4/`.
- Escuta: `127.0.0.1:56137`, porta selecionada entre as disponíveis.
- Banco: `aera_fase2_test`; usuário local de teste: `aera_test`.
- Autenticação trust somente no cluster de teste, acessível por loopback e contendo apenas dados fictícios.

O teste define explicitamente as URLs e credenciais de datasource e Flyway. Não usa DB_URL, DB_USER ou DB_PASSWORD do ambiente. O script para exclusivamente o cluster que criou no bloco finally e conserva os arquivos/logs em target para inspeção. A parada foi confirmada por `server stopped`, pelo log `database system is shut down` e pela ausência de `postmaster.pid`.

O teste de integração foi projetado para execução pelo script. Não habilite manualmente a opção de integração apontando para uma instância existente: use sempre o cluster novo criado pelo script.

## Arquivos novos ou modificados

Produção:

- Novo `src/main/java/br/org/aera/financeiro/service/MovimentacaoService.java`.
- Novo `src/main/java/br/org/aera/financeiro/service/RegistrarMovimentacao.java`.
- Novo `src/main/java/br/org/aera/financeiro/service/MovimentacaoInvalidaException.java`.
- Modificado `src/main/java/br/org/aera/financeiro/web/MovimentacaoController.java`.
- Modificado `src/main/java/br/org/aera/financeiro/security/SecurityConfig.java`.

Testes e documentação:

- Modificado `src/test/java/br/org/aera/financeiro/web/SegurancaMovimentacaoTest.java`.
- Novo `src/test/java/br/org/aera/financeiro/service/MovimentacaoServiceTest.java`.
- Novo `src/test/java/br/org/aera/financeiro/service/PostgreSqlMovimentacaoTest.java`.
- Novo `scripts/testar-postgres.ps1`.
- Novo `FASE2-VERIFICACAO.md`.

## Comandos executados e reprodução

```powershell
# Testes comuns, sem banco (executado antes e depois da ampliação da suíte)
mvn -B -ntp "-Dmaven.repo.local=C:/Users/Peres mamute/.m2/repository" test

# Diagnóstico Docker: sem daemon disponível; não utilizado nos testes
docker version --format '{{.Server.Version}}'

# Primeira tentativa bloqueada pela política de scripts do Windows
.\scripts\testar-postgres.ps1

# Execução aprovada, sem alterar a política persistente do sistema
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\testar-postgres.ps1
```

O script executou os seguintes comandos nativos, com caminhos completos e valores exclusivos de teste:

```powershell
initdb.exe -D <pasta-nova>/data -U aera_test -A trust --encoding=UTF8 --locale=C
pg_ctl.exe -D <pasta-nova>/data -l <pasta-nova>/postgres.log -o "-h 127.0.0.1 -p 56137" -w -t 30 start
createdb.exe -h 127.0.0.1 -p 56137 -U aera_test aera_fase2_test
mvn -B -ntp "-Dmaven.repo.local=C:/Users/Peres mamute/.m2/repository" -Daera.postgres.integration=true -Daera.postgres.port=56137 test
pg_ctl.exe -D <pasta-nova>/data -w -t 30 -m fast stop
```

As demais operações foram inspeções com `rg --files`, `Get-Content`, `Get-ChildItem`, `Get-Command` e `Test-Path` nos arquivos do projeto, binários instalados, cache Maven, relatórios e logs. Edições via apply_patch. Nenhum comando destrutivo foi executado.

## Limitações e observações

- Nenhuma falha de teste permaneceu. Houve obstáculos de ambiente: Docker indisponível e execução de scripts inicialmente bloqueada, resolvidos usando PostgreSQL já instalado e permissão de execução apenas no processo aprovado.
- Permanecem avisos não bloqueantes já observados na Fase 1 sobre o agente dinâmico do Mockito e operações genéricas nos testes.
- Os testes MVC usam resolver de views de teste: não verificam renderização visual ou HTML Thymeleaf. A integração verifica aplicação, serviço, persistência, migration e esquema, sem testar navegação em navegador.
- Não foram implementados controle de concorrência para alteração simultânea de cadastros, idempotência de envios, saldo, auditoria imutável ou regras de estorno.
- Os relatórios Surefire em `target/surefire-reports/` refletem a última execução padrão; os resultados da execução completa anterior estão registrados acima. Logs do PostgreSQL isolado foram preservados.
