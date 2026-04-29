package com.example.cuentas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cuentas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cuentaId;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroCuenta;

    @Column(nullable = false, length = 50)
    private String tipoCuenta;

    @Column(nullable = false)
    private Double saldoInicial;

    @Column(nullable = false)
    private Double saldoDisponible;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private Long clienteId;
}
