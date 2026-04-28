package com.example.cuentas.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaRequestDTO {
    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 20, message = "El número de cuenta no puede exceder los 20 caracteres")
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Size(max = 255, message = "El tipo de cuenta no puede exceder los 255 caracteres")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @PositiveOrZero(message = "El saldo inicial debe ser un valor positivo o cero")
    private Double saldoInicial;

    @NotNull(message = "El saldo disponible es obligatorio")
    @PositiveOrZero(message = "El saldo disponible debe ser un valor positivo o cero")
    private Double saldoDisponible;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

}
