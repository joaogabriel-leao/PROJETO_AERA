package br.com.aera.backend.patrimonio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.aera.backend.patrimonio.repository.AssetRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Testes de integracao do CRUD de patrimonio.
 *
 * <p>Usa MockMvc para simular requisicoes HTTP reais contra os endpoints,
 * passando por controller, service, repositorio e banco H2 em memoria.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetRepository repository;

    @BeforeEach
    void limparBase() {
        // Garante isolamento entre os testes (o H2 fica vivo durante a execucao).
        repository.deleteAll();
    }

    private String payloadCompleto(String name, String category, String status) {
        return """
                {
                  "name": "%s",
                  "category": "%s",
                  "quantity": 10,
                  "unit": "Unidade",
                  "location": "Deposito",
                  "condition": "NOVO",
                  "status": "%s",
                  "acquisitionValue": 199.90
                }
                """.formatted(name, category, status);
    }

    @Test
    void deveCriarItemGerandoCodigoAutomatico() throws Exception {
        mockMvc.perform(post("/patrimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCompleto("Bola de futebol", "Equipamento esportivo", "DISPONIVEL")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.code", is("PAT-0001")))
                .andExpect(jsonPath("$.name", is("Bola de futebol")))
                .andExpect(jsonPath("$.status", is("DISPONIVEL")));
    }

    @Test
    void deveGerarCodigosSequenciais() throws Exception {
        criarItem(payloadCompleto("Item A", "Movel", "DISPONIVEL"));
        MvcResult segundo = criarItem(payloadCompleto("Item B", "Movel", "DISPONIVEL"));

        JsonNode node = objectMapper.readTree(segundo.getResponse().getContentAsString());
        assertThat(node.get("code").asText()).isEqualTo("PAT-0002");
    }

    @Test
    void deveBuscarItemPorId() throws Exception {
        String id = criarEObterId(payloadCompleto("Cadeira", "Movel", "EM_USO"));

        mockMvc.perform(get("/patrimonio/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Cadeira")))
                .andExpect(jsonPath("$.status", is("EM_USO")));
    }

    @Test
    void deveRetornar404QuandoIdNaoExiste() throws Exception {
        mockMvc.perform(get("/patrimonio/{id}", "id-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(notNullValue())))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deveListarComPaginacao() throws Exception {
        criarItem(payloadCompleto("Item A", "Movel", "DISPONIVEL"));
        criarItem(payloadCompleto("Item B", "Eletronico", "EM_USO"));

        mockMvc.perform(get("/patrimonio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(hasSize(2))))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void deveFiltrarPorStatus() throws Exception {
        criarItem(payloadCompleto("Item A", "Movel", "DISPONIVEL"));
        criarItem(payloadCompleto("Item B", "Eletronico", "EM_USO"));

        mockMvc.perform(get("/patrimonio").param("status", "EM_USO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(hasSize(1))))
                .andExpect(jsonPath("$.content[0].name", is("Item B")));
    }

    @Test
    void deveFiltrarPorBuscaTextual() throws Exception {
        criarItem(payloadCompleto("Bola de volei", "Equipamento esportivo", "DISPONIVEL"));
        criarItem(payloadCompleto("Mesa de reuniao", "Movel", "EM_USO"));

        mockMvc.perform(get("/patrimonio").param("search", "bola"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(hasSize(1))))
                .andExpect(jsonPath("$.content[0].name", is("Bola de volei")));
    }

    @Test
    void deveAtualizarParcialmente() throws Exception {
        String id = criarEObterId(payloadCompleto("Projetor", "Eletronico", "DISPONIVEL"));

        mockMvc.perform(patch("/patrimonio/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "EM_MANUTENCAO", "notes": "Lampada queimada" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EM_MANUTENCAO")))
                .andExpect(jsonPath("$.notes", is("Lampada queimada")))
                // Campos nao enviados permanecem inalterados:
                .andExpect(jsonPath("$.name", is("Projetor")))
                .andExpect(jsonPath("$.category", is("Eletronico")));
    }

    @Test
    void deveRejeitarCriacaoSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/patrimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "category": "Movel", "quantity": 1, "unit": "Unidade",
                                  "location": "Sala", "condition": "NOVO", "status": "DISPONIVEL" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is(notNullValue())));
    }

    @Test
    void deveRejeitarCodigoDuplicado() throws Exception {
        String comCodigo = """
                {
                  "code": "PAT-9999",
                  "name": "Item X", "category": "Movel", "quantity": 1,
                  "unit": "Unidade", "location": "Sala",
                  "condition": "NOVO", "status": "DISPONIVEL"
                }
                """;
        criarItem(comCodigo);

        mockMvc.perform(post("/patrimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comCodigo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void deveExcluirItemExistente() throws Exception {
        String id = criarEObterId(payloadCompleto("Item para excluir", "Movel", "DISPONIVEL"));

        // Exclui -> 204 No Content
        mockMvc.perform(delete("/patrimonio/{id}", id))
                .andExpect(status().isNoContent());

        // Buscar depois -> 404 (confirma que sumiu)
        mockMvc.perform(get("/patrimonio/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoExcluirIdInexistente() throws Exception {
        mockMvc.perform(delete("/patrimonio/{id}", "id-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deveRetornarResumoDoPatrimonio() throws Exception {
        // 2 disponiveis, 1 em uso, 1 em manutencao, 1 danificado = total 5
        criarItem(payloadCompleto("A", "Movel", "DISPONIVEL"));
        criarItem(payloadCompleto("B", "Movel", "DISPONIVEL"));
        criarItem(payloadCompleto("C", "Movel", "EM_USO"));
        criarItem(payloadCompleto("D", "Movel", "EM_MANUTENCAO"));
        // Item danificado: condition=DANIFICADO (payloadCompleto usa NOVO, entao crio manualmente)
        mockMvc.perform(post("/patrimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "E", "category": "Movel", "quantity": 1, "unit": "Unidade",
                                  "location": "Sala", "condition": "DANIFICADO", "status": "BAIXADO" }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/patrimonio/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(5)))
                .andExpect(jsonPath("$.available", is(2)))
                .andExpect(jsonPath("$.inUse", is(1)))
                .andExpect(jsonPath("$.maintenance", is(1)))
                .andExpect(jsonPath("$.damaged", is(1)));
    }

    @Test
    void resumoDeveRetornarZerosQuandoVazio() throws Exception {
        mockMvc.perform(get("/patrimonio/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.available", is(0)));
    }

    @Test
    void deveLimitarOTamanhoMaximoDaPagina() throws Exception {
        // Mesmo pedindo size gigante, o backend deve limitar a 100 (protege contra DoS).
        mockMvc.perform(get("/patrimonio").param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(100)));
    }

    @Test
    void deveNormalizarPaginaNegativa() throws Exception {
        mockMvc.perform(get("/patrimonio").param("page", "-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(0)));
    }

    @Test
    void deveRejeitarNomeMuitoLongo() throws Exception {
        String nomeGigante = "x".repeat(300); // acima do limite de 255
        String payload = """
                { "name": "%s", "category": "Movel", "quantity": 1, "unit": "Unidade",
                  "location": "Sala", "condition": "NOVO", "status": "DISPONIVEL" }
                """.formatted(nomeGigante);

        mockMvc.perform(post("/patrimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is(notNullValue())));
    }

    // ---- helpers ----
    private MvcResult criarItem(String json) throws Exception {
        return mockMvc.perform(post("/patrimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String criarEObterId(String json) throws Exception {
        MvcResult result = criarItem(json);
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asText();
    }
}
