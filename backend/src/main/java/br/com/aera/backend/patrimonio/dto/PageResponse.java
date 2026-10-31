package br.com.aera.backend.patrimonio.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Envelope de paginacao no formato esperado pelo frontend.
 *
 * <p>Corresponde ao tipo {@code PageResult<T>} em
 * {@code frontend/src/types/index.ts}:
 * {@code { content, page, size, totalElements, totalPages }}.</p>
 *
 * <p>Nao expomos o objeto {@code Page} do Spring diretamente porque o JSON
 * dele tem um formato diferente (e instavel entre versoes).</p>
 *
 * @param <T> tipo dos itens da pagina
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /** Monta um {@link PageResponse} a partir de um {@link Page} do Spring Data. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
