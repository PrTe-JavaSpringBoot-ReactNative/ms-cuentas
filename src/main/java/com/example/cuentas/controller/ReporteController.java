package com.example.cuentas.controller;

import com.example.cuentas.dto.ReporteEstadoCuentaDTO;
import com.example.cuentas.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para Reportes.
 *
 * F4: Genera un reporte de "Estado de Cuenta" especificando:
 * - Un rango de fechas
 * - Un cliente (opcional, si se envía; si no, se asume el clienteId del path)
 *
 * Retorna:
 * - Cuentas asociadas con sus respectivos saldos
 * - Detalle de movimientos de las cuentas en el rango de fechas
 */
@Slf4j
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    /**
     * GET /api/reportes?clienteId={id}&fechaInicio={date}&fechaFin={date}
     * Genera un reporte de estado de cuenta para un cliente en un rango de fechas.
     * @param clienteId ID del cliente
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de ReporteEstadoCuentaDTO
     */
    @GetMapping
    public ResponseEntity<List<ReporteEstadoCuentaDTO>> generarReporte(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        log.info("GET /reportes - Generando reporte para cliente {} entre {} y {}",
                clienteId, fechaInicio, fechaFin);

        // Convertir LocalDate a LocalDateTime (inicio del día y fin del día)
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();

        List<ReporteEstadoCuentaDTO> reporte = reporteService.generarReporteEstadoCuenta(
                clienteId, inicio, fin);
        return ResponseEntity.ok(reporte);
    }
}
