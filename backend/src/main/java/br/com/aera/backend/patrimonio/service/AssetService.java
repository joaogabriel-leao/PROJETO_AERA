package br.com.aera.backend.patrimonio.service;

import br.com.aera.backend.common.exception.ResourceNotFoundException;
import br.com.aera.backend.patrimonio.domain.Asset;
import br.com.aera.backend.patrimonio.domain.AssetCondition;
import br.com.aera.backend.patrimonio.domain.AssetStatus;
import br.com.aera.backend.patrimonio.dto.AssetCreateRequest;
import br.com.aera.backend.patrimonio.dto.AssetResponse;
import br.com.aera.backend.patrimonio.dto.AssetSummaryResponse;
import br.com.aera.backend.patrimonio.dto.AssetUpdateRequest;
import br.com.aera.backend.patrimonio.dto.PageResponse;
import br.com.aera.backend.patrimonio.repository.AssetRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio do modulo de patrimonio/materiais.
 *
 * <p>Fica entre o controller (HTTP) e o repositorio (banco). Aqui moram:
 * geracao de id e codigo, validacoes de negocio, montagem de filtros e
 * conversao entre entidade e DTOs.</p>
 */
@Service
public class AssetService {

    private static final String CODE_PREFIX = "PAT-";

    /** Teto para o tamanho da pagina, protege contra requisicoes abusivas. */
    private static final int MAX_PAGE_SIZE = 100;

    private final AssetRepository repository;

    public AssetService(AssetRepository repository) {
        this.repository = repository;
    }

    /**
     * Cria um novo item.
     *
     * <p>Gera um id (UUID). Se o cliente nao informar o codigo, gera um
     * sequencial (PAT-0001, PAT-0002...). Se informar, valida que nao esta
     * duplicado.</p>
     */
    @Transactional
    public AssetResponse create(AssetCreateRequest request) {
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID().toString());

        String code = normalize(request.code());
        if (code == null) {
            code = generateNextCode();
        } else if (repository.existsByCode(code)) {
            throw new IllegalArgumentException("Ja existe um item com o codigo '" + code + "'.");
        }
        asset.setCode(code);

        asset.setName(request.name());
        asset.setCategory(request.category());
        asset.setQuantity(request.quantity());
        asset.setUnit(request.unit());
        asset.setLocation(request.location());
        asset.setCondition(request.condition());
        asset.setStatus(request.status());
        asset.setAcquisitionDate(request.acquisitionDate());
        asset.setAcquisitionValue(request.acquisitionValue());
        asset.setSupplier(request.supplier());
        asset.setNotes(request.notes());

        return AssetResponse.from(repository.save(asset));
    }

    /**
     * Lista itens com filtros opcionais e paginacao.
     *
     * @param search   texto buscado em codigo ou nome (contains, case-insensitive)
     * @param category filtro exato por categoria
     * @param status   filtro exato por situacao
     * @param condition filtro exato por estado de conservacao
     * @param location filtro por localizacao (contains, case-insensitive)
     * @param page     numero da pagina (base 0)
     * @param size     tamanho da pagina
     */
    @Transactional(readOnly = true)
    public PageResponse<AssetResponse> list(
            String search,
            String category,
            AssetStatus status,
            AssetCondition condition,
            String location,
            int page,
            int size) {

        // Protege contra valores abusivos ou invalidos de paginacao:
        // - size limitado entre 1 e MAX_PAGE_SIZE (evita carregar tudo na memoria / DoS)
        // - page nunca negativa
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Specification<Asset> spec = buildSpecification(search, category, status, condition, location);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "code"));

        Page<AssetResponse> result = repository.findAll(spec, pageable).map(AssetResponse::from);
        return PageResponse.from(result);
    }

    /** Busca um item por id ou lanca 404. */
    @Transactional(readOnly = true)
    public AssetResponse getById(String id) {
        return AssetResponse.from(findOrThrow(id));
    }

    /**
     * Atualizacao parcial (PATCH): aplica apenas os campos nao nulos recebidos.
     */
    @Transactional
    public AssetResponse update(String id, AssetUpdateRequest request) {
        Asset asset = findOrThrow(id);

        if (request.name() != null) {
            asset.setName(request.name());
        }
        if (request.category() != null) {
            asset.setCategory(request.category());
        }
        if (request.quantity() != null) {
            asset.setQuantity(request.quantity());
        }
        if (request.unit() != null) {
            asset.setUnit(request.unit());
        }
        if (request.location() != null) {
            asset.setLocation(request.location());
        }
        if (request.condition() != null) {
            asset.setCondition(request.condition());
        }
        if (request.status() != null) {
            asset.setStatus(request.status());
        }
        if (request.acquisitionDate() != null) {
            asset.setAcquisitionDate(request.acquisitionDate());
        }
        if (request.acquisitionValue() != null) {
            asset.setAcquisitionValue(request.acquisitionValue());
        }
        if (request.supplier() != null) {
            asset.setSupplier(request.supplier());
        }
        if (request.notes() != null) {
            asset.setNotes(request.notes());
        }

        return AssetResponse.from(repository.save(asset));
    }

    /**
     * Remove um item pelo id.
     *
     * <p>Verifica a existencia antes (via {@link #findOrThrow}) para retornar
     * 404 quando o id nao existe, em vez de falhar silenciosamente.</p>
     */
    @Transactional
    public void delete(String id) {
        Asset asset = findOrThrow(id);
        repository.delete(asset);
    }

    /**
     * Monta o resumo do patrimonio para o Dashboard: total de itens e
     * contagens por situacao/estado relevantes.
     */
    @Transactional(readOnly = true)
    public AssetSummaryResponse summary() {
        return new AssetSummaryResponse(
                repository.count(),
                repository.countByStatus(AssetStatus.DISPONIVEL),
                repository.countByStatus(AssetStatus.EM_USO),
                repository.countByStatus(AssetStatus.EM_MANUTENCAO),
                repository.countByCondition(AssetCondition.DANIFICADO)
        );
    }

    // ---- Auxiliares internos ----

    private Asset findOrThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de patrimonio nao encontrado para o id '" + id + "'."));
    }

    /**
     * Monta o filtro dinamico. Cada parametro so vira condicao se estiver
     * preenchido, e as condicoes sao combinadas com AND.
     */
    private Specification<Asset> buildSpecification(
            String search,
            String category,
            AssetStatus status,
            AssetCondition condition,
            String location) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(search)) {
                String like = "%" + search.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("name")), like)
                ));
            }
            if (hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (condition != null) {
                predicates.add(cb.equal(root.get("condition"), condition));
            }
            if (hasText(location)) {
                predicates.add(cb.like(
                        cb.lower(root.get("location")),
                        "%" + location.toLowerCase(Locale.ROOT) + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Gera o proximo codigo sequencial no padrao PAT-0001.
     * Le o maior codigo existente e incrementa.
     */
    private String generateNextCode() {
        List<String> top = repository.findGeneratedCodesOrdered(PageRequest.of(0, 1));
        int next = 1;
        if (!top.isEmpty()) {
            String lastCode = top.get(0);
            try {
                next = Integer.parseInt(lastCode.substring(CODE_PREFIX.length())) + 1;
            } catch (NumberFormatException ignored) {
                // Se o ultimo codigo nao seguir o padrao numerico, recomeca do 1.
                next = 1;
            }
        }
        // Garante unicidade mesmo diante de codigos manuais que colidam.
        String candidate = formatCode(next);
        while (repository.existsByCode(candidate)) {
            next++;
            candidate = formatCode(next);
        }
        return candidate;
    }

    private String formatCode(int number) {
        return CODE_PREFIX + String.format("%04d", number);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /** Normaliza uma string: null/branco vira null; caso contrario, trim. */
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
