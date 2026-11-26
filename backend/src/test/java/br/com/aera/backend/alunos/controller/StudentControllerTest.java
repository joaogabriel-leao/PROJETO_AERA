package br.com.aera.backend.alunos.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.aera.backend.alunos.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** Exercita o context-path /api e o fluxo HTTP ate o banco H2, sem mocks de service. */
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private StudentRepository repository;

    @BeforeEach
    @AfterEach
    void limparAlunos() {
        repository.deleteAll();
    }

    @Test
    void deveCadastrarSemDocumentosCom201LocationEIdentificacaoGerada() throws Exception {
        var result = mockMvc.perform(post("/api/alunos").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(payload().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.code", matchesPattern("ALU-[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.birthDate").value("2010-06-15"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.createdAt").isString())
                .andReturn();

        JsonNode created = mapper.readTree(result.getResponse().getContentAsString());
        UUID.fromString(created.get("id").asText());
        assertThat(result.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/alunos/" + created.get("id").asText());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void naoDeveAceitarCamposTecnicosEnviadosPeloCliente() throws Exception {
        String suppliedId = "00000000-0000-0000-0000-000000000001";
        ObjectNode input = payload().put("id", suppliedId).put("code", "CODIGO-CLIENTE")
                .put("createdAt", "2000-01-01T00:00:00Z");
        JsonNode created = criar(input);

        assertThat(created.get("id").asText()).isNotEqualTo(suppliedId);
        assertThat(created.get("code").asText()).isNotEqualTo("CODIGO-CLIENTE");
        assertThat(created.get("createdAt").asText()).doesNotStartWith("2000-01-01");
    }

    @Test
    void deveConsultarDetalhePorUuidIncluindoDadosCadastrais() throws Exception {
        JsonNode created = criar(payload().put("cpf", "CPF-FICTICIO").put("rg", "RG-FICTICIO")
                .put("guardianName", "Responsavel ficticio").put("sex", "Prefiro nao informar"));

        mockMvc.perform(get("/api/alunos/{id}", created.get("id").asText()).contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.get("id").asText()))
                .andExpect(jsonPath("$.fullName").value("Aluno ficticio"))
                .andExpect(jsonPath("$.cpf").value("CPF-FICTICIO"))
                .andExpect(jsonPath("$.rg").value("RG-FICTICIO"))
                .andExpect(jsonPath("$.sex").value("Prefiro nao informar"))
                .andExpect(jsonPath("$.guardianName").value("Responsavel ficticio"));
    }

    @Test
    void deveListarComPaginacaoSemExporDocumentosOuInformacoesSensiveis() throws Exception {
        criar(payload().put("cpf", "CPF-FICTICIO").put("rg", "RG-FICTICIO")
                .put("guardianCpf", "RESP-FICTICIO").put("notes", "Observacao ficticia")
                .put("importantInformation", "Informacao ficticia").put("email", "aluno@example.invalid"));
        criar(payload());

        mockMvc.perform(get("/api/alunos").contextPath("/api").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].cpf").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].rg").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].guardianCpf").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].address").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].notes").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].importantInformation").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].email").doesNotHaveJsonPath());
        mockMvc.perform(get("/api/alunos").contextPath("/api").param("page", "1").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void deveRetornarListaVaziaComPaginacaoPadrao() throws Exception {
        mockMvc.perform(get("/api/alunos").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"nome", "codigo", "responsavel"})
    void deveBuscarPorNomeCodigoOuResponsavel(String field) throws Exception {
        JsonNode created = criar(payload().put("fullName", "Aluno Azul Ficticio")
                .put("guardianName", "Responsavel Verde Ficticio"));
        criar(payload().put("fullName", "Outro aluno ficticio"));
        String search = switch (field) {
            case "nome" -> "  AZUL  ";
            case "codigo" -> created.get("code").asText().toLowerCase();
            default -> "VERDE";
        };

        mockMvc.perform(get("/api/alunos").contextPath("/api").param("search", search))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(created.get("id").asText()));
    }

    @Test
    void deveCombinarBuscaComSituacaoEModalidade() throws Exception {
        JsonNode created = criar(payload().put("modality", "Surf"));
        criar(payload().put("modality", "Surf").put("status", "INATIVO"));
        criar(payload().put("modality", "Bodyboard"));

        mockMvc.perform(get("/api/alunos").contextPath("/api")
                        .param("search", "aluno").param("status", "ATIVO").param("modality", "surf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(created.get("id").asText()));
    }

    @Test
    void deveNormalizarPaginaNegativaELimitarTamanho() throws Exception {
        mockMvc.perform(get("/api/alunos").contextPath("/api").param("page", "-5").param("size", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void deveAtualizarParcialmentePreservandoCamposNaoEnviadosETecnicos() throws Exception {
        JsonNode created = criar(payload().put("cpf", "CPF-FICTICIO").put("phone", "00000000000"));
        String id = created.get("id").asText();
        JsonNode before = mapper.readTree(mockMvc.perform(get("/api/alunos/{id}", id).contextPath("/api"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        mockMvc.perform(patch("/api/alunos/{id}", id).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Nome ficticio atualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nome ficticio atualizado"))
                .andExpect(jsonPath("$.phone").value("00000000000"))
                .andExpect(jsonPath("$.cpf").value("CPF-FICTICIO"))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.code").value(before.get("code").asText()))
                .andExpect(jsonPath("$.createdAt").value(before.get("createdAt").asText()));
    }

    @Test
    void deveLimparCamposOpcionaisExplicitamenteEPreservarNullOmitido() throws Exception {
        JsonNode created = criar(payload().put("phone", "00000000000").put("cpf", "CPF-FICTICIO")
                .put("startDate", "2026-01-10"));
        String id = created.get("id").asText();

        mockMvc.perform(patch("/api/alunos/{id}", id).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clearFields\":[\"phone\",\"startDate\"],\"cpf\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").isEmpty())
                .andExpect(jsonPath("$.startDate").isEmpty())
                .andExpect(jsonPath("$.cpf").value("CPF-FICTICIO"));
    }

    @Test
    void deveAlterarSituacaoNosDoisSentidosComContratoDoFrontend() throws Exception {
        JsonNode created = criar(payload());
        String id = created.get("id").asText();
        for (String target : new String[]{"INATIVO", "ATIVO"}) {
            mockMvc.perform(patch("/api/alunos/{id}/situacao", id).contextPath("/api")
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + target + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(target))
                    .andExpect(jsonPath("$.code").value(created.get("code").asText()))
                    .andExpect(jsonPath("$.fullName").value("Aluno ficticio"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"fullName", "birthDate", "status"})
    void deveRetornar400QuandoFaltaCampoObrigatorio(String field) throws Exception {
        ObjectNode input = payload();
        input.remove(field);

        mockMvc.perform(post("/api/alunos").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(input.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.timestamp").isString());
        assertThat(repository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"fullName", "birthDate", "email"})
    void deveRetornar400ParaDadosInvalidosNoCadastroENaAtualizacao(String field) throws Exception {
        JsonNode created = criar(payload());
        String value = switch (field) {
            case "fullName" -> "   ";
            case "birthDate" -> LocalDate.now().plusDays(1).toString();
            default -> "sem-arroba";
        };
        ObjectNode invalid = payload().put(field, value);

        mockMvc.perform(post("/api/alunos").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(invalid.toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(patch("/api/alunos/{id}", created.get("id").asText()).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(invalid.toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        assertThat(repository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{", "[]", "{\"status\":\"INVALIDO\"}", "{\"birthDate\":\"31/12/2010\"}"})
    void deveRetornar400ParaCorpoAusenteMalformadoOuTiposInvalidos(String content) throws Exception {
        mockMvc.perform(post("/api/alunos").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.trace").doesNotHaveJsonPath());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"status\":null}", "{\"status\":\"INVALIDO\"}"})
    void deveValidarCorpoDaAlteracaoDeSituacao(String content) throws Exception {
        JsonNode created = criar(payload());

        mockMvc.perform(patch("/api/alunos/{id}/situacao", created.get("id").asText()).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"clearFields\":[\"id\"]}", "{\"clearFields\":[\"desconhecido\"]}",
            "{\"clearFields\":[\"\"]}", "{\"notes\":\"Texto ficticio\",\"clearFields\":[\"notes\"]}"})
    void deveRetornar400ParaLimpezaInvalidaSemPersistirAlteracoes(String content) throws Exception {
        JsonNode created = criar(payload().put("notes", "Texto ficticio original"));
        String id = created.get("id").asText();

        mockMvc.perform(patch("/api/alunos/{id}", id).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(get("/api/alunos/{id}", id).contextPath("/api"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.notes").value("Texto ficticio original"));
    }

    @ParameterizedTest
    @CsvSource({"GET,''", "PATCH,''", "PATCH,/situacao"})
    void deveRetornar404ParaAlunoInexistente(String method, String suffix) throws Exception {
        String path = "/api/alunos/" + UUID.randomUUID() + suffix;
        String content = suffix.isEmpty() ? "{\"fullName\":\"Aluno ficticio\"}" : "{\"status\":\"INATIVO\"}";

        mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.valueOf(method), path).contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isString());
    }

    @ParameterizedTest
    @CsvSource({"GET,''", "PATCH,''", "PATCH,/situacao"})
    void deveRetornar400ParaUuidMalformado(String method, String suffix) throws Exception {
        String content = suffix.isEmpty() ? "{\"fullName\":\"Aluno ficticio\"}" : "{\"status\":\"INATIVO\"}";

        mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.valueOf(method), "/api/alunos/uuid-invalido" + suffix)
                        .contextPath("/api").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @CsvSource({"page,abc", "size,abc", "status,INVALIDO"})
    void deveRetornar400ParaParametroDeFiltroOuPaginaInvalido(String name, String value) throws Exception {
        mockMvc.perform(get("/api/alunos").contextPath("/api").param(name, value))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    private JsonNode criar(ObjectNode input) throws Exception {
        var result = mockMvc.perform(post("/api/alunos").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON).content(input.toString()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("http://localhost/api/alunos/[0-9a-f-]{36}")))
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    private ObjectNode payload() {
        return mapper.createObjectNode().put("fullName", "Aluno ficticio")
                .put("birthDate", "2010-06-15").put("status", "ATIVO");
    }
}
