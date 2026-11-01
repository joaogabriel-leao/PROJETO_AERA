package br.org.aera.financeiro.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="categorias")
public class Categoria {
    @Id private UUID id;
    @Column(nullable=false,unique=true,length=100) private String nome;
    @Column(nullable=false) private boolean ativa=true;
    protected Categoria() {}
    public Categoria(String nome){this.id=UUID.randomUUID();this.nome=nome;}
    public UUID getId(){return id;}
    public String getNome(){return nome;}
    public boolean isAtiva(){return ativa;}
}
