package com.example.clientes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteValidacionDTO {

    private Long clienteId;

    private Long cuentaId;

    private String numeroCuenta;

    private Boolean clienteExiste;
}
