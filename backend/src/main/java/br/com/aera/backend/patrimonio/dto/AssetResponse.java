package br.com.aera.backend.patrimonio.dto;

import br.com.aera.backend.patrimonio.domain.Asset;
import br.com.aera.backend.patrimonio.domain.AssetCondition;
import br.com.aera.backend.patrimonio.domain.AssetStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Formato de saida de um item na API.
 *
 * <p>Espelha exatamente o tipo {@code Asset} do frontend
 * ({@code frontend/src/types/index.ts}), garantindo que o JSON retornado
 * seja consumido sem ajustes.</p>
 */
public record AssetResponse(
        String id,
        String code,
        String name,
        String category,
        Integer quantity,
        String unit,
        String location,
        AssetCondition condition,
        AssetStatus status,
        LocalDate acquisitionDate,
        BigDecimal acquisitionValue,
        String supplier,
        String notes
) {

    /** Converte uma entidade {@link Asset} em DTO de resposta. */
    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getCode(),
                asset.getName(),
                asset.getCategory(),
                asset.getQuantity(),
                asset.getUnit(),
                asset.getLocation(),
                asset.getCondition(),
                asset.getStatus(),
                asset.getAcquisitionDate(),
                asset.getAcquisitionValue(),
                asset.getSupplier(),
                asset.getNotes()
        );
    }
}
