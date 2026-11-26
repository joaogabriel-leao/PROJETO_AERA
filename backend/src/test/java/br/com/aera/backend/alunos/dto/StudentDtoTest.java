package br.com.aera.backend.alunos.dto;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.aera.backend.alunos.domain.Student;
import br.com.aera.backend.alunos.domain.StudentStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class StudentDtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @BeforeAll
    static void configurarValidacao() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void encerrarValidacao() {
        factory.close();
    }

    @Test
    void deveAceitarCadastroSemDocumentosOuResponsavel() throws Exception {
        StudentCreateRequest request = mapper.treeToValue(cadastroMinimo(), StudentCreateRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.cpf()).isNull();
        assertThat(request.rg()).isNull();
        assertThat(request.guardianName()).isNull();
        assertThat(request.guardianCpf()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void deveRejeitarCadastroSemNomeValido(String name) throws Exception {
        ObjectNode payload = cadastroMinimo();
        payload.put("fullName", name);

        assertThat(validator.validate(mapper.treeToValue(payload, StudentCreateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("fullName");
    }

    @ParameterizedTest
    @ValueSource(strings = {"birthDate", "status"})
    void deveExigirNascimentoESituacaoNoCadastro(String field) throws Exception {
        ObjectNode payload = cadastroMinimo();
        payload.remove(field);

        assertThat(validator.validate(mapper.treeToValue(payload, StudentCreateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(field);
    }

    @Test
    void deveAceitarCamposOpcionaisSemImporFormatoDeDocumento() throws Exception {
        ObjectNode payload = cadastroMinimo();
        payload.put("cpf", "CPF-FICTICIO");
        payload.put("rg", "RG-FICTICIO");
        payload.put("guardianCpf", "DOCUMENTO-FICTICIO");
        payload.put("phone", "+00 (00) 00000-0000");
        payload.put("email", "aluno@example.invalid");
        payload.put("sex", "Prefiro nao informar");

        StudentCreateRequest request = mapper.treeToValue(payload, StudentCreateRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.phone()).isEqualTo("+00 (00) 00000-0000");
    }

    @Test
    void deveAceitarAtualizacaoVaziaSemExigirCamposDeCadastro() throws Exception {
        StudentUpdateRequest request = mapper.readValue("{}", StudentUpdateRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.birthDate()).isNull();
        assertThat(request.status()).isNull();
    }

    @Test
    void deveAceitarAtualizacaoSomenteDaSituacao() throws Exception {
        StudentUpdateRequest request = mapper.readValue("{\"status\":\"INATIVO\"}", StudentUpdateRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.status()).isEqualTo(StudentStatus.INATIVO);
        assertThat(request.fullName()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    void deveRejeitarNomeEmBrancoQuandoInformadoNaAtualizacao(String name) throws Exception {
        ObjectNode payload = mapper.createObjectNode().put("fullName", name);

        assertThat(validator.validate(mapper.treeToValue(payload, StudentUpdateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("fullName");
    }

    @Test
    void deveRejeitarNascimentoFuturoNosDoisContratos() throws Exception {
        ObjectNode payload = cadastroMinimo();
        payload.put("birthDate", LocalDate.now().plusDays(1).toString());

        assertThat(validator.validate(mapper.treeToValue(payload, StudentCreateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString()).contains("birthDate");
        assertThat(validator.validate(mapper.treeToValue(payload, StudentUpdateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString()).contains("birthDate");
    }

    @Test
    void deveRejeitarEmailInvalidoNosDoisContratos() throws Exception {
        ObjectNode payload = cadastroMinimo().put("email", "sem-arroba");

        assertThat(validator.validate(mapper.treeToValue(payload, StudentCreateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString()).contains("email");
        assertThat(validator.validate(mapper.treeToValue(payload, StudentUpdateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString()).contains("email");
    }

    @ParameterizedTest
    @CsvSource({"fullName,255", "phone,30", "notes,2000", "importantInformation,2000"})
    void deveRespeitarLimitesDeArmazenamentoNosDoisContratos(String field, int limit) throws Exception {
        ObjectNode payload = cadastroMinimo().put(field, "x".repeat(limit));

        assertThat(validator.validate(mapper.treeToValue(payload, StudentCreateRequest.class))).isEmpty();
        assertThat(validator.validate(mapper.treeToValue(payload, StudentUpdateRequest.class))).isEmpty();

        payload.put(field, "x".repeat(limit + 1));

        assertThat(validator.validate(mapper.treeToValue(payload, StudentCreateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString()).contains(field);
        assertThat(validator.validate(mapper.treeToValue(payload, StudentUpdateRequest.class)))
                .extracting(violation -> violation.getPropertyPath().toString()).contains(field);
    }

    @Test
    void listagemDeveExporApenasCamposNecessariosATabela() {
        Student student = alunoFicticio();
        JsonNode json = mapper.valueToTree(StudentListResponse.from(student));

        assertThat(json.properties()).extracting(entry -> entry.getKey())
                .containsExactlyInAnyOrder("id", "code", "fullName", "birthDate", "phone",
                        "guardianName", "modality", "status", "createdAt");
        assertThat(json.get("id").asText()).isEqualTo(student.getId().toString());
        assertThat(json.get("birthDate").asText()).isEqualTo("2010-06-15");
        assertThat(json.get("status").asText()).isEqualTo("ATIVO");
    }

    @Test
    void detalheDeveIncluirDadosCadastraisEDatasSemArmazenarIdade() {
        Student student = alunoFicticio();
        JsonNode json = mapper.valueToTree(StudentDetailResponse.from(student));

        assertThat(json.get("cpf").asText()).isEqualTo("CPF-FICTICIO");
        assertThat(json.get("rg").asText()).isEqualTo("RG-FICTICIO");
        assertThat(json.get("guardianCpf").asText()).isEqualTo("RESP-FICTICIO");
        assertThat(json.get("address").asText()).isEqualTo("Endereco ficticio");
        assertThat(json.get("sex").asText()).isEqualTo("Prefiro nao informar");
        assertThat(json.get("importantInformation").asText()).isEqualTo("Informacao ficticia");
        assertThat(json.get("startDate").asText()).isEqualTo("2026-01-10");
        assertThat(json.get("createdAt").asText()).isEqualTo("2026-01-10T12:00:00Z");
        assertThat(json.has("age")).isFalse();
        assertThat(json.size()).isEqualTo(27);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarNomeVazioNaListaDeCamposALimpar(String field) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.putArray("clearFields").add(field);

        assertThat(validator.validate(mapper.treeToValue(payload, StudentUpdateRequest.class)))
                .isNotEmpty();
    }

    @Test
    void deveAceitarPedidoExplicitoDeLimpezaDeTextoEData() throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.putArray("clearFields").add("phone").add("startDate");
        StudentUpdateRequest request = mapper.treeToValue(payload, StudentUpdateRequest.class);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.clearFields()).containsExactlyInAnyOrder("phone", "startDate");
    }

    private ObjectNode cadastroMinimo() {
        return mapper.createObjectNode()
                .put("fullName", "Aluno ficticio")
                .put("birthDate", "2010-06-15")
                .put("status", "ATIVO");
    }

    private Student alunoFicticio() {
        Student student = new Student();
        student.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        student.setCode("ALU-TESTE");
        student.setFullName("Aluno ficticio");
        student.setBirthDate(LocalDate.of(2010, 6, 15));
        student.setStatus(StudentStatus.ATIVO);
        student.setCreatedAt(OffsetDateTime.parse("2026-01-10T12:00:00Z"));
        student.setPhone("00000000000");
        student.setGuardianName("Responsavel ficticio");
        student.setModality("Surf");
        student.setCpf("CPF-FICTICIO");
        student.setRg("RG-FICTICIO");
        student.setGuardianCpf("RESP-FICTICIO");
        student.setAddress("Endereco ficticio");
        student.setSex("Prefiro nao informar");
        student.setImportantInformation("Informacao ficticia");
        student.setStartDate(LocalDate.of(2026, 1, 10));
        return student;
    }
}
