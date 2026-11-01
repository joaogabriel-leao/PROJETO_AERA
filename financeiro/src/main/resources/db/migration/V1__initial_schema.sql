CREATE TABLE usuarios (
 id UUID PRIMARY KEY, username VARCHAR(60) NOT NULL UNIQUE,
 senha_hash VARCHAR(255) NOT NULL,
 perfil VARCHAR(30) NOT NULL CHECK (perfil IN ('ADMIN','TESOUREIRO','OPERADOR','CONSULTA')),
 ativo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE contas (
 id UUID PRIMARY KEY, nome VARCHAR(100) NOT NULL UNIQUE, ativa BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE categorias (
 id UUID PRIMARY KEY, nome VARCHAR(100) NOT NULL UNIQUE, ativa BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE movimentacoes (
 id UUID PRIMARY KEY,
 conta_id UUID NOT NULL REFERENCES contas(id),
 categoria_id UUID NOT NULL REFERENCES categorias(id),
 usuario_id UUID NOT NULL REFERENCES usuarios(id),
 tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA')),
 valor NUMERIC(15,2) NOT NULL CHECK (valor > 0),
 data_movimentacao DATE NOT NULL,
 descricao VARCHAR(500) NOT NULL,
 criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_movimentacoes_data ON movimentacoes (data_movimentacao DESC);
CREATE INDEX idx_movimentacoes_conta ON movimentacoes (conta_id);
