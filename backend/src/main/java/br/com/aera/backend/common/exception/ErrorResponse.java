package br.com.aera.backend.common.exception;

import java.time.OffsetDateTime;

/**
 * Corpo padrao de resposta de erro da API.
 *
 * <p>O campo {@code message} e o que o frontend le em
 * {@code frontend/src/services/http.ts} para exibir o erro ao usuario.</p>
 *
 * @param status    codigo HTTP
 * @param error     nome curto do status (ex.: "Not Found")
 * @param message   mensagem amigavel para o usuario
 * @param timestamp momento do erro
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, OffsetDateTime.now());
    }
}
