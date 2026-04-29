package com.example.cuentas.exception;

public class CuentaNotFoundException extends RuntimeException {

    public CuentaNotFoundException(String numeroCuenta) {
        super("Cuenta con número " + numeroCuenta + " no encontrada");
    }

    public CuentaNotFoundException(String numeroCuenta, Throwable cause) {
        super("Cuenta con número " + numeroCuenta + " no encontrada", cause);
    }
}
