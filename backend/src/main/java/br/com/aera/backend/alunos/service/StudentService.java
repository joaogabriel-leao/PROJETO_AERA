package br.com.aera.backend.alunos.service;

import br.com.aera.backend.alunos.domain.Student;
import br.com.aera.backend.alunos.domain.StudentStatus;
import br.com.aera.backend.alunos.dto.StudentCreateRequest;
import br.com.aera.backend.alunos.dto.StudentDetailResponse;
import br.com.aera.backend.alunos.dto.StudentListResponse;
import br.com.aera.backend.alunos.dto.StudentPageResponse;
import br.com.aera.backend.alunos.dto.StudentSummaryResponse;
import br.com.aera.backend.alunos.dto.StudentUpdateRequest;
import br.com.aera.backend.alunos.repository.StudentRepository;
import br.com.aera.backend.common.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Regras cadastrais de alunos. Os campos do Google Forms permanecem provisorios.
 *
 * <p>Codigos ALU-UUID sao gerados sem contador compartilhado. As restricoes
 * unicas de id e code no banco impedem duplicacao mesmo entre varias instancias.
 * Uma eventual colisao e rejeitada atomicamente, sem sobrescrever cadastros.</p>
 */
@Service
@Validated
public class StudentService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final long NEW_STUDENT_DAYS = 30;

    private static final Set<String> OPTIONAL_FIELDS = Set.of(
            "sex",
            "cpf",
            "rg",
            "address",
            "neighborhood",
            "city",
            "zipCode",
            "phone",
            "whatsapp",
            "email",
            "guardianName",
            "guardianCpf",
            "guardianPhone",
            "guardianWhatsapp",
            "relationship",
            "modality",
            "level",
            "classGroup",
            "startDate",
            "notes",
            "importantInformation");

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    /** UUID e data de cadastro sao gerados pelo JPA e pelo callback da entidade. */
    @Transactional
    public StudentDetailResponse create(@NotNull @Valid StudentCreateRequest request) {
        Student student = new Student();
        student.setCode("ALU-" + UUID.randomUUID());
        student.setFullName(normalize(request.fullName()));
        student.setBirthDate(request.birthDate());
        student.setSex(normalize(request.sex()));
        student.setCpf(normalize(request.cpf()));
        student.setRg(normalize(request.rg()));
        student.setAddress(normalize(request.address()));
        student.setNeighborhood(normalize(request.neighborhood()));
        student.setCity(normalize(request.city()));
        student.setZipCode(normalize(request.zipCode()));
        student.setPhone(normalize(request.phone()));
        student.setWhatsapp(normalize(request.whatsapp()));
        student.setEmail(normalize(request.email()));
        student.setGuardianName(normalize(request.guardianName()));
        student.setGuardianCpf(normalize(request.guardianCpf()));
        student.setGuardianPhone(normalize(request.guardianPhone()));
        student.setGuardianWhatsapp(normalize(request.guardianWhatsapp()));
        student.setRelationship(normalize(request.relationship()));
        student.setModality(normalize(request.modality()));
        student.setLevel(normalize(request.level()));
        student.setClassGroup(normalize(request.classGroup()));
        student.setStartDate(request.startDate());
        student.setStatus(request.status());
        student.setNotes(normalize(request.notes()));
        student.setImportantInformation(normalize(request.importantInformation()));
        return StudentDetailResponse.from(repository.saveAndFlush(student));
    }

    @Transactional(readOnly = true)
    public StudentDetailResponse getById(@NotNull UUID id) {
        return StudentDetailResponse.from(findOrThrow(id));
    }

    /**
     * Busca por nome, codigo ou responsavel sem diferenciar maiusculas.
     * Busca e filtros sao combinados com AND. Paginas iniciam em zero;
     * valores negativos sao normalizados e o tamanho fica entre 1 e 100.
     */
    @Transactional(readOnly = true)
    public StudentPageResponse list(
            String search, StudentStatus status, String modality, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "code"));
        var result = repository.findAll(buildSpecification(search, status, modality), pageable)
                .map(StudentListResponse::from);
        return StudentPageResponse.from(result);
    }

    /**
     * Atualizacao parcial: null/omissao preserva o valor atual.
     * Texto opcional vazio limpa o campo, persistindo null. Textos continuam
     * sujeitos as validacoes do DTO antes da normalizacao de espacos.
     * clearFields permite limpar explicitamente qualquer campo opcional,
     * inclusive startDate. Informar e limpar o mesmo campo e rejeitado.
     * Identificador, codigo e data de cadastro nao fazem parte deste contrato.
     */
    @Transactional
    public StudentDetailResponse update(
            @NotNull UUID id, @NotNull @Valid StudentUpdateRequest request) {
        Set<String> clearFields = request.clearFields() == null ? Set.of() : request.clearFields();
        if (!OPTIONAL_FIELDS.containsAll(clearFields)) {
            throw new IllegalArgumentException("Somente campos cadastrais opcionais podem ser limpos.");
        }

        Student student = findOrThrow(id);
        if (request.fullName() != null) {
            student.setFullName(request.fullName().strip());
        }
        if (request.birthDate() != null) {
            student.setBirthDate(request.birthDate());
        }
        applyOptional("sex", request.sex(), clearFields,
                value -> student.setSex(normalize(value)));
        applyOptional("cpf", request.cpf(), clearFields,
                value -> student.setCpf(normalize(value)));
        applyOptional("rg", request.rg(), clearFields,
                value -> student.setRg(normalize(value)));
        applyOptional("address", request.address(), clearFields,
                value -> student.setAddress(normalize(value)));
        applyOptional("neighborhood", request.neighborhood(), clearFields,
                value -> student.setNeighborhood(normalize(value)));
        applyOptional("city", request.city(), clearFields,
                value -> student.setCity(normalize(value)));
        applyOptional("zipCode", request.zipCode(), clearFields,
                value -> student.setZipCode(normalize(value)));
        applyOptional("phone", request.phone(), clearFields,
                value -> student.setPhone(normalize(value)));
        applyOptional("whatsapp", request.whatsapp(), clearFields,
                value -> student.setWhatsapp(normalize(value)));
        applyOptional("email", request.email(), clearFields,
                value -> student.setEmail(normalize(value)));
        applyOptional("guardianName", request.guardianName(), clearFields,
                value -> student.setGuardianName(normalize(value)));
        applyOptional("guardianCpf", request.guardianCpf(), clearFields,
                value -> student.setGuardianCpf(normalize(value)));
        applyOptional("guardianPhone", request.guardianPhone(), clearFields,
                value -> student.setGuardianPhone(normalize(value)));
        applyOptional("guardianWhatsapp", request.guardianWhatsapp(), clearFields,
                value -> student.setGuardianWhatsapp(normalize(value)));
        applyOptional("relationship", request.relationship(), clearFields,
                value -> student.setRelationship(normalize(value)));
        applyOptional("modality", request.modality(), clearFields,
                value -> student.setModality(normalize(value)));
        applyOptional("level", request.level(), clearFields,
                value -> student.setLevel(normalize(value)));
        applyOptional("classGroup", request.classGroup(), clearFields,
                value -> student.setClassGroup(normalize(value)));
        applyOptional("startDate", request.startDate(), clearFields,
                student::setStartDate);
        if (request.status() != null) {
            student.setStatus(request.status());
        }
        applyOptional("notes", request.notes(), clearFields,
                value -> student.setNotes(normalize(value)));
        applyOptional("importantInformation", request.importantInformation(), clearFields,
                value -> student.setImportantInformation(normalize(value)));
        return StudentDetailResponse.from(repository.saveAndFlush(student));
    }

    /** Alterar para a situacao atual e permitido; nao ha outras transicoes presumidas. */
    @Transactional
    public StudentDetailResponse updateStatus(@NotNull UUID id, @NotNull StudentStatus status) {
        Student student = findOrThrow(id);
        student.setStatus(status);
        return StudentDetailResponse.from(repository.saveAndFlush(student));
    }

    /** Totais para a Visao Geral. "Novos" = cadastrados nos ultimos 30 dias. */
    @Transactional(readOnly = true)
    public StudentSummaryResponse summary() {
        return new StudentSummaryResponse(
                repository.count(),
                repository.countByStatus(StudentStatus.ATIVO),
                repository.countByStatus(StudentStatus.INATIVO),
                repository.countByCreatedAtGreaterThanEqual(OffsetDateTime.now().minusDays(NEW_STUDENT_DAYS)));
    }

    /** Exclusao definitiva do cadastro. Lanca 404 (ResourceNotFoundException) se nao existir. */
    @Transactional
    public void delete(@NotNull UUID id) {
        repository.delete(findOrThrow(id));
        repository.flush();
    }

    private Student findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aluno nao encontrado para o id '" + id + "'."));
    }

    private Specification<Student> buildSpecification(
            String search, StudentStatus status, String modality) {
        String normalizedSearch = normalize(search);
        String normalizedModality = normalize(modality);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (normalizedSearch != null) {
                String like = "%" + escapeLike(normalizedSearch.toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), like, '\\'),
                        cb.like(cb.lower(root.get("code")), like, '\\'),
                        cb.like(cb.lower(root.get("guardianName")), like, '\\')));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (normalizedModality != null) {
                predicates.add(cb.equal(cb.lower(root.get("modality")),
                        normalizedModality.toLowerCase(Locale.ROOT)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private <T> void applyOptional(
            String field, T value, Set<String> clearFields, Consumer<T> setter) {
        if (clearFields.contains(field)) {
            if (value != null) {
                throw new IllegalArgumentException(
                        "O campo '" + field + "' nao pode ser alterado e limpo na mesma requisicao.");
            }
            setter.accept(null);
        } else if (value != null) {
            setter.accept(value);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
