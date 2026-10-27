# Integração com API

Os serviços deste diretório centralizam a comunicação com o backend Spring Boot. A interface visual não chama a API enquanto ela não estiver disponível, preservando o protótipo e seus estados vazios atuais.

## Configuração

Copie `.env.example` para `.env.local` e informe a URL base da API:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Contrato inicial

- `GET | POST | PATCH /alunos`
- `GET | POST | PATCH /patrimonio`
- `GET /visao-geral`
- `GET /financeiro/resumo`
- `GET | POST /financeiro/movimentacoes`

Os caminhos ficam concentrados em `endpoints.ts`; ajuste-os caso o contrato final do backend use nomes diferentes.
