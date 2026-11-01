package br.org.aera.financeiro.web;

import br.org.aera.financeiro.domain.TipoMovimentacao;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MovimentacaoForm {
 @NotNull private UUID contaId;
 @NotNull private UUID categoriaId;
 @NotNull private TipoMovimentacao tipo;
 @NotNull @DecimalMin(value="0.01") @Digits(integer=13,fraction=2) private BigDecimal valor;
 @NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate dataMovimentacao = LocalDate.now();
 @NotBlank @Size(max=500) private String descricao;
 public UUID getContaId(){return contaId;} public void setContaId(UUID value){contaId=value;}
 public UUID getCategoriaId(){return categoriaId;} public void setCategoriaId(UUID value){categoriaId=value;}
 public TipoMovimentacao getTipo(){return tipo;} public void setTipo(TipoMovimentacao value){tipo=value;}
 public BigDecimal getValor(){return valor;} public void setValor(BigDecimal value){valor=value;}
 public LocalDate getDataMovimentacao(){return dataMovimentacao;} public void setDataMovimentacao(LocalDate value){dataMovimentacao=value;}
 public String getDescricao(){return descricao;} public void setDescricao(String value){descricao=value;}
}
