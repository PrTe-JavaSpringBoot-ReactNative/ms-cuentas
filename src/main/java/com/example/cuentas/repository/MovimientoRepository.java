package com.example.cuentas.repository;

import com.example.cuentas.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, String> {

    List<Movimiento> findByNumeroCuenta(String numeroCuenta);

    List<Movimiento> findByNumeroCuentaAndFechaBetween(String numeroCuenta, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
