package br.com.aera.backend.alunos.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Paginacao de alunos no formato PageResult do frontend, sem expor entidades. */
public record StudentPageResponse(
        List<StudentListResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static StudentPageResponse from(Page<StudentListResponse> page) {
        return new StudentPageResponse(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
