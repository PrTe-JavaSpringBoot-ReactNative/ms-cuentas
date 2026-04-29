package com.example.cuentas.exception;

public class ClienteNoExisteException extends RuntimeException {

    public ClienteNoExisteException(String message) {
        super(message);
    }

    public ClienteNoExisteException(String message, Throwable cause) {
        super(message, cause);
    }
}
