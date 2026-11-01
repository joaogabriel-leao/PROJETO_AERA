package br.org.aera.financeiro.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="movimentacoes")
public class Movimentacao {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="conta_id") private Conta conta;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="categoria_id") private Categoria categoria;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id") private Usuario usuario;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private TipoMovimentacao tipo;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal valor;
    @Column(name="data_movimentacao",nullable=false) private LocalDate dataMovimentacao;
    @Column(nullable=false,length=500) private String descricao;
    @Column(name="criado_em",nullable=false) private OffsetDateTime criadoEm;
    protected Movimentacao() {}
    public Movimentacao(Conta conta,Categoria categoria,Usuario usuario,TipoMovimentacao tipo,BigDecimal valor,LocalDate data,String descricao){
        this.id=UUID.randomUUID();this.conta=conta;this.categoria=categoria;this.usuario=usuario;
        this.tipo=tipo;this.valor=valor;this.dataMovimentacao=data;this.descricao=descricao;
        this.criadoEm=OffsetDateTime.now();
    }
    public UUID getId(){return id;}
    public Conta getConta(){return conta;}
    public Categoria getCategoria(){return categoria;}
    public Usuario getUsuario(){return usuario;}
    public TipoMovimentacao getTipo(){return tipo;}
    public BigDecimal getValor(){return valor;}
    public LocalDate getDataMovimentacao(){return dataMovimentacao;}
    public String getDescricao(){return descricao;}
    public OffsetDateTime getCriadoEm(){return criadoEm;}
}
