package com.example.cuentas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequestDTO {

    @NotNull(message = "La cuenta es obligatoria")
    private String numeroCuenta;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private String tipoMovimiento;

    @NotNull(message = "El valor es obligatorio")
    private Double valor;
}
