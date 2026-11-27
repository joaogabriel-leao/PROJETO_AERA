package br.com.aera.backend.alunos.dto;

/**
 * Resumo de alunos para a Visao Geral. Espelha {@code DashboardSummary.students}
 * do frontend: {@code { total, active, inactive, new }}.
 *
 * @param total    alunos cadastrados
 * @param active   alunos com situacao ATIVO
 * @param inactive alunos com situacao INATIVO
 * @param newCount alunos cadastrados nos ultimos 30 dias (serializado como "new")
 */
public record StudentSummaryResponse(
        long total,
        long active,
        long inactive,
        @com.fasterxml.jackson.annotation.JsonProperty("new") long newCount
) {
}
