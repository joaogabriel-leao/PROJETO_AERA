package br.org.aera.financeiro.service;

import br.org.aera.financeiro.domain.*;
import br.org.aera.financeiro.repo.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/** Executar somente pelo script que cria um cluster exclusivo de teste. */
@EnabledIfSystemProperty(named = "aera.postgres.integration", matches = "true")
@SpringBootTest(properties = {"ADMIN_INITIAL_PASSWORD=senha-apenas-para-testes-123", "ADMIN_INITIAL_USERNAME=admin-teste"})
@Transactional
@WithMockUser(username = "operador-teste", roles = "OPERADOR")
class PostgreSqlMovimentacaoTest {
    @DynamicPropertySource
    static void bancoIsolado(DynamicPropertyRegistry properties) {
        String port = System.getProperty("aera.postgres.port", "");
        if (!port.matches("[0-9]{1,5}") || Integer.parseInt(port) < 1024 || Integer.parseInt(port) > 65535) {
            throw new IllegalStateException("Execute scripts/testar-postgres.ps1 para criar o banco isolado.");
        }
        // Não usa DB_URL, DB_USER ou DB_PASSWORD do ambiente.
        String url = "jdbc:postgresql://127.0.0.1:" + port + "/aera_fase2_test";
        properties.add("spring.datasource.url", () -> url);
        properties.add("spring.datasource.username", () -> "aera_test");
        properties.add("spring.datasource.password", () -> "");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        properties.add("spring.flyway.enabled", () -> "true");
        properties.add("spring.flyway.url", () -> url);
        properties.add("spring.flyway.user", () -> "aera_test");
        properties.add("spring.flyway.password", () -> "");
    }

    @Autowired MovimentacaoService service;
    @Autowired MovimentacaoRepository movimentos;
    @Autowired ContaRepository contas;
    @Autowired CategoriaRepository categorias;
    @Autowired UsuarioRepository usuarios;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;
    Conta conta;
    Categoria categoria;

    @BeforeEach void preparar() {
        conta = contas.saveAndFlush(new Conta("Caixa isolado"));
        categoria = categorias.saveAndFlush(new Categoria("Categoria isolada"));
        usuarios.saveAndFlush(new Usuario("operador-teste", "hash-ficticio", "OPERADOR"));
    }

    @Test void migrationOriginalAplicadaEValidada() {
        assertDoesNotThrow(() -> flyway.validate());
        assertEquals("1", flyway.info().current().getVersion().toString());
        assertTrue(jdbc.queryForObject("select version()", String.class).startsWith("PostgreSQL 17"));
    }

    @ParameterizedTest @EnumSource(TipoMovimentacao.class)
    void persisteERelacionaEntradaESaida(TipoMovimentacao tipo) {
        var salvo = service.registrar(entrada(tipo));
        movimentos.flush();
        entityManager.clear();
        var recarregado = service.listar().stream().filter(m -> m.getId().equals(salvo.getId())).findFirst().orElseThrow();
        assertAll(() -> assertEquals(tipo, recarregado.getTipo()),
            () -> assertEquals(new BigDecimal("12.34"), recarregado.getValor()),
            () -> assertEquals("Teste PostgreSQL", recarregado.getDescricao()),
            () -> assertEquals(conta.getId(), recarregado.getConta().getId()),
            () -> assertEquals(categoria.getId(), recarregado.getCategoria().getId()),
            () -> assertEquals("operador-teste", recarregado.getUsuario().getUsername()),
            () -> assertNotNull(recarregado.getCriadoEm()));
    }

    @Test void contaDesativadaNoBancoNaoPermiteLancamento() {
        jdbc.update("update contas set ativa = false where id = ?", conta.getId());
        entityManager.clear();
        assertThrows(MovimentacaoInvalidaException.class, () -> service.registrar(entrada(TipoMovimentacao.SAIDA)));
        assertEquals(0, movimentos.count());
    }

    @Test void usuarioDesativadoNoBancoNaoPermiteLancamento() {
        jdbc.update("update usuarios set ativo = false where username = 'operador-teste'");
        entityManager.clear();
        assertThrows(AccessDeniedException.class, () -> service.registrar(entrada(TipoMovimentacao.ENTRADA)));
        assertEquals(0, movimentos.count());
    }

    @Test void bancoRejeitaValorNegativoMesmoForaDoServico() {
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> jdbc.update(
            "insert into movimentacoes (id,conta_id,categoria_id,usuario_id,tipo,valor,data_movimentacao,descricao) "
                + "select ?,?,?,id,'SAIDA',-1,current_date,'Inválido' from usuarios where username='operador-teste'",
            java.util.UUID.randomUUID(), conta.getId(), categoria.getId()));
        // PostgreSQL aborta a transação; o framework faz rollback ao terminar o teste.
    }

    RegistrarMovimentacao entrada(TipoMovimentacao tipo) {
        return new RegistrarMovimentacao(conta.getId(), categoria.getId(), tipo,
            new BigDecimal("12.34"), LocalDate.of(2026, 11, 1), " Teste PostgreSQL ");
    }
}
