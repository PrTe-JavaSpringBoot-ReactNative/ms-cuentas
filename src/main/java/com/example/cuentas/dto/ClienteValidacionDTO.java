package com.example.cuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO usado como mensaje en RabbitMQ para la validación de clienteId.
 *
 * Ciclo de vida del mensaje:
 *
 *  1. ms-cuentas crea este objeto al guardar una cuenta nueva.
 *     Campos poblados: clienteId, cuentaId, numeroCuenta.
 *     Campo 'clienteExiste' queda en null (aún no se sabe).
 *
 *  2. ms-cuentas lo publica en la queue: clientes.solicitudes.validacion
 *
 *  3. ms-clientes recibe el mensaje, busca el clienteId en su DB,
 *     y completa el campo 'clienteExiste' con true o false.
 *
 *  4. ms-clientes publica la respuesta en: cuentas.validacion.respuesta
 *
 *  5. ms-cuentas recibe la respuesta y actúa:
 *     - clienteExiste = true  → cuenta queda activa, no se hace nada
 *     - clienteExiste = false → cuenta se marca INVALIDA o se elimina
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteValidacionDTO {

    /** ID del cliente a validar (viene del request de creación de cuenta) */
    private Long clienteId;

    /** ID interno de la cuenta recién creada (para poder encontrarla en la respuesta) */
    private Long cuentaId;

    /** Número de cuenta (informativo, útil para logs) */
    private String numeroCuenta;

    /**
     * Resultado de la validación.
     * null     → solicitud aún no procesada (mensaje de ida)
     * true     → el clienteId existe en ms-clientes
     * false    → el clienteId NO existe en ms-clientes
     */
    private Boolean clienteExiste;
}
