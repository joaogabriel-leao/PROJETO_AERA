package br.com.aera.backend.patrimonio.controller;

import br.com.aera.backend.patrimonio.domain.AssetCondition;
import br.com.aera.backend.patrimonio.domain.AssetStatus;
import br.com.aera.backend.patrimonio.dto.AssetCreateRequest;
import br.com.aera.backend.patrimonio.dto.AssetResponse;
import br.com.aera.backend.patrimonio.dto.AssetSummaryResponse;
import br.com.aera.backend.patrimonio.dto.AssetUpdateRequest;
import br.com.aera.backend.patrimonio.dto.PageResponse;
import br.com.aera.backend.patrimonio.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST do patrimonio/materiais.
 *
 * <p>Com o context-path {@code /api} (ver application.properties), a URL base
 * efetiva e {@code http://localhost:8080/api/patrimonio}, exatamente o que o
 * frontend chama em {@code assetService.ts}.</p>
 */
@RestController
@RequestMapping("/patrimonio")
public class AssetController {

    private final AssetService service;

    public AssetController(AssetService service) {
        this.service = service;
    }

    /**
     * Lista itens com filtros opcionais e paginacao.
     *
     * <p>Exemplo:
     * {@code GET /api/patrimonio?search=bola&status=DISPONIVEL&page=0&size=20}</p>
     *
     * @param search    texto buscado em codigo ou nome (opcional)
     * @param category  filtro por categoria (opcional)
     * @param status    filtro por situacao (opcional)
     * @param condition filtro por estado de conservacao (opcional)
     * @param location  filtro por localizacao (opcional)
     * @param page      numero da pagina, base 0 (padrao 0)
     * @param size      itens por pagina (padrao 20)
     */
    @GetMapping
    public PageResponse<AssetResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) AssetCondition condition,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return service.list(search, category, status, condition, location, page, size);
    }

    /** Busca um item pelo id. Retorna 404 se nao existir. */
    @GetMapping("/{id}")
    public AssetResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    /**
     * Resumo do patrimonio para a tela de Visao Geral (Dashboard):
     * total e contagens por situacao/estado.
     * {@code GET /api/patrimonio/resumo}
     */
    @GetMapping("/resumo")
    public AssetSummaryResponse summary() {
        return service.summary();
    }

    /** Cria um novo item. Retorna 201 Created com o item criado. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse create(@Valid @RequestBody AssetCreateRequest request) {
        return service.create(request);
    }

    /** Atualiza parcialmente um item existente. Retorna 404 se nao existir. */
    @PatchMapping("/{id}")
    public AssetResponse update(
            @PathVariable String id,
            @Valid @RequestBody AssetUpdateRequest request) {
        return service.update(id, request);
    }

    /** Remove um item. Retorna 204 No Content. 404 se nao existir. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
