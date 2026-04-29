package com.example.cuentas.service;

import com.example.cuentas.client.ClientesServiceClient;
import com.example.cuentas.dto.MovimientoRequestDTO;
import com.example.cuentas.dto.MovimientoResponseDTO;
import com.example.cuentas.entity.Cuenta;
import com.example.cuentas.entity.Movimiento;
import com.example.cuentas.exception.CuentaNotFoundException;
import com.example.cuentas.exception.SaldoInsuficienteException;
import com.example.cuentas.repository.CuentaRepository;
import com.example.cuentas.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final ClientesServiceClient clientesServiceClient;

    /**
     * Obtiene todos los movimientos de una cuenta específica.
     *
     * @param numeroCuenta numero de la cuenta
     * @return Lista de MovimientoResponseDTO
     */
    public List<MovimientoResponseDTO> obtenerMovimientosPorCuenta(String numeroCuenta) {
        log.info("Obteniendo movimientos de la cuenta {}", numeroCuenta);

        cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNotFoundException("Cuenta ID: " + numeroCuenta));

        return movimientoRepository.findByNumeroCuenta(numeroCuenta).stream()
                .map(this::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Registra un nuevo movimiento en una cuenta.
     * @param movimientoRequest DTO con datos del movimiento
     * @return MovimientoResponseDTO del movimiento creado
     * @throws CuentaNotFoundException Si la cuenta no existe
     * @throws SaldoInsuficienteException Si es retiro y no hay saldo suficiente
     * @throws RuntimeException Si cliente no existe o no está activo
     */
    public MovimientoResponseDTO crearMovimiento(MovimientoRequestDTO movimientoRequest) {
        log.info("Creando movimiento: {} de {} en cuenta {}",
                movimientoRequest.getTipoMovimiento(),
                movimientoRequest.getValor(),
                movimientoRequest.getNumeroCuenta());

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(movimientoRequest.getNumeroCuenta())
                .orElseThrow(() -> {
                    log.warn("Cuenta {} no encontrada", movimientoRequest.getNumeroCuenta());
                    return new CuentaNotFoundException("Cuenta ID: " + movimientoRequest.getNumeroCuenta());
                });

        log.debug("Validando cliente {} asincronicamente para movimiento...", cuenta.getClienteId());
        CompletableFuture<Boolean> validacionFuture = clientesServiceClient.validarClienteActivoAsync(cuenta.getClienteId());
        boolean clienteActivo = validacionFuture.join();

        if (!clienteActivo) {
            log.warn("Cliente {} no existe o no está activo", cuenta.getClienteId());
            throw new RuntimeException("Cliente " + cuenta.getClienteId() + " no existe o no está activo");
        }
        log.debug("✓ Cliente {} validado como activo", cuenta.getClienteId());

        return procesarMovimiento(cuenta, movimientoRequest);
    }

    @Transactional
    private MovimientoResponseDTO procesarMovimiento(Cuenta cuenta, MovimientoRequestDTO movimientoRequest) {
        Double valorMovimiento = movimientoRequest.getValor();
        String tipo = movimientoRequest.getTipoMovimiento().toUpperCase();

        Double montoDescontar = "RETIRO".equals(tipo) ? valorMovimiento : -valorMovimiento;

        if ("RETIRO".equals(tipo)) {
            if (cuenta.getSaldoDisponible() < -montoDescontar) {
                log.warn("Saldo insuficiente en cuenta {}. Saldo: {}, Movimiento: {}",
                        cuenta.getCuentaId(),
                        cuenta.getSaldoDisponible(),
                        montoDescontar);
                throw new SaldoInsuficienteException("Saldo no disponible");
            }
        }

        if (!"RETIRO".equals(tipo) && !"DEPOSITO".equals(tipo)) {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + tipo);
        }

        cuenta.setSaldoDisponible(cuenta.getSaldoDisponible() + montoDescontar);

        cuentaRepository.save(cuenta);

        Movimiento movimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(tipo)
                .valor(valorMovimiento)
                .saldo(cuenta.getSaldoDisponible())
                .numeroCuenta(movimientoRequest.getNumeroCuenta())
                .cuentaId(cuenta.getCuentaId())
                .build();

        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);

        log.info("Movimiento {} creado exitosamente. Nuevo saldo: {}",
                movimientoGuardado.getMovimientoId(),
                cuenta.getSaldoDisponible());

        return entityToResponseDTO(movimientoGuardado);
    }

    /**
     * Obtiene movimientos de una cuenta en un rango de fechas.
     *
     * @param numeroCuenta numero de la cuenta
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Lista de movimientos en el rango
     */
    public List<MovimientoResponseDTO> obtenerMovimientosPorFecha(String numeroCuenta, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Obteniendo movimientos de cuenta {} entre {} y {}",
                numeroCuenta, fechaInicio, fechaFin);

        return movimientoRepository.findByNumeroCuentaAndFechaBetween(numeroCuenta, fechaInicio, fechaFin).stream()
                .map(this::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    private MovimientoResponseDTO entityToResponseDTO(Movimiento movimiento) {
        return MovimientoResponseDTO.builder()
                .movimientoId(movimiento.getMovimientoId())
                .fecha(movimiento.getFecha())
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .valor(movimiento.getValor())
                .saldo(movimiento.getSaldo())
                .numeroCuenta(movimiento.getNumeroCuenta())
                .build();
    }
}
