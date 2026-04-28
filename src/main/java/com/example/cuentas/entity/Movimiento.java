package com.example.cuentas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;   
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "movimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movimientoId;

    @Column(nullable = false)
    private Date fecha;

    @Column(nullable = false, length = 255)
    private String tipoMovimiento;

    @Column(nullable = false)
    private Double valor;

    @Column(nullable = false)
    private Double saldo;

    @Column(nullable = false)
    private Long cuentaId;
}
