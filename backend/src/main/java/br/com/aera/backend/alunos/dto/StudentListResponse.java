package br.com.aera.backend.alunos.dto;

import br.com.aera.backend.alunos.domain.Student;
import br.com.aera.backend.alunos.domain.StudentStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Resumo para a tabela, sem documentos, endereco ou observacoes sensiveis. */
public record StudentListResponse(
        UUID id,
        String code,
        String fullName,
        LocalDate birthDate,
        String phone,
        String guardianName,
        String modality,
        StudentStatus status,
        OffsetDateTime createdAt
) {
    public static StudentListResponse from(Student student) {
        return new StudentListResponse(
                student.getId(),
                student.getCode(),
                student.getFullName(),
                student.getBirthDate(),
                student.getPhone(),
                student.getGuardianName(),
                student.getModality(),
                student.getStatus(),
                student.getCreatedAt()
        );
    }
}
