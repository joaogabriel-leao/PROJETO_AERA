package br.com.aera.backend.alunos.dto;

import br.com.aera.backend.alunos.domain.Student;
import br.com.aera.backend.alunos.domain.StudentStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Ficha completa do aluno, destinada a consultas administrativas autorizadas. */
public record StudentDetailResponse(
        UUID id,
        String code,
        String fullName,
        LocalDate birthDate,
        String sex,
        String cpf,
        String rg,
        String address,
        String neighborhood,
        String city,
        String zipCode,
        String phone,
        String whatsapp,
        String email,
        String guardianName,
        String guardianCpf,
        String guardianPhone,
        String guardianWhatsapp,
        String relationship,
        String modality,
        String level,
        String classGroup,
        LocalDate startDate,
        StudentStatus status,
        String notes,
        String importantInformation,
        OffsetDateTime createdAt
) {
    public static StudentDetailResponse from(Student student) {
        return new StudentDetailResponse(
                student.getId(),
                student.getCode(),
                student.getFullName(),
                student.getBirthDate(),
                student.getSex(),
                student.getCpf(),
                student.getRg(),
                student.getAddress(),
                student.getNeighborhood(),
                student.getCity(),
                student.getZipCode(),
                student.getPhone(),
                student.getWhatsapp(),
                student.getEmail(),
                student.getGuardianName(),
                student.getGuardianCpf(),
                student.getGuardianPhone(),
                student.getGuardianWhatsapp(),
                student.getRelationship(),
                student.getModality(),
                student.getLevel(),
                student.getClassGroup(),
                student.getStartDate(),
                student.getStatus(),
                student.getNotes(),
                student.getImportantInformation(),
                student.getCreatedAt()
        );
    }
}
