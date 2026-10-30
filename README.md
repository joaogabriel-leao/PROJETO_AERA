# Projeto AERA

Aplicação full stack organizada como **monorepo**: o frontend (React + Vite)
e o backend (Spring Boot) convivem no mesmo repositório, cada um em sua pasta.

---

## Arquitetura

```
PROJETO_AERA/
├── frontend/                 # Aplicação web (React + Vite + TypeScript)
│   ├── src/
│   │   ├── components/       # Componentes de UI reutilizáveis
│   │   ├── pages/            # Telas (Alunos, Financeiro, Patrimônio, etc.)
│   │   ├── services/         # Camada de comunicação com a API do backend
│   │   ├── hooks/            # Hooks customizados
│   │   ├── types/            # Tipos TypeScript compartilhados
│   │   └── utils/            # Funções utilitárias
│   ├── .env.example          # Modelo das variáveis de ambiente (copiar p/ .env)
│   └── package.json
│
├── backend/                  # API REST (Spring Boot + Java 21 + Maven)
│   ├── src/main/java/br/com/aera/backend/
│   │   └── BackendApplication.java   # Classe principal (ponto de entrada)
│   ├── src/main/resources/
│   │   ├── application.properties       # Config comum + profile padrão (dev)
│   │   ├── application-dev.properties    # Config de desenvolvimento
│   │   └── application-prod.properties   # Config de produção (travada)
│   ├── src/test/java/...              # Testes
│   └── pom.xml                        # Dependências e build (Maven)
│
└── README.md                 # Este arquivo
```

### Como as camadas se comunicam

```
[ Navegador ]
      │
      ▼
[ Frontend  ]  React + Vite   → roda em http://localhost:5173
      │
      │  chamadas HTTP (fetch) usando VITE_API_BASE_URL
      ▼
[ Backend   ]  Spring Boot    → roda em http://localhost:8080/api
      │
      ▼
[ Banco     ]  H2 em memória (desenvolvimento) — definitivo a definir pelo grupo
```

O frontend concentra todas as chamadas de rede em `frontend/src/services/`.
A URL base da API é lida da variável `VITE_API_BASE_URL` e aponta, por padrão,
para `http://localhost:8080/api` — exatamente onde o backend escuta.

---

## Pré-requisitos

| Ferramenta | Versão usada no projeto | Para quê |
|------------|-------------------------|----------|
| Node.js    | 18+ (recomendado 20+)   | Rodar o frontend |
| npm        | acompanha o Node        | Gerenciar pacotes do frontend |
| Java (JDK) | 21                      | Rodar o backend |
| Maven      | 3.9+                    | Build do backend |

---

## Como rodar

> Frontend e backend sobem separadamente, em terminais diferentes.

### 1. Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

A API ficará disponível em `http://localhost:8080/api`.

> Ao rodar assim, a aplicação sobe no **profile `dev`** (é o padrão). Veja a
> seção **"Ambientes (profiles dev/prod)"** abaixo para entender o que isso
> significa e como rodar em produção.

Outros comandos úteis:

```bash
mvn clean test       # compila e roda os testes (usa o profile dev)
mvn clean package    # gera o .jar em backend/target/
```

---

## Ambientes (profiles dev/prod) — backend

O backend usa **Spring Profiles**: é o **mesmo código** que se comporta de
forma diferente dependendo do ambiente. Não são dois projetos — é uma só
aplicação com dois "modos" de execução.

As configurações ficam divididas em três arquivos em
`backend/src/main/resources/`:

| Arquivo | Conteúdo |
|---------|----------|
| `application.properties` | Configuração **comum** aos dois ambientes (porta, context-path, CORS) e define o profile padrão (`dev`). |
| `application-dev.properties` | Específico de **desenvolvimento**. |
| `application-prod.properties` | Específico de **produção**. |

### O que muda entre os ambientes

| Configuração | `dev` (desenvolvimento) | `prod` (produção) |
|--------------|-------------------------|-------------------|
| Banco de dados | H2 em memória (fixo) | Lido de variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) |
| Console web do H2 (`/api/h2-console`) | **Ligado** (fácil inspecionar dados) | **Desligado** (segurança) |
| Logs de SQL (`show-sql`) | **Ligados** (ajudam a depurar) | **Desligados** (não vazar dados nos logs) |
| Detalhes de erro nas respostas | Padrão | **Ocultados** (não expor stack trace) |
| Origens de CORS | `localhost` | Definidas por `APP_CORS_ALLOWED_ORIGINS` |

### Como rodar em cada ambiente

```bash
# Desenvolvimento (padrão — é o que você usa no dia a dia):
cd backend
mvn spring-boot:run
#   equivalente a: mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Produção (quando/se o sistema for publicado):
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

> **No dia a dia, nada muda para quem desenvolve:** basta rodar
> `mvn spring-boot:run` e a aplicação sobe em `dev`. O profile `prod` fica
> **pronto e documentado** para o futuro, mas não é usado enquanto o projeto
> roda localmente.

### Variáveis de ambiente usadas em produção

Em `prod`, segredos e configurações sensíveis **não ficam no código** — são
lidos de variáveis de ambiente definidas no servidor:

| Variável | Para quê | Exemplo |
|----------|----------|---------|
| `DB_URL` | URL de conexão do banco | `jdbc:postgresql://host:5432/aera` |
| `DB_USERNAME` | Usuário do banco | `aera_app` |
| `DB_PASSWORD` | Senha do banco | (secreta) |
| `APP_CORS_ALLOWED_ORIGINS` | Domínios do frontend autorizados (separados por vírgula) | `https://aera.com.br` |

> **Nota sobre o schema em produção:** hoje o `prod` usa
> `spring.jpa.hibernate.ddl-auto=update` (o Hibernate cria/atualiza as tabelas),
> porque o grupo ainda não adotou um banco definitivo nem *migrations*. Quando
> isso acontecer (ex.: Flyway/Liquibase), o recomendado é trocar para
> `validate` — mais seguro, pois o Hibernate apenas confere o schema sem
> alterá-lo. Isso está comentado no próprio `application-prod.properties`.

### ⚠️ Segurança: ponto em aberto para o grupo

O backend **ainda não possui autenticação/autorização**. Hoje, qualquer
requisição à API consegue criar, editar e excluir dados sem login. Como o
sistema roda apenas em `localhost`, o risco atual é baixo — mas **antes de
qualquer publicação, o grupo precisa decidir em conjunto** como será o controle
de acesso, já que isso atravessa todos os módulos (alunos, patrimônio,
financeiro).

### 2. Frontend (React + Vite)

```bash
cd frontend
npm install                 # só na primeira vez (ou quando mudar dependências)
cp .env.example .env        # opcional por enquanto (ver nota abaixo)
npm run dev                 # inicia o servidor de desenvolvimento
```

A interface ficará disponível em `http://localhost:5173`.

> **Nota sobre o `.env`:** criar o `.env` é **opcional no momento**. O frontend
> lê a variável `VITE_API_BASE_URL`, mas tem um valor padrão embutido: se o
> `.env` não existir, a base da API cai para `/api` automaticamente
> (ver `frontend/src/services/http.ts`). Criar o `.env` passa a ser importante
> na hora de integrar com o backend (para apontar explicitamente para
> `http://localhost:8080/api`) e se, no futuro, surgir alguma variável sem
> valor padrão.

---

## Variáveis de ambiente (frontend)

O arquivo `.env` **não é versionado** (fica só na sua máquina). Use o
`.env.example` como modelo:

```env
# URL base da API backend
VITE_API_BASE_URL=http://localhost:8080/api
```

No Vite, apenas variáveis com o prefixo `VITE_` ficam disponíveis no código do
frontend. Por isso **não coloque segredos** (senhas, tokens) aqui — tudo que vai
para o frontend é embutido no build e fica visível no navegador.

---

## Estado atual do projeto

- [x] Estrutura do monorepo (frontend/ + backend/)
- [x] Frontend React + Vite funcional
- [x] Backend Spring Boot inicializável (Spring Web + Validation + Data JPA)
- [x] **Banco de dados (desenvolvimento)**: H2 em memória, já ativo via Spring
      Data JPA. Decisão **temporária** do grupo enquanto o banco definitivo
      (PostgreSQL/MySQL) não é escolhido. Como usamos JPA, a migração futura
      exige apenas trocar o driver no `pom.xml` e ajustar o datasource.
- [x] **Ambientes separados (profiles dev/prod)** — ver seção acima.
- [x] **Módulo Patrimônio / Materiais**: CRUD completo implementado e integrado
      ao frontend (listar, filtrar, paginar, detalhar, criar, editar, alterar
      situação, excluir) + endpoint de resumo para o Dashboard.
- [ ] Módulo Alunos (a implementar)
- [ ] Módulo Financeiro (a implementar)
- [ ] **Autenticação/Autorização**: ainda não implementada (ver aviso de
      segurança acima).
- [ ] Upload de foto no Patrimônio: previsto no frontend, **em stand-by** no
      backend (o cadastro funciona com os demais campos).

### Contrato de API previsto (definido pelo frontend)

Os endpoints que o backend precisará expor (ver `frontend/src/services/`):

| Recurso        | Métodos               | Status |
|----------------|-----------------------|--------|
| `/patrimonio`  | GET, POST, PATCH, DELETE | ✅ implementado |
| `/patrimonio/{id}` | GET, PATCH, DELETE | ✅ implementado |
| `/patrimonio/resumo` | GET             | ✅ implementado |
| `/alunos`      | GET, POST, PATCH      | a implementar |
| `/visao-geral` | GET                   | a implementar (o Dashboard já consome `/patrimonio/resumo`) |
| `/financeiro/resumo`        | GET      | a implementar |
| `/financeiro/movimentacoes` | GET, POST | a implementar |

---

## Divisão da equipe

- **Frontend:** 2 integrantes
- **Backend:** 3 integrantes (incluindo responsável pela integração)
