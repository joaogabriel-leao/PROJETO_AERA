package br.com.aera.backend.patrimonio.domain;

/**
 * Situacao atual de um item do patrimonio (onde/como ele esta).
 *
 * <p>Os nomes das constantes precisam bater exatamente com os valores
 * esperados pelo frontend (ver {@code AssetStatus} em
 * {@code frontend/src/types/index.ts}).</p>
 */
public enum AssetStatus {
    DISPONIVEL,
    EM_USO,
    EMPRESTADO,
    EM_MANUTENCAO,
    BAIXADO
}
