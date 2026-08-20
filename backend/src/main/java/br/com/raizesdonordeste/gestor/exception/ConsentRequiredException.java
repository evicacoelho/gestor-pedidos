package br.com.raizesdonordeste.gestor.exception;

public class ConsentRequiredException extends RuntimeException {
    public ConsentRequiredException(String message) {
        super(message);
    }
}
