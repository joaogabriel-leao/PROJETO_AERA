package br.org.aera.financeiro.web;

import br.org.aera.financeiro.domain.*;
import br.org.aera.financeiro.repo.*;
import jakarta.validation.Valid;
import br.org.aera.financeiro.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/movimentacoes")
public class MovimentacaoController {
    private final MovimentacaoService movimentos;
    private final ContaRepository contas;
    private final CategoriaRepository categorias;

    public MovimentacaoController(MovimentacaoService movimentos, ContaRepository contas,
            CategoriaRepository categorias) {
        this.movimentos = movimentos;
        this.contas = contas;
        this.categorias = categorias;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("movimentacoes", movimentos.listar());
        return "movimentacoes";
    }

    private void prepararFormulario(Model model) {
        model.addAttribute("contas", contas.findAllByAtivaTrueOrderByNomeAsc());
        model.addAttribute("categorias", categorias.findAllByAtivaTrueOrderByNomeAsc());
        model.addAttribute("tipos", TipoMovimentacao.values());
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("form", new MovimentacaoForm());
        prepararFormulario(model);
        return "nova-movimentacao";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("form") MovimentacaoForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            prepararFormulario(model);
            return "nova-movimentacao";
        }
        try {
            movimentos.registrar(new RegistrarMovimentacao(form.getContaId(), form.getCategoriaId(),
                    form.getTipo(), form.getValor(), form.getDataMovimentacao(), form.getDescricao()));
        } catch (MovimentacaoInvalidaException ex) {
            result.rejectValue(ex.getCampo(), "invalid", ex.getMessage());
            prepararFormulario(model);
            return "nova-movimentacao";
        }
        flash.addFlashAttribute("sucesso", "Movimentação registrada.");
        return "redirect:/movimentacoes";
    }
}
