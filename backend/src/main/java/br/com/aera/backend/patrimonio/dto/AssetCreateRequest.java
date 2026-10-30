package br.com.aera.backend.patrimonio.dto;

import br.com.aera.backend.patrimonio.domain.AssetCondition;
import br.com.aera.backend.patrimonio.domain.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados recebidos na criacao de um item (POST /patrimonio).
 *
 * <p>As anotacoes de validacao sao verificadas automaticamente pelo Spring
 * quando o controller usa {@code @Valid}. Campos opcionais (ex.: supplier)
 * nao tem restricao de obrigatoriedade, mas tem limite de tamanho para
 * evitar entradas abusivas.</p>
 *
 * <p>Observacao: {@code code} e opcional aqui. Se nao vier, o service gera
 * automaticamente (ex.: PAT-0001).</p>
 */
public record AssetCreateRequest(
        @Size(max = 40, message = "O codigo deve ter no maximo 40 caracteres.")
        String code,

        @NotBlank(message = "O nome do item e obrigatorio.")
        @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres.")
        String name,

        @NotBlank(message = "A categoria e obrigatoria.")
        @Size(max = 255, message = "A categoria deve ter no maximo 255 caracteres.")
        String category,

        @NotNull(message = "A quantidade e obrigatoria.")
        @PositiveOrZero(message = "A quantidade nao pode ser negativa.")
        Integer quantity,

        @NotBlank(message = "A unidade e obrigatoria.")
        @Size(max = 255, message = "A unidade deve ter no maximo 255 caracteres.")
        String unit,

        @NotBlank(message = "A localizacao e obrigatoria.")
        @Size(max = 255, message = "A localizacao deve ter no maximo 255 caracteres.")
        String location,

        @NotNull(message = "O estado de conservacao e obrigatorio.")
        AssetCondition condition,

        @NotNull(message = "A situacao e obrigatoria.")
        AssetStatus status,

        LocalDate acquisitionDate,

        @PositiveOrZero(message = "O valor de aquisicao nao pode ser negativo.")
        BigDecimal acquisitionValue,

        @Size(max = 255, message = "O fornecedor deve ter no maximo 255 caracteres.")
        String supplier,

        @Size(max = 2000, message = "As observacoes devem ter no maximo 2000 caracteres.")
        String notes
) {
}
