package br.org.aera.financeiro.service;

/** Referência inexistente ou inativa que pode ser corrigida no formulário. */
public class MovimentacaoInvalidaException extends RuntimeException {
    private final String campo;

    public MovimentacaoInvalidaException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() { return campo; }
}
