package br.com.aera.backend.alunos.repository;

import br.com.aera.backend.alunos.domain.Student;
import br.com.aera.backend.alunos.domain.StudentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Persistencia de alunos com operacoes basicas e suporte a filtros dinamicos,
 * seguindo o padrao do repositorio de patrimonio.
 */
@Repository
public interface StudentRepository
        extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {

    long countByStatus(StudentStatus status);

    long countByCreatedAtGreaterThanEqual(OffsetDateTime since);
}
