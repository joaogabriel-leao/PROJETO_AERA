package br.com.aera.backend.patrimonio.dto;

import br.com.aera.backend.patrimonio.domain.AssetCondition;
import br.com.aera.backend.patrimonio.domain.AssetStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados recebidos na atualizacao parcial de um item (PATCH /patrimonio/{id}).
 *
 * <p>Todos os campos sao opcionais: somente os que vierem preenchidos
 * (nao nulos) serao aplicados. Os demais permanecem com o valor atual.
 * Os limites de tamanho (@Size) protegem contra entradas abusivas.</p>
 */
public record AssetUpdateRequest(
        @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres.")
        String name,

        @Size(max = 255, message = "A categoria deve ter no maximo 255 caracteres.")
        String category,

        @PositiveOrZero(message = "A quantidade nao pode ser negativa.")
        Integer quantity,

        @Size(max = 255, message = "A unidade deve ter no maximo 255 caracteres.")
        String unit,

        @Size(max = 255, message = "A localizacao deve ter no maximo 255 caracteres.")
        String location,

        AssetCondition condition,
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
