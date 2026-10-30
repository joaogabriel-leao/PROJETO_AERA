package br.com.aera.backend.common.exception;

/**
 * Lancada quando um recurso solicitado nao existe (ex.: item de patrimonio
 * com id inexistente). O handler global traduz esta excecao em HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
