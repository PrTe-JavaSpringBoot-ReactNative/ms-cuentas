package com.example.cuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaResponseDTO {
    private String numeroCuenta;
    private String tipoCuenta;
    private Double saldoInicial;
    private Double saldoDisponible;
    private Long clienteId;
}