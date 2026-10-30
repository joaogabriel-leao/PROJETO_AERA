package br.com.aera.backend.patrimonio.dto;

/**
 * Resumo do patrimonio para a tela de Visao Geral (Dashboard).
 *
 * <p>Espelha o objeto {@code assets} do tipo {@code DashboardSummary} do
 * frontend ({@code frontend/src/types/index.ts}):
 * {@code { total, available, inUse, maintenance, damaged }}.</p>
 *
 * @param total       total de itens cadastrados
 * @param available   itens com situacao DISPONIVEL
 * @param inUse       itens com situacao EM_USO
 * @param maintenance itens com situacao EM_MANUTENCAO
 * @param damaged     itens com estado de conservacao DANIFICADO
 */
public record AssetSummaryResponse(
        long total,
        long available,
        long inUse,
        long maintenance,
        long damaged
) {
}
