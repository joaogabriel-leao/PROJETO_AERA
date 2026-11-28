package br.com.aera.backend.alunos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.aera.backend.alunos.domain.StudentStatus;
import br.com.aera.backend.alunos.dto.StudentCreateRequest;
import br.com.aera.backend.alunos.dto.StudentDetailResponse;
import br.com.aera.backend.alunos.dto.StudentUpdateRequest;
import br.com.aera.backend.alunos.repository.StudentRepository;
import br.com.aera.backend.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Integracao real entre service, validacao, transacoes, repository e H2. */
@SpringBootTest
class StudentServiceTest {

    @Autowired
    private StudentService service;

    @Autowired
    private StudentRepository repository;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    @AfterEach
    void limparAlunos() {
        repository.deleteAll();
    }

    @Test
    void deveCadastrarGerandoIdentificacaoCodigoEDataNoServidor() {
        StudentDetailResponse saved = service.create(createRequest(Map.of()));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.code()).matches("ALU-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(saved.createdAt()).isNotNull();
        assertThat(saved.status()).isEqualTo(StudentStatus.ATIVO);
        assertThat(saved.cpf()).isNull();
        assertThat(repository.count()).isEqualTo(1);
        assertThat(service.getById(saved.id()).fullName()).isEqualTo("Aluno ficticio");
    }

    @Test
    void deveCadastrarEConsultarTodosOsCamposOpcionais() {
        Map<String, Object> payload = cadastroCompleto();
        StudentDetailResponse saved = service.create(mapper.convertValue(payload, StudentCreateRequest.class));
        JsonNode loaded = mapper.valueToTree(service.getById(saved.id()));

        payload.forEach((field, value) -> assertThat(loaded.get(field))
                .as(field).isEqualTo(mapper.valueToTree(value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"fullName", "birthDate", "status"})
    void deveRejeitarCadastroSemCampoObrigatorio(String field) {
        Map<String, Object> payload = cadastroMinimo();
        payload.remove(field);

        assertThatThrownBy(() -> service.create(mapper.convertValue(payload, StudentCreateRequest.class)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(repository.count()).isZero();
    }

    @Test
    void deveRejeitarNomeEmBrancoENascimentoFuturoNoCadastro() {
        assertThatThrownBy(() -> service.create(createRequest(Map.of("fullName", "   "))))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.create(createRequest(Map.of(
                "birthDate", LocalDate.now().plusDays(1).toString()))))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(repository.count()).isZero();
    }

    @Test
    void deveExcluirAlunoExistenteERejeitarNovaExclusao() {
        StudentDetailResponse saved = service.create(createRequest(Map.of()));

        service.delete(saved.id());

        assertThat(repository.existsById(saved.id())).isFalse();
        assertThatThrownBy(() -> service.delete(saved.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.getById(saved.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveRejeitarExclusaoDeAlunoInexistente() {
        assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveRejeitarExclusaoSemId() {
        assertThatThrownBy(() -> service.delete(null))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"consulta", "atualizacao", "situacao"})
    void deveTratarAlunoNaoEncontrado(String operation) {
        UUID missing = UUID.randomUUID();

        assertThatThrownBy(() -> {
            switch (operation) {
                case "consulta" -> service.getById(missing);
                case "atualizacao" -> service.update(missing, updateRequest(Map.of("fullName", "Outro aluno ficticio")));
                case "situacao" -> service.updateStatus(missing, StudentStatus.INATIVO);
                default -> throw new AssertionError("Operacao de teste desconhecida.");
            }
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveBuscarPorNomeCodigoOuResponsavelSemDiferenciarMaiusculas() {
        StudentDetailResponse saved = service.create(createRequest(Map.of(
                "fullName", "Aluno Azul Ficticio", "guardianName", "Responsavel Verde Ficticio")));
        service.create(createRequest(Map.of("fullName", "Outro aluno ficticio")));

        assertThat(service.list("  AZUL  ", null, null, 0, 20).content())
                .extracting(item -> item.id()).containsExactly(saved.id());
        assertThat(service.list("VERDE", null, null, 0, 20).content())
                .extracting(item -> item.id()).containsExactly(saved.id());
        assertThat(service.list(saved.code().toLowerCase(), null, null, 0, 20).content())
                .extracting(item -> item.id()).containsExactly(saved.id());
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_", "\\"})
    void deveBuscarCuringasComoTextoLiteral(String symbol) {
        StudentDetailResponse saved = service.create(createRequest(Map.of("fullName", "Aluno " + symbol + " ficticio")));
        service.create(createRequest(Map.of("fullName", "Aluno comum ficticio")));

        assertThat(service.list(symbol, null, null, 0, 20).content())
                .extracting(item -> item.id()).containsExactly(saved.id());
    }

    @Test
    void deveCombinarBuscaSituacaoEModalidade() {
        StudentDetailResponse saved = service.create(createRequest(Map.of("modality", "Surf")));
        service.create(createRequest(Map.of("modality", "Surf", "status", "INATIVO")));
        service.create(createRequest(Map.of("modality", "Bodyboard")));

        assertThat(service.list("aluno", StudentStatus.ATIVO, "  SURF  ", 0, 20).content())
                .extracting(item -> item.id()).containsExactly(saved.id());
        assertThat(service.list(null, StudentStatus.INATIVO, null, 0, 20).totalElements()).isEqualTo(1);
        assertThat(service.list(null, null, "Bodyboard", 0, 20).totalElements()).isEqualTo(1);
    }

    @Test
    void devePaginarSemRepetirRegistrosComOrdenacaoEstavel() {
        for (int i = 0; i < 3; i++) {
            service.create(createRequest(Map.of("fullName", "Aluno ficticio " + i)));
        }
        var first = service.list(null, null, null, 0, 2);
        var second = service.list(null, null, null, 1, 2);

        assertThat(first.totalElements()).isEqualTo(3);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.page()).isZero();
        assertThat(first.size()).isEqualTo(2);
        assertThat(first.content()).hasSize(2);
        assertThat(second.content()).hasSize(1);
        List<String> codes = new ArrayList<>();
        first.content().forEach(item -> codes.add(item.code()));
        second.content().forEach(item -> codes.add(item.code()));
        assertThat(codes).doesNotHaveDuplicates().isSorted();
        assertThat(service.list(null, null, null, 10, 2).content()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"-1,0,0,1", "0,-5,0,1", "-5,10000,0,100"})
    void deveNormalizarPaginacao(int page, int size, int expectedPage, int expectedSize) {
        var result = service.list(null, null, null, page, size);

        assertThat(result.page()).isEqualTo(expectedPage);
        assertThat(result.size()).isEqualTo(expectedSize);
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void deveIgnorarFiltrosEmBrancoERetornarVazioQuandoNaoHaCorrespondencia() {
        service.create(createRequest(Map.of()));

        assertThat(service.list("  ", null, "\t", 0, 20).totalElements()).isEqualTo(1);
        assertThat(service.list("inexistente", null, null, 0, 20).content()).isEmpty();
    }

    @Test
    void devePreservarCamposOmitidosEIdentificacaoNaAtualizacaoParcial() {
        var created = service.create(mapper.convertValue(cadastroCompleto(), StudentCreateRequest.class));
        var before = service.getById(created.id());
        var updated = service.update(created.id(), updateRequest(Map.of("fullName", "Nome ficticio atualizado")));

        assertThat(updated.fullName()).isEqualTo("Nome ficticio atualizado");
        assertThat(updated).usingRecursiveComparison().ignoringFields("fullName").isEqualTo(before);
        assertThat(service.getById(created.id())).isEqualTo(updated);
    }

    @Test
    void deveAtualizarTodosOsCamposCadastrais() {
        var created = service.create(createRequest(Map.of()));
        Map<String, Object> payload = cadastroCompleto();
        payload.put("status", "INATIVO");
        var updated = service.update(created.id(), updateRequest(payload));
        JsonNode result = mapper.valueToTree(service.getById(updated.id()));

        payload.forEach((field, value) -> assertThat(result.get(field))
                .as(field).isEqualTo(mapper.valueToTree(value)));
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.code()).isEqualTo(created.code());
    }

    @Test
    void devePreservarValoresEmAtualizacaoVaziaOuComNullExplicito() throws Exception {
        var created = service.create(mapper.convertValue(cadastroCompleto(), StudentCreateRequest.class));
        var before = service.getById(created.id());

        assertThat(service.update(created.id(), updateRequest(Map.of()))).isEqualTo(before);
        var nullRequest = mapper.readValue("{\"phone\":null,\"startDate\":null}", StudentUpdateRequest.class);
        assertThat(service.update(created.id(), nullRequest)).isEqualTo(before);
    }

    @Test
    void deveLimparCamposOpcionaisExplicitamenteIncluindoData() {
        var created = service.create(mapper.convertValue(cadastroCompleto(), StudentCreateRequest.class));
        var updated = service.update(created.id(), updateRequest(Map.of(
                "clearFields", List.of("phone", "cpf", "startDate"))));

        assertThat(updated.phone()).isNull();
        assertThat(updated.cpf()).isNull();
        assertThat(updated.startDate()).isNull();
        assertThat(updated.rg()).isEqualTo("RG-FICTICIO");
        assertThat(service.getById(created.id())).isEqualTo(updated);
    }

    @Test
    void deveNormalizarEspacosELimparTextoOpcionalVazio() {
        var created = service.create(createRequest(Map.of("fullName", "  Aluno ficticio  ",
                "phone", "00000000000", "email", "aluno@example.invalid", "notes", "Texto ficticio")));
        assertThat(created.fullName()).isEqualTo("Aluno ficticio");

        var updated = service.update(created.id(), updateRequest(Map.of(
                "phone", "   ", "email", "", "notes", "", "city", "  Cidade ficticia  ")));

        assertThat(updated.phone()).isNull();
        assertThat(updated.email()).isNull();
        assertThat(updated.notes()).isNull();
        assertThat(updated.city()).isEqualTo("Cidade ficticia");
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "code", "createdAt", "fullName", "birthDate", "status", "desconhecido"})
    void deveRejeitarLimpezaDeCampoObrigatorioTecnicoOuDesconhecido(String field) {
        var created = service.create(createRequest(Map.of()));

        assertThatThrownBy(() -> service.update(created.id(), updateRequest(Map.of("clearFields", List.of(field)))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(service.getById(created.id()).fullName()).isEqualTo(created.fullName());
    }

    @Test
    void deveReverterTransacaoQuandoAtualizacaoELimpezaDoMesmoCampoConflitam() {
        var created = service.create(createRequest(Map.of("notes", "Texto ficticio original")));
        var before = service.getById(created.id());

        assertThatThrownBy(() -> service.update(created.id(), updateRequest(Map.of(
                "fullName", "Nome ficticio alterado", "notes", "Outro texto ficticio",
                "clearFields", List.of("notes")))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(service.getById(created.id())).isEqualTo(before);
    }

    @Test
    void deveRejeitarNascimentoFuturoENomeEmBrancoNaAtualizacao() {
        var created = service.create(createRequest(Map.of()));
        var before = service.getById(created.id());

        assertThatThrownBy(() -> service.update(created.id(), updateRequest(Map.of(
                "birthDate", LocalDate.now().plusDays(1).toString()))))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.update(created.id(), updateRequest(Map.of("fullName", "  "))))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(service.getById(created.id())).isEqualTo(before);
    }

    @Test
    void deveAlterarSituacaoNosDoisSentidosSemAlterarOutrosDados() {
        var created = service.create(mapper.convertValue(cadastroCompleto(), StudentCreateRequest.class));
        var before = service.getById(created.id());
        var inactive = service.updateStatus(created.id(), StudentStatus.INATIVO);

        assertThat(inactive.status()).isEqualTo(StudentStatus.INATIVO);
        assertThat(inactive).usingRecursiveComparison().ignoringFields("status").isEqualTo(before);
        assertThat(service.updateStatus(created.id(), StudentStatus.ATIVO)).isEqualTo(before);
        assertThat(service.updateStatus(created.id(), StudentStatus.ATIVO)).isEqualTo(before);
        assertThatThrownBy(() -> service.updateStatus(created.id(), null))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void deveRejeitarArgumentosObrigatoriosNulos() {
        assertThatThrownBy(() -> service.create(null)).isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.getById(null)).isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.update(UUID.randomUUID(), null)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void deveCadastrarConcorrentementeSemDuplicarIdsOuCodigos() throws Exception {
        var executor = Executors.newFixedThreadPool(4);
        var ready = new CountDownLatch(4);
        var start = new CountDownLatch(1);
        List<Future<List<StudentDetailResponse>>> futures = new ArrayList<>();
        try {
            for (int worker = 0; worker < 4; worker++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Tempo esgotado aguardando inicio do teste.");
                    }
                    List<StudentDetailResponse> results = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        results.add(service.create(createRequest(Map.of())));
                    }
                    return results;
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<StudentDetailResponse> results = new ArrayList<>();
            for (var future : futures) {
                results.addAll(future.get(30, TimeUnit.SECONDS));
            }

            assertThat(results).hasSize(16);
            assertThat(results).extracting(StudentDetailResponse::id).doesNotHaveDuplicates().doesNotContainNull();
            assertThat(results).extracting(StudentDetailResponse::code).doesNotHaveDuplicates().doesNotContainNull();
            assertThat(repository.count()).isEqualTo(16);
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private StudentCreateRequest createRequest(Map<String, Object> extra) {
        Map<String, Object> payload = cadastroMinimo();
        payload.putAll(extra);
        return mapper.convertValue(payload, StudentCreateRequest.class);
    }

    private StudentUpdateRequest updateRequest(Map<String, Object> payload) {
        return mapper.convertValue(payload, StudentUpdateRequest.class);
    }

    private Map<String, Object> cadastroMinimo() {
        return new HashMap<>(Map.of("fullName", "Aluno ficticio", "birthDate", "2010-06-15", "status", "ATIVO"));
    }

    private Map<String, Object> cadastroCompleto() {
        Map<String, Object> payload = cadastroMinimo();
        payload.put("sex", "Prefiro nao informar");
        payload.put("cpf", "CPF-FICTICIO");
        payload.put("rg", "RG-FICTICIO");
        payload.put("address", "Endereco ficticio");
        payload.put("neighborhood", "Bairro ficticio");
        payload.put("city", "Cidade ficticia");
        payload.put("zipCode", "00000-000");
        payload.put("phone", "00000000000");
        payload.put("whatsapp", "00000000000");
        payload.put("email", "aluno@example.invalid");
        payload.put("guardianName", "Responsavel ficticio");
        payload.put("guardianCpf", "RESP-FICTICIO");
        payload.put("guardianPhone", "00000000000");
        payload.put("guardianWhatsapp", "00000000000");
        payload.put("relationship", "Outro");
        payload.put("modality", "Surf");
        payload.put("level", "Iniciante");
        payload.put("classGroup", "Turma ficticia");
        payload.put("startDate", "2026-01-10");
        payload.put("notes", "Observacao ficticia");
        payload.put("importantInformation", "Informacao ficticia");
        return payload;
    }
}
