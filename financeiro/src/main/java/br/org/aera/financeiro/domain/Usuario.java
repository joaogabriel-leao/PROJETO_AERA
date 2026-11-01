package br.org.aera.financeiro.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="usuarios")
public class Usuario {
    @Id private UUID id;
    @Column(nullable=false,unique=true,length=60) private String username;
    @Column(name="senha_hash",nullable=false) private String senhaHash;
    @Column(nullable=false,length=30) private String perfil;
    @Column(nullable=false) private boolean ativo = true;
    protected Usuario() {}
    public Usuario(String username,String senhaHash,String perfil) {
        this.id=UUID.randomUUID(); this.username=username; this.senhaHash=senhaHash; this.perfil=perfil;
    }
    public UUID getId(){return id;}
    public String getUsername(){return username;}
    public String getSenhaHash(){return senhaHash;}
    public String getPerfil(){return perfil;}
    public boolean isAtivo(){return ativo;}
}
