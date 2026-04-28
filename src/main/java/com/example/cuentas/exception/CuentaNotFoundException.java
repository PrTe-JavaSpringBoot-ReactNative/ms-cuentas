package com.example.cuentas.exception;

public class CuentaNotFoundException extends RuntimeException {

    public CuentaNotFoundException(String message) {
        super(message);
    }

    public CuentaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CuentaNotFoundException(String numeroCuenta) {
        super("Cuenta con numero de cuenta " + numeroCuenta + " no encontrado");
    }
}