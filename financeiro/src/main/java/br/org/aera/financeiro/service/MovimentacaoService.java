package br.org.aera.financeiro.service;

import br.org.aera.financeiro.domain.*;
import br.org.aera.financeiro.repo.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimentacaoService {
    private final MovimentacaoRepository movimentos;
    private final ContaRepository contas;
    private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios;
    private final Validator validator;

    public MovimentacaoService(MovimentacaoRepository movimentos, ContaRepository contas,
            CategoriaRepository categorias, UsuarioRepository usuarios, Validator validator) {
        this.movimentos = movimentos;
        this.contas = contas;
        this.categorias = categorias;
        this.usuarios = usuarios;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','TESOUREIRO','OPERADOR','CONSULTA')")
    public List<Movimentacao> listar() {
        usuarioAtual(Set.of("ADMIN", "TESOUREIRO", "OPERADOR", "CONSULTA"));
        return movimentos.buscarTodas();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','TESOUREIRO','OPERADOR')")
    public Movimentacao registrar(RegistrarMovimentacao entrada) {
        var usuario = usuarioAtual(Set.of("ADMIN", "TESOUREIRO", "OPERADOR"));
        if (entrada == null) {
            throw new IllegalArgumentException("Informe os dados da movimentação.");
        }
        var violacoes = validator.validate(entrada);
        if (!violacoes.isEmpty()) {
            throw new ConstraintViolationException(violacoes);
        }
        var conta = contas.findById(entrada.contaId()).filter(Conta::isAtiva)
                .orElseThrow(() -> new MovimentacaoInvalidaException("contaId", "Selecione uma conta ativa."));
        var categoria = categorias.findById(entrada.categoriaId()).filter(Categoria::isAtiva)
                .orElseThrow(() -> new MovimentacaoInvalidaException("categoriaId", "Selecione uma categoria ativa."));
        return movimentos.save(new Movimentacao(conta, categoria, usuario, entrada.tipo(), entrada.valor(),
                entrada.dataMovimentacao(), entrada.descricao().trim()));
    }

    // Reconsulta o perfil persistido: sessões antigas não mantêm permissão revogada.
    private Usuario usuarioAtual(Set<String> perfisPermitidos) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Autenticação necessária.");
        }
        return usuarios.findByUsernameAndAtivoTrue(authentication.getName())
                .filter(u -> perfisPermitidos.contains(u.getPerfil()))
                .orElseThrow(() -> new AccessDeniedException("Usuário sem permissão para esta operação."));
    }
}
