package com.example.cuentas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para ms-cuentas.
 *
 * Centraliza el tratamiento de errores y retorna respuestas JSON estandarizadas.
 *
 * TODO: Agregar handlers específicos del dominio:
 *   - CuentaNotFoundException      → 404
 *   - SaldoInsuficienteException   → 422 (F3: "Saldo no disponible")
 *   - ClienteInvalidoException     → 400 (clienteId no existe en ms-clientes)
 *   - MethodArgumentNotValidException → 400 (validaciones de @Valid)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // TODO: Handlers específicos de dominio aquí
}
