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
│   │   └── application.properties     # Configurações (porta, context-path, banco)
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
[ Banco     ]  AINDA NÃO DEFINIDO pelo grupo
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

Outros comandos úteis:

```bash
mvn clean test       # compila e roda os testes
mvn clean package    # gera o .jar em backend/target/
```

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
- [x] Backend Spring Boot inicializável (Spring Web + Validation)
- [ ] **Banco de dados**: ainda não definido pelo grupo.
      O Spring Data JPA e os drivers (H2 / PostgreSQL / MySQL) estão
      declarados mas **comentados** em `backend/pom.xml`. A aplicação sobe
      sem banco. Para ativar, siga as instruções comentadas no `pom.xml` e
      no `application.properties`.
- [ ] Controllers / endpoints da API (a implementar)

### Contrato de API previsto (definido pelo frontend)

Os endpoints que o backend precisará expor (ver `frontend/src/services/`):

| Recurso        | Métodos               |
|----------------|-----------------------|
| `/alunos`      | GET, POST, PATCH      |
| `/patrimonio`  | GET, POST, PATCH      |
| `/visao-geral` | GET                   |
| `/financeiro/resumo`        | GET      |
| `/financeiro/movimentacoes` | GET, POST |

---

## Divisão da equipe

- **Frontend:** 2 integrantes
- **Backend:** 3 integrantes (incluindo responsável pela integração)
