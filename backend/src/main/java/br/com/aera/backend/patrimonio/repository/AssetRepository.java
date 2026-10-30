package br.com.aera.backend.patrimonio.repository;

import br.com.aera.backend.patrimonio.domain.Asset;
import br.com.aera.backend.patrimonio.domain.AssetCondition;
import br.com.aera.backend.patrimonio.domain.AssetStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositorio do patrimonio.
 *
 * <p>Estendendo {@link JpaRepository}, o Spring Data gera automaticamente
 * as operacoes basicas (save, findById, findAll, delete, count...).</p>
 *
 * <p>Estendendo {@link JpaSpecificationExecutor}, ganhamos a capacidade de
 * montar filtros dinamicos e combinaveis (usado na listagem com filtros).</p>
 */
@Repository
public interface AssetRepository
        extends JpaRepository<Asset, String>, JpaSpecificationExecutor<Asset> {

    /** Verifica se ja existe um item com o codigo informado (evita duplicados). */
    boolean existsByCode(String code);

    /** Conta itens por situacao (ex.: quantos estao DISPONIVEL). */
    long countByStatus(AssetStatus status);

    /** Conta itens por estado de conservacao (ex.: quantos estao DANIFICADO). */
    long countByCondition(AssetCondition condition);

    /**
     * Retorna os codigos ja usados no padrao "PAT-NNNN", ordenados do maior
     * numero para o menor. Usado com {@code Pageable} de tamanho 1 para obter
     * apenas o maior codigo e gerar o proximo sequencial.
     *
     * <p>Ordenamos por LENGTH primeiro (para "PAT-10" vir depois de "PAT-9")
     * e depois pelo proprio codigo.</p>
     */
    @Query("SELECT a.code FROM Asset a WHERE a.code LIKE 'PAT-%' "
            + "ORDER BY LENGTH(a.code) DESC, a.code DESC")
    List<String> findGeneratedCodesOrdered(Pageable pageable);
}
