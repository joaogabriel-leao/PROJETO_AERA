package br.org.aera.financeiro.service;

import br.org.aera.financeiro.domain.TipoMovimentacao;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Dados de entrada independentes da camada HTTP. Valores são sempre positivos. */
public record RegistrarMovimentacao(
        @NotNull UUID contaId,
        @NotNull UUID categoriaId,
        @NotNull TipoMovimentacao tipo,
        @NotNull @DecimalMin("0.01") @Digits(integer = 13, fraction = 2) BigDecimal valor,
        @NotNull LocalDate dataMovimentacao,
        @NotBlank @Size(max = 500) String descricao) {
}
