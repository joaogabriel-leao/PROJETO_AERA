package br.org.aera.financeiro.web;

import br.org.aera.financeiro.domain.*;
import br.org.aera.financeiro.repo.*;
import br.org.aera.financeiro.security.SecurityConfig;
import br.org.aera.financeiro.service.MovimentacaoService;
import br.org.aera.financeiro.service.RegistrarMovimentacao;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/** Contexto MVC mínimo: sem datasource, JPA, Flyway ou AdminBootstrap. */
@SpringJUnitWebConfig(SegurancaMovimentacaoTest.Config.class)
class SegurancaMovimentacaoTest {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, MovimentacaoService.class, HomeController.class, MovimentacaoController.class, ConfigController.class})
    static class Config {
        @Bean UsuarioRepository usuarioRepository() { return mock(UsuarioRepository.class); }
        @Bean ContaRepository contas() { return mock(ContaRepository.class); }
        @Bean CategoriaRepository categorias() { return mock(CategoriaRepository.class); }
        @Bean MovimentacaoRepository movimentos() { return mock(MovimentacaoRepository.class); }
        @Bean InternalResourceViewResolver views() {
            return new InternalResourceViewResolver("/test-views/", ".html");
        }
    }

    @org.springframework.beans.factory.annotation.Autowired WebApplicationContext context;
    @org.springframework.beans.factory.annotation.Autowired UsuarioRepository usuarios;
    @org.springframework.beans.factory.annotation.Autowired ContaRepository contas;
    @org.springframework.beans.factory.annotation.Autowired CategoriaRepository categorias;
    @org.springframework.beans.factory.annotation.Autowired MovimentacaoRepository movimentos;
    @org.springframework.beans.factory.annotation.Autowired PasswordEncoder encoder;
    @org.springframework.beans.factory.annotation.Autowired MovimentacaoService service;
    MockMvc mvc;
    Conta conta;
    Categoria categoria;

    @BeforeEach void preparar() {
        reset(usuarios, contas, categorias, movimentos);
        conta = new Conta("Conta fictícia");
        categoria = new Categoria("Categoria fictícia");
        when(contas.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(categorias.findById(categoria.getId())).thenReturn(Optional.of(categoria));
        when(usuarios.findByUsernameAndAtivoTrue("operador"))
            .thenReturn(Optional.of(new Usuario("operador", encoder.encode("senha-teste"), "OPERADOR")));
        when(usuarios.findByUsernameAndAtivoTrue("leitor"))
            .thenReturn(Optional.of(new Usuario("leitor", "nao-utilizada", "CONSULTA")));
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void loginPublico() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
    }
    @Test void anonimoPrecisaAutenticar() throws Exception {
        mvc.perform(get("/movimentacoes")).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }
    @Test void senhaCorretaAutentica() throws Exception {
        mvc.perform(formLogin().user("operador").password("senha-teste"))
            .andExpect(authenticated().withUsername("operador")).andExpect(redirectedUrl("/"));
    }
    @Test void senhaIncorretaRecusada() throws Exception {
        mvc.perform(formLogin().user("operador").password("errada"))
            .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }
    @Test void usuarioAusenteOuInativoRecusado() throws Exception {
        mvc.perform(formLogin().user("inativo").password("senha-teste"))
            .andExpect(unauthenticated());
    }
    @Test void configuracaoExigeAdmin() throws Exception {
        mvc.perform(get("/config").with(user("operador").roles("OPERADOR")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/config").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk());
    }
    @Test void leitorNaoPodeLancar() throws Exception {
        mvc.perform(get("/movimentacoes").with(user("leitor").roles("CONSULTA")))
            .andExpect(status().isOk());
        clearInvocations(movimentos);
        mvc.perform(get("/movimentacoes/nova").with(user("leitor").roles("CONSULTA")))
            .andExpect(status().isForbidden());
        mvc.perform(envio().with(user("leitor").roles("CONSULTA")).with(csrf()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(movimentos);
    }
    @ParameterizedTest @ValueSource(strings={"ADMIN", "TESOUREIRO", "OPERADOR"})
    void perfisPermitidosPodemLancar(String perfil) throws Exception {
        mvc.perform(envio().with(user("operador").roles(perfil)).with(csrf()))
            .andExpect(redirectedUrl("/movimentacoes"));
        verify(movimentos).save(argThat(m -> m.getValor().toPlainString().equals("12.34")
            && m.getDescricao().equals("Teste") && m.getConta() == conta
            && m.getCategoria() == categoria && m.getUsuario().getUsername().equals("operador")));
    }
    @Test void postExigeCsrf() throws Exception {
        mvc.perform(envio().with(user("operador").roles("OPERADOR")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(movimentos);
    }
    @ParameterizedTest @ValueSource(strings={"0", "-1", "1.001", "10000000000000", "abc"})
    void valorInvalidoNaoPersiste(String valor) throws Exception {
        mvc.perform(envio().with(r -> { r.setParameter("valor", valor); return r; }).with(user("operador").roles("OPERADOR")).with(csrf()))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "valor"));
        verifyNoInteractions(movimentos);
    }
    @Test void camposObrigatorios() throws Exception {
        mvc.perform(post("/movimentacoes/salvar").param("dataMovimentacao", "")
            .with(user("operador").roles("OPERADOR")).with(csrf()))
            .andExpect(model().attributeHasFieldErrors("form", "contaId", "categoriaId", "tipo", "valor", "descricao", "dataMovimentacao"));
        verifyNoInteractions(movimentos);
    }
    @Test void descricaoLongaRecusada() throws Exception {
        mvc.perform(envio().with(r -> { r.setParameter("descricao", "x".repeat(501)); return r; }).with(user("operador").roles("OPERADOR")).with(csrf()))
            .andExpect(model().attributeHasFieldErrors("form", "descricao"));
        verifyNoInteractions(movimentos);
    }
    @Test void contaInexistenteRecusada() throws Exception {
        when(contas.findById(conta.getId())).thenReturn(Optional.empty());
        mvc.perform(envio().with(user("operador").roles("OPERADOR")).with(csrf()))
            .andExpect(model().attributeHasErrors("form")).andExpect(view().name("nova-movimentacao"));
        verifyNoInteractions(movimentos);
    }
    @Test void categoriaInativaRecusada() throws Exception {
        Categoria inativa = mock(Categoria.class);
        when(inativa.isAtiva()).thenReturn(false);
        when(categorias.findById(categoria.getId())).thenReturn(Optional.of(inativa));
        mvc.perform(envio().with(user("operador").roles("OPERADOR")).with(csrf()))
            .andExpect(model().attributeHasErrors("form"));
        verifyNoInteractions(movimentos);
    }
    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles="CONSULTA")
    void servicoBloqueiaEscritaMesmoSemController() {
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.security.access.AccessDeniedException.class,
            () -> service.registrar(new RegistrarMovimentacao(conta.getId(), categoria.getId(), TipoMovimentacao.ENTRADA,
                java.math.BigDecimal.ONE, java.time.LocalDate.now(), "Teste")));
        verifyNoInteractions(movimentos);
    }
    @Test void usuarioDesativadoDuranteSessaoRecebe403() throws Exception {
        when(usuarios.findByUsernameAndAtivoTrue("operador")).thenReturn(Optional.empty());
        mvc.perform(envio().with(user("operador").roles("OPERADOR")).with(csrf()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(movimentos);
    }
    @Test void perfilRevogadoDuranteSessaoRecebe403() throws Exception {
        when(usuarios.findByUsernameAndAtivoTrue("operador"))
            .thenReturn(Optional.of(new Usuario("operador", "hash", "CONSULTA")));
        mvc.perform(envio().with(user("operador").roles("OPERADOR")).with(csrf()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(movimentos);
    }
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder envio() {
        return post("/movimentacoes/salvar")
            .param("contaId", conta.getId().toString()).param("categoriaId", categoria.getId().toString())
            .param("tipo", "ENTRADA").param("valor", "12.34")
            .param("dataMovimentacao", "2026-11-01").param("descricao", " Teste ");
    }
}
