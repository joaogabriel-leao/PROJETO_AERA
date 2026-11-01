package br.org.aera.financeiro.service;

import br.org.aera.financeiro.domain.*;
import br.org.aera.financeiro.repo.*;
import jakarta.validation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovimentacaoServiceTest {
    static ValidatorFactory factory;
    MovimentacaoRepository movimentos;
    ContaRepository contas;
    CategoriaRepository categorias;
    UsuarioRepository usuarios;
    MovimentacaoService service;
    Conta conta;
    Categoria categoria;
    Usuario usuario;

    @BeforeAll static void validator() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void fecharValidator() { factory.close(); }
    @AfterEach void limparAutenticacao() { SecurityContextHolder.clearContext(); }

    @BeforeEach void preparar() {
        movimentos = mock(MovimentacaoRepository.class);
        contas = mock(ContaRepository.class);
        categorias = mock(CategoriaRepository.class);
        usuarios = mock(UsuarioRepository.class);
        service = new MovimentacaoService(movimentos, contas, categorias, usuarios, factory.getValidator());
        conta = new Conta("Caixa de teste");
        categoria = new Categoria("Doações de teste");
        usuario = new Usuario("operador", "hash-ficticio", "OPERADOR");
        when(contas.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(categorias.findById(categoria.getId())).thenReturn(Optional.of(categoria));
        when(usuarios.findByUsernameAndAtivoTrue("operador")).thenReturn(Optional.of(usuario));
        when(movimentos.save(any(Movimentacao.class))).thenAnswer(i -> i.getArgument(0));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("operador", "", java.util.List.of()));
    }

    @ParameterizedTest @EnumSource(TipoMovimentacao.class)
    void registraEntradaESaidaComValorPositivo(TipoMovimentacao tipo) {
        var resultado = service.registrar(entrada(tipo, new BigDecimal("123.45"), "  Teste  "));
        assertAll(() -> assertEquals(tipo, resultado.getTipo()),
                () -> assertEquals(new BigDecimal("123.45"), resultado.getValor()),
                () -> assertEquals("Teste", resultado.getDescricao()),
                () -> assertSame(usuario, resultado.getUsuario()),
                () -> assertSame(conta, resultado.getConta()),
                () -> assertSame(categoria, resultado.getCategoria()),
                () -> assertNotNull(resultado.getId()),
                () -> assertNotNull(resultado.getCriadoEm()));
        verify(movimentos).save(resultado);
    }

    @ParameterizedTest @EnumSource(TipoMovimentacao.class)
    void aceitaLimitesMonetarios(TipoMovimentacao tipo) {
        service.registrar(entrada(tipo, new BigDecimal("0.01"), "Teste"));
        service.registrar(entrada(tipo, new BigDecimal("9999999999999.99"), "Teste"));
        verify(movimentos, times(2)).save(any());
    }

    @ParameterizedTest @CsvSource({"ENTRADA,0", "SAIDA,0", "ENTRADA,-1", "SAIDA,-1", "ENTRADA,1.001", "SAIDA,1.001", "ENTRADA,10000000000000", "SAIDA,10000000000000"})
    void rejeitaValorInvalido(TipoMovimentacao tipo, BigDecimal valor) {
        assertThrows(ConstraintViolationException.class, () -> service.registrar(entrada(tipo, valor, "Teste")));
        verifyNoInteractions(contas, categorias, movimentos);
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"   "})
    void rejeitaDescricaoVazia(String descricao) {
        assertThrows(ConstraintViolationException.class,
                () -> service.registrar(entrada(TipoMovimentacao.ENTRADA, BigDecimal.ONE, descricao)));
        verifyNoInteractions(movimentos);
    }

    @Test void rejeitaDescricaoLonga() {
        assertThrows(ConstraintViolationException.class,
                () -> service.registrar(entrada(TipoMovimentacao.ENTRADA, BigDecimal.ONE, "x".repeat(501))));
        verifyNoInteractions(movimentos);
    }

    @Test void exigeTodosOsCampos() {
        var ex = assertThrows(ConstraintViolationException.class,
                () -> service.registrar(new RegistrarMovimentacao(null, null, null, null, null, null)));
        assertEquals(6, ex.getConstraintViolations().size());
        verifyNoInteractions(movimentos);
    }

    @Test void rejeitaComandoNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(null));
        verifyNoInteractions(movimentos);
    }

    @Test void rejeitaContaInativa() {
        Conta inativa = mock(Conta.class);
        when(contas.findById(conta.getId())).thenReturn(Optional.of(inativa));
        var ex = assertThrows(MovimentacaoInvalidaException.class, () -> service.registrar(valida()));
        assertEquals("contaId", ex.getCampo());
        verifyNoInteractions(movimentos);
    }

    @Test void rejeitaCategoriaInexistente() {
        when(categorias.findById(categoria.getId())).thenReturn(Optional.empty());
        var ex = assertThrows(MovimentacaoInvalidaException.class, () -> service.registrar(valida()));
        assertEquals("categoriaId", ex.getCampo());
        verifyNoInteractions(movimentos);
    }

    @Test void rejeitaUsuarioDesativadoAposLogin() {
        when(usuarios.findByUsernameAndAtivoTrue("operador")).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> service.registrar(valida()));
        verifyNoInteractions(contas, categorias, movimentos);
    }

    @Test void rejeitaPerfilRevogadoAposLogin() {
        when(usuarios.findByUsernameAndAtivoTrue("operador"))
            .thenReturn(Optional.of(new Usuario("operador", "hash", "CONSULTA")));
        assertThrows(AccessDeniedException.class, () -> service.registrar(valida()));
        verifyNoInteractions(movimentos);
    }

    @Test void rejeitaSemAutenticacao() {
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> service.registrar(valida()));
        verifyNoInteractions(usuarios, movimentos);
    }

    @Test void propagaFalhaDePersistenciaSemInformarSucesso() {
        when(movimentos.save(any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("teste"));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> service.registrar(valida()));
    }

    RegistrarMovimentacao valida() { return entrada(TipoMovimentacao.ENTRADA, BigDecimal.ONE, "Teste"); }
    RegistrarMovimentacao entrada(TipoMovimentacao tipo, BigDecimal valor, String descricao) {
        return new RegistrarMovimentacao(conta.getId(), categoria.getId(), tipo, valor, LocalDate.of(2026, 11, 1), descricao);
    }
}
