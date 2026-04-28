package com.example.cuentas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Endpoint de salud del microservicio ms-cuentas.
 * Permite verificar que el servicio está activo.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
                "status", "UP",
                "service", "ms-cuentas",
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(response);
    }
}
