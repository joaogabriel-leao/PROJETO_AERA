package br.com.aera.backend.alunos.dto;

import br.com.aera.backend.alunos.domain.StudentStatus;
import jakarta.validation.constraints.NotNull;

/** Corpo de PATCH /api/alunos/{id}/situacao, compativel com studentService.updateStatus. */
public record StudentStatusUpdateRequest(
        @NotNull(message = "A situacao e obrigatoria.")
        StudentStatus status
) {
}
