package com.example.cuentas.controller;

import com.example.cuentas.dto.MovimientoRequestDTO;
import com.example.cuentas.dto.MovimientoResponseDTO;
import com.example.cuentas.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    /**
     * GET /api/movimientos/{numeroCuenta}
     *
     * @param numeroCuenta numero de la cuenta
     * @return Lista de movimientos
     */
    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<List<MovimientoResponseDTO>> obtenerMovimientosPorCuenta(
            @PathVariable String numeroCuenta) {
        log.info("GET /movimientos/{} - Obteniendo movimientos de la cuenta", numeroCuenta);
        return ResponseEntity.ok(movimientoService.obtenerMovimientosPorCuenta(numeroCuenta));
    }

    /**
     * POST /api/movimientos
     * @param movimientoRequest DTO con datos del movimiento
     * @return MovimientoResponseDTO del movimiento creado
     */
    @PostMapping
    public ResponseEntity<MovimientoResponseDTO> crearMovimiento(
            @Valid @RequestBody MovimientoRequestDTO movimientoRequest) {
        log.info("POST /movimientos - Creando movimiento: {}", movimientoRequest.getTipoMovimiento());
        MovimientoResponseDTO movimiento = movimientoService.crearMovimiento(movimientoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimiento);
    }
}
