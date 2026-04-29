package com.example.cuentas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 - Cuenta no encontrada.
     * Se lanza cuando se busca una cuenta por número o ID y no existe.
     */
    @ExceptionHandler(CuentaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaNotFound(CuentaNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    /**
     * 409 - Número de cuenta duplicado.
     * Se lanza al intentar crear una cuenta con un número que ya existe.
     */
    @ExceptionHandler(CuentaYaExisteException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaYaExiste(CuentaYaExisteException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflicto - Recurso duplicado", ex.getMessage());
    }

    /**
     * 422 - Saldo insuficiente.
     * Se lanza al intentar registrar un movimiento que deja saldo negativo.
     * El mensaje devuelto es exactamente el requerido por la especificación: "Saldo no disponible"
     */
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo no disponible", ex.getMessage());
    }

    /**
     * 400 - Error de validación de cliente.
     * Se lanza cuando el cliente no existe o hay error en la comunicación con ms-clientes.
     */
    @ExceptionHandler(ClienteNoExisteException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNoExiste(ClienteNoExisteException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Cliente inválido", ex.getMessage());
    }

    /**
     * 400/500 - Error de comunicación con ms-clientes.
     * Se lanza cuando hay problemas de conectividad o el cliente solicitado no existe.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("no existe") ?
                HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        return buildResponse(status, "Error", ex.getMessage() != null ? ex.getMessage() : "Error desconocido");
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<Map<String, Object>> handleCompletionException(CompletionException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof SaldoInsuficienteException) {
            return handleSaldoInsuficiente((SaldoInsuficienteException) cause);
        } else if (cause instanceof CuentaNotFoundException) {
            return handleCuentaNotFound((CuentaNotFoundException) cause);
        } else if (cause instanceof CuentaYaExisteException) {
            return handleCuentaYaExiste((CuentaYaExisteException) cause);
        } else if (cause instanceof ClienteNoExisteException) {
            return handleClienteNoExiste((ClienteNoExisteException) cause);
        } else if (cause instanceof RuntimeException) {
            return handleRuntimeException((RuntimeException) cause);
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error",
                cause != null && cause.getMessage() != null ? cause.getMessage() : "Error desconocido");
    }

    /**
     * 400 - Validación de campos fallida.
     * Se lanza cuando el body del request no pasa las validaciones de @Valid.
     * Devuelve el detalle de todos los campos que fallaron.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validación fallida", errores);
    }

    /**
     * 500 - Error genérico no controlado.
     * Captura cualquier excepción que no tenga un handler específico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage() != null ? ex.getMessage() : "Error desconocido"
        );
    }

    /**
     * Constructor auxiliar de respuesta de error estandarizada.
     * Todos los errores tienen el mismo formato JSON:
     * {
     *   "timestamp": "...",
     *   "status": 404,
     *   "error": "Recurso no encontrado",
     *   "message": "Cuenta con número 478758 no encontrada"
     * }
     */
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
