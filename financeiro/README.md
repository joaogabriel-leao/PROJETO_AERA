# AERA Financeiro — base inicial (MVP de desenvolvimento)

**Não utilizar com dados financeiros reais ainda.** Esta é a primeira implementação de login, contas, categorias e lançamentos. Não há rotina de backup/restore, auditoria imutável completa, gerenciamento de usuários, testes de integração, conciliação bancária nem instalação como serviço.

## Arquitetura definida

- Computador do administrativo financeiro executa **Spring Boot + PostgreSQL**.
- Enquanto o computador estiver ligado, a unidade principal pode usar a aplicação localmente, inclusive sem internet.
- Acesso remoto de outras unidades só funciona com computador e internet da unidade principal ativos.
- No futuro, acesso remoto por VPN privada (ex.: Tailscale), não expondo porta 8080 ou 5432 na internet.
- Google Drive será destino de backups **criptografados**, não banco de dados nem serviço de hospedagem.
- Se computador desligar, servidor e backups automáticos param; criar rotina de inicialização automática na máquina final.

## Requisitos de desenvolvimento

- Java 21, Maven 3.6.3+, PostgreSQL em execução.
- Spring Boot 4.1.2 (versão estável em 01/11/2026).

## Configuração local

1. Crie banco e usuário no PostgreSQL, por exemplo como usuário postgres:

```sql
CREATE USER aera WITH PASSWORD 'UMA_SENHA_FORTE_DIFERENTE';
CREATE DATABASE aera_financeiro OWNER aera;
```

2. Defina variáveis de ambiente no terminal/IDE. **Nunca coloque senhas reais no Git.** O `.env.example` é apenas um modelo, não é carregado automaticamente.

```bash
export DB_URL='jdbc:postgresql://localhost:5432/aera_financeiro'
export DB_USER='aera'
export DB_PASSWORD='UMA_SENHA_FORTE_DIFERENTE'
export ADMIN_INITIAL_USERNAME='admin'
export ADMIN_INITIAL_PASSWORD='UMA_SENHA_FORTE_DE_12_OU_MAIS_CARACTERES'
```

No Windows PowerShell, utilize `$env:DB_URL = '...'`, e assim por diante.

3. Execute `mvn spring-boot:run`. Abra `http://localhost:8080`.
4. No primeiro início, o sistema cria usuário ADMIN com a senha informada. Depois, os usuários permanecem gravados no banco. Não há senha padrão embutida no código.
5. Cadastre contas e categorias na seção Configurações, depois lance movimentações.

## Limitações conhecidas desta etapa

- Apenas a primeira conta ADMIN é criada; módulo de gestão de usuários e alteração de senhas ainda será implementado.
- Operações ficam disponíveis no banco, mas **não há exclusão/estorno nem edição ainda**, para evitar perda de rastreabilidade.
- Sem armazenamento de anexos ou controle de contas a pagar/receber.
- Sem saldo inicial, extratos bancários, dashboard, conciliação ou relatórios; estes dependem de regras financeiras confirmadas pela instituição.
- A interface não está pronta para produção (validação/fluxos de erro, auditoria, acessibilidade, testes e avaliação de segurança pendentes).
- O computador local deve ter backup separado e disco confiável; um backup apenas no mesmo computador não protege contra falha física.
- `SERVER_ADDRESS` default é `127.0.0.1` (acesso somente na própria máquina). Não alterar para `0.0.0.0` e liberar firewall antes de configurar VPN e demais proteções.

## Próximas entregas

1. Gestão de usuários por administrador, perfis e alteração de senha.
2. Serviço de lançamentos com trilha de auditoria e estorno, saldo e regras de negócio.
3. Rotina de proteção a cada movimentação: log/registro durável + backup PostgreSQL periódico e envio eventual criptografado ao Google Drive, com fila offline.
4. Procedimento testado de restauração em nova máquina (incluindo anexos e chaves de criptografia).
5. Relatórios, comprovantes e operação como serviço no sistema operacional escolhido.
6. Testes automatizados e homologação com dados fictícios antes do uso real.

## Nota sobre Google Drive

Para uma conta Google comum da AERA, a integração deve usar OAuth 2.0 autorizado pela instituição, com tokens protegidos. Não use uma conta de serviço isolada como proprietária de arquivos no Meu Drive pessoal. A conta e as credenciais pertencem à instituição.
