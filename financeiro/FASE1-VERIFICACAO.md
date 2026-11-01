# Verificação da Fase 1 — 01/11/2026

## Resultado

- POM declara Java 21; Java e Maven executam com Oracle JDK 21.0.2.
- Maven 3.9.11; Spring Boot 4.1.2 mantido, sem mudança de arquitetura.
- Dependências necessárias a `test` resolvidas no cache do usuário.
- Compilação inicial dos 16 arquivos de produção com `release 21`: sucesso.
- Execução inicial: nenhum teste existente.
- Execução final: **20 testes, 0 falhas, 0 erros, 0 ignorados; BUILD SUCCESS**.

## Alterações

- `pom.xml`: adicionada somente a dependência de escopo test `spring-security-test`, com versão gerenciada pelo parent.
- `.gitignore`: adicionado `.m2/` para ignorar o cache local tentado durante o diagnóstico.
- `src/test/java/br/org/aera/financeiro/web/SegurancaMovimentacaoTest.java`: contexto MVC com configuração de segurança real, BCrypt e repositórios Mockito. Sem auto-configuração de banco, JPA, Flyway ou bootstrap do administrador; não carrega application.yml nem credenciais do ambiente.
- Este relatório documenta execução, limites e resultados.

Cobertura: login público, redirecionamento anônimo, senha correta/incorreta, usuário não retornado pelo filtro de ativos, configuração exclusiva de ADMIN, leitura por CONSULTA e bloqueio de escrita, escrita por ADMIN/TESOUREIRO/OPERADOR, CSRF, campos obrigatórios, valores inválidos e limites monetários, descrição longa, conta inexistente e categoria inativa. Casos inválidos verificam ausência de persistência. O caso válido verifica dados enviados ao repositório, incluindo descrição aparada e usuário autenticado.

## Falhas encontradas e tratamento

1. `mvn test` tentou usar `C:\.m2\repository`, sem permissão no ambiente restrito. Resolvido para esta execução com `-Dmaven.repo.local` explícito; nenhuma configuração global alterada.
2. A tentativa de cache no workspace não conseguiu acessar Maven Central (`Permission denied: getsockopt`). Execução com permissão aprovada e cache do usuário concluiu com sucesso.
3. Primeira execução da suíte nova: 18 passaram e 2 falharam por problemas do teste (padrão de URL incompatível com `/login` e colisão do nome do bean mock com `usuarios` de SecurityConfig). Corrigidas as expectativas e o nome do bean; execução final passou.
4. `git status --short` falhou por ownership do repositório localizado em `C:\Users\Peres mamute`. Nenhuma exceção global de Git foi criada.

Não foram confirmados erros de compilação ou configuração na aplicação que exigissem alterações de produção. Permanecem avisos não bloqueantes de operações genéricas nos testes e carregamento dinâmico do agente Mockito no Java 21.

## Comandos executados

Comandos de build e diagnóstico (PowerShell):

```powershell
java -version
mvn -version
git status --short
mvn -B -ntp test
mvn -B -ntp "-Dmaven.repo.local=.m2/repository" test
mvn -B -ntp "-Dmaven.repo.local=C:/Users/Peres mamute/.m2/repository" test
```

O último comando foi executado três vezes: baseline sem testes, primeira suíte com duas falhas, suíte corrigida com 20 aprovações. Ele executa resolução das dependências necessárias, compilação e testes; não inicia a aplicação.

Inspeções somente de leitura: `Get-Location`, `rg --files --hidden` (excluindo .git, target e node_modules), `Get-ChildItem` para AGENTS.md, versões do parent no cache Maven e relatórios Surefire, e `Get-Content` no POM, .gitignore, README, application.yml, migration V1 e classes de domínio, repositórios, segurança e controllers. Edições feitas via apply_patch.

## Limites

Nenhum banco real ou temporário foi acessado. Nenhuma ferramenta foi instalada, migration alterada, esquema modificado ou comando destrutivo executado. Os testes não validam PostgreSQL, migrations, inicialização completa da aplicação nem renderização Thymeleaf: as views são resolvidas por um resolver de teste. O teste de usuário inativo simula o retorno vazio de `findByUsernameAndAtivoTrue`, sem testar a consulta JPA.

Relatórios gerados em `target/surefire-reports/`.
