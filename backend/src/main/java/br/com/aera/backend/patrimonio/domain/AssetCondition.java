package br.com.aera.backend.patrimonio.domain;

/**
 * Estado de conservacao de um item do patrimonio.
 *
 * <p>Os nomes das constantes precisam bater exatamente com os valores
 * esperados pelo frontend (ver {@code AssetCondition} em
 * {@code frontend/src/types/index.ts}), pois o Jackson converte
 * enum &lt;-&gt; JSON usando o nome da constante.</p>
 */
public enum AssetCondition {
    NOVO,
    BOM,
    REGULAR,
    NECESSITA_MANUTENCAO,
    DANIFICADO,
    INUTILIZADO
}
