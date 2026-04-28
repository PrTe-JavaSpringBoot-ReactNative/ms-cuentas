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

    //Leer todas las cuentas
    // GET /api/cuentas
    @GetMapping
    public ResponseEntity<List<CuentaResponseDTO>> obtenerTodas() {
        List<CuentaResponseDTO> cuentas = cuentaService.obtenerTodasLasCuentas();
        return ResponseEntity.ok(cuentas);
    }

    //Leer una cuenta por numeroCuenta
    // GET /api/cuentas/{numCuenta}
    @GetMapping("/{numCuenta}")
    public ResponseEntity<CuentaResponseDTO> obtenerPorId(@PathVariable String numCuenta) {
        CuentaResponseDTO cuenta = cuentaService.obtenerCuentaPorNumeroCuenta(numCuenta);
        return ResponseEntity.ok(new CuentaResponseDTO());
    }

    // Crear una nueva cuenta
    // POST /api/cuentas
    @PostMapping
    public ResponseEntity<CuentaResponseDTO> crearCuenta(@Valid @RequestBody CuentaRequestDTO cuentaRequest) {
        CuentaResponseDTO nuevaCuenta = cuentaService.crearCuenta(cuentaRequest);
        return new ResponseEntity<>(nuevaCuenta, HttpStatus.CREATED);
    }

    // Actualizar una cuenta existente
    // PUT /api/cuentas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> actualizarCuenta(@PathVariable Long id, @Valid @RequestBody CuentaRequestDTO cuentaRequest) {
        CuentaResponseDTO cuentaActualizada = cuentaService.actualizarCuenta(id, cuentaRequest);
        return ResponseEntity.ok(cuentaActualizada);
    }

    // Eliminar una cuenta
    // DELETE /api/cuentas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id) {
        cuentaService.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }
}
