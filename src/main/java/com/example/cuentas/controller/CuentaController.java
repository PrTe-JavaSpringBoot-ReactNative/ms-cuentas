package com.example.cuentas.controller;

import com.example.cuentas.dto.CuentaRequestDTO;
import com.example.cuentas.dto.CuentaResponseDTO;
import com.example.cuentas.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    // GET /api/cuentas
    @GetMapping
    public ResponseEntity<List<CuentaResponseDTO>> obtenerTodas() {
        List<CuentaResponseDTO> cuentas = cuentaService.obtenerTodasLasCuentas();
        return ResponseEntity.ok(cuentas);
    }

    // GET /api/cuentas/{numCuenta}
    @GetMapping("/{numCuenta}")
    public ResponseEntity<CuentaResponseDTO> obtenerPorNumeroCuenta(@PathVariable String numCuenta) {
        CuentaResponseDTO cuenta = cuentaService.obtenerCuentaPorNumeroCuenta(numCuenta);
        return ResponseEntity.ok(cuenta);
    }

    // POST /api/cuentas
    @PostMapping
    public ResponseEntity<CuentaResponseDTO> crearCuenta(@Valid @RequestBody CuentaRequestDTO cuentaRequest) {
        CuentaResponseDTO nuevaCuenta = cuentaService.crearCuenta(cuentaRequest);
        return new ResponseEntity<>(nuevaCuenta, HttpStatus.CREATED);
    }

    // PUT /api/cuentas/{numCuenta}
    @PutMapping("/{numCuenta}")
    public ResponseEntity<CuentaResponseDTO> actualizarCuenta(@PathVariable String numCuenta, @Valid @RequestBody CuentaRequestDTO cuentaRequest) {
        CuentaResponseDTO cuentaActualizada = cuentaService.actualizarCuenta(numCuenta, cuentaRequest);
        return ResponseEntity.ok(cuentaActualizada);
    }

    // DELETE /api/cuentas/{numCuenta}
    @DeleteMapping("/{numCuenta}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable String numCuenta) {
        cuentaService.eliminarCuenta(numCuenta);
        return ResponseEntity.noContent().build();
    }
}
