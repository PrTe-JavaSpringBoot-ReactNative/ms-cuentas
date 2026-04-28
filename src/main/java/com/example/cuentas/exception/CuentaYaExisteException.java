package com.example.cuentas.exception;

public class CuentaYaExisteException extends RuntimeException {

    public CuentaYaExisteException(String message) {
        super(message);
    }

    public CuentaYaExisteException(String message, Throwable cause) {
        super(message, cause);
    }
}
