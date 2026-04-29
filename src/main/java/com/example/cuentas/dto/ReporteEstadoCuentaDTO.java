package com.example.cuentas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteEstadoCuentaDTO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;

    private String cliente;

    private String numeroCuenta;

    private String tipo;

    private Double saldoInicial;

    private Boolean estado;

    private Double movimiento;

    private Double saldoDisponible;
}
