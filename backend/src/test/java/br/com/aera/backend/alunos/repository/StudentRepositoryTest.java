package br.com.aera.backend.alunos.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.aera.backend.alunos.domain.Student;
import br.com.aera.backend.alunos.domain.StudentStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void devePersistirCamposAdicionaisPreservandoIdentificacaoEDataDeCadastro() {
        Student student = alunoFicticio("ALU-TESTE-1");
        student.setCpf("CPF-FICTICIO");
        student.setRg("RG-FICTICIO");
        student.setSex("Prefiro nao informar");
        student.setPhone("00000000000");
        student.setWhatsapp("00000000000");
        student.setEmail("aluno@example.invalid");
        student.setAddress("Endereco ficticio");
        student.setNeighborhood("Bairro ficticio");
        student.setCity("Cidade ficticia");
        student.setZipCode("00000-000");
        student.setGuardianName("Responsavel ficticio");
        student.setGuardianCpf("RESP-FICTICIO");
        student.setGuardianPhone("00000000000");
        student.setGuardianWhatsapp("00000000000");
        student.setRelationship("Outro");
        student.setModality("Surf");
        student.setLevel("Iniciante");
        student.setClassGroup("Turma ficticia");
        student.setStartDate(LocalDate.of(2026, 1, 10));
        student.setNotes("Observacao ficticia");
        student.setImportantInformation("Informacao ficticia");

        Student saved = repository.saveAndFlush(student);
        entityManager.refresh(saved);
        UUID id = saved.getId();
        OffsetDateTime createdAt = saved.getCreatedAt();
        assertThat(id).isNotNull();
        assertThat(createdAt).isNotNull();
        entityManager.clear();

        Student loaded = repository.findById(id).orElseThrow();
        assertThat(loaded).usingRecursiveComparison().isEqualTo(saved);

        loaded.setStatus(StudentStatus.INATIVO);
        repository.flush();
        entityManager.clear();

        Student updated = repository.findById(id).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(StudentStatus.INATIVO);
        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getCode()).isEqualTo("ALU-TESTE-1");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void devePersistirCadastroSemDocumentosOuResponsavel() {
        Student saved = repository.saveAndFlush(alunoFicticio("ALU-TESTE-2"));
        entityManager.clear();

        Student loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getCpf()).isNull();
        assertThat(loaded.getRg()).isNull();
        assertThat(loaded.getGuardianName()).isNull();
        assertThat(loaded.getGuardianCpf()).isNull();
    }

    @Test
    void deveManterUnicidadeDoCodigo() {
        repository.saveAndFlush(alunoFicticio("ALU-DUPLICADO"));

        assertThatThrownBy(() -> repository.saveAndFlush(alunoFicticio("ALU-DUPLICADO")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Student alunoFicticio(String code) {
        Student student = new Student();
        student.setCode(code);
        student.setFullName("Aluno ficticio");
        student.setBirthDate(LocalDate.of(2010, 6, 15));
        student.setStatus(StudentStatus.ATIVO);
        return student;
    }
}
