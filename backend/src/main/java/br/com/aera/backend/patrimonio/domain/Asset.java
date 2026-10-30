package br.com.aera.backend.patrimonio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Item do patrimonio/material da instituicao.
 *
 * <p>Mapeia a tabela {@code assets} no banco e espelha o tipo {@code Asset}
 * definido pelo frontend. Cada campo abaixo corresponde a um campo do
 * contrato em {@code frontend/src/types/index.ts}.</p>
 */
@Entity
@Table(name = "assets")
public class Asset {

    /** Identificador unico (UUID em formato String). Gerado pelo service. */
    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    /** Codigo do patrimonio, ex.: "PAT-0001". Unico. Gerado se nao informado. */
    @Column(nullable = false, unique = true, length = 40)
    private String code;

    /** Nome do item. Obrigatorio. */
    @Column(nullable = false)
    private String name;

    /** Categoria, ex.: "Equipamento esportivo", "Movel", "Eletronico". */
    @Column(nullable = false)
    private String category;

    /** Quantidade em estoque. */
    @Column(nullable = false)
    private Integer quantity;

    /** Unidade de medida, ex.: "Unidade", "Kit", "Par". */
    @Column(nullable = false)
    private String unit;

    /** Localizacao fisica do item. */
    @Column(nullable = false)
    private String location;

    /** Estado de conservacao (armazenado como texto). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssetCondition condition;

    /** Situacao atual (armazenado como texto). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status;

    /** Data de aquisicao (opcional). */
    @Column
    private LocalDate acquisitionDate;

    /** Valor de aquisicao (opcional). BigDecimal para evitar erros com dinheiro. */
    @Column(precision = 15, scale = 2)
    private BigDecimal acquisitionValue;

    /** Fornecedor (opcional). */
    @Column
    private String supplier;

    /** Observacoes livres (opcional). */
    @Column(length = 2000)
    private String notes;

    /** Construtor vazio exigido pelo JPA/Hibernate. */
    public Asset() {
    }

    // ---- Getters e Setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public AssetCondition getCondition() {
        return condition;
    }

    public void setCondition(AssetCondition condition) {
        this.condition = condition;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public BigDecimal getAcquisitionValue() {
        return acquisitionValue;
    }

    public void setAcquisitionValue(BigDecimal acquisitionValue) {
        this.acquisitionValue = acquisitionValue;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
