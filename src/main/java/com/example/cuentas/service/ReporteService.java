package com.example.cuentas.service;

import com.example.cuentas.client.ClientesServiceClient;
import com.example.cuentas.dto.ReporteEstadoCuentaDTO;
import com.example.cuentas.entity.Cuenta;
import com.example.cuentas.entity.Movimiento;
import com.example.cuentas.repository.CuentaRepository;
import com.example.cuentas.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ClientesServiceClient clientesServiceClient;

    /**
     * Genera un reporte de estado de cuenta para un cliente en un rango de fechas.
     *
     * @param clienteId ID del cliente
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de ReporteEstadoCuentaDTO con los movimientos en el rango
     * @throws RuntimeException si el cliente no existe o hay error de conectividad
     */
    public List<ReporteEstadoCuentaDTO> generarReporteEstadoCuenta(
            Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {

        log.info("Generando reporte de estado de cuenta para cliente {} entre {} y {}",
                clienteId, fechaInicio, fechaFin);

        log.debug("Iniciando obtención PARALELA de datos del cliente y cuentas...");
        CompletableFuture<ClientesServiceClient.ClienteDTO> clienteFuture =
                clientesServiceClient.obtenerClientePorIdAsync(clienteId);

        log.debug("Cliente {} tiene {} cuenta(s)", clienteId, cuentasDelCliente.size());

        log.debug("Esperando respuesta de MS-Clientes...");
        ClientesServiceClient.ClienteDTO cliente = clienteFuture.join();
        log.debug("✓ Cliente obtenido: {}", cliente.getNombre());

        List<ReporteEstadoCuentaDTO> reportes = construirReporte(cliente, cuentasDelCliente, fechaInicio, fechaFin);

        log.info("Reporte generado con {} líneas", reportes.size());
        return reportes;
    }


    private List<Cuenta> obtenerCuentasDelCliente(Long clienteId) {
        return cuentaRepository.findAll().stream()
                .filter(c -> c.getClienteId().equals(clienteId))
                .toList();
    }


    private List<ReporteEstadoCuentaDTO> construirReporte(
            ClientesServiceClient.ClienteDTO cliente,
            List<Cuenta> cuentas,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        List<ReporteEstadoCuentaDTO> reportes = new ArrayList<>();

        for (Cuenta cuenta : cuentas) {
            List<Movimiento> movimientos = movimientoRepository
                    .findByNumeroCuentaAndFechaBetween(cuenta.getNumeroCuenta(), fechaInicio, fechaFin);

            log.debug("Cuenta {} tiene {} movimiento(s) en el rango",
                    cuenta.getNumeroCuenta(), movimientos.size());

            for (Movimiento movimiento : movimientos) {
                ReporteEstadoCuentaDTO reporte = ReporteEstadoCuentaDTO.builder()
                        .fecha(movimiento.getFecha())
                        .cliente(cliente.getNombre())
                        .numeroCuenta(cuenta.getNumeroCuenta())
                        .tipo(cuenta.getTipoCuenta())
                        .saldoInicial(cuenta.getSaldoInicial())
                        .estado("ACTIVA".equals(cuenta.getEstado()))
                        .movimiento(movimiento.getValor())
                        .saldoDisponible(movimiento.getSaldo())
                        .build();

                reportes.add(reporte);
            }
        }

        return reportes;
    }
}
