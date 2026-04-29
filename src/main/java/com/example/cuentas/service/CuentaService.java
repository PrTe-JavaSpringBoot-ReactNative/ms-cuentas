package com.example.cuentas.service;

import com.example.cuentas.client.ClientesServiceClient;
import com.example.cuentas.dto.CuentaRequestDTO;
import com.example.cuentas.dto.CuentaResponseDTO;
import com.example.cuentas.entity.Cuenta;
import com.example.cuentas.exception.CuentaNotFoundException;
import com.example.cuentas.exception.CuentaYaExisteException;
import com.example.cuentas.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClientesServiceClient clientesServiceClient;

    public List<CuentaResponseDTO> obtenerTodasLasCuentas() {
        log.info("Obteniendo todas las cuentas...");
        return cuentaRepository.findAll().stream()
                .map(this::entityToResponseDTO)
                .collect(Collectors.toList());
    }

    public CuentaResponseDTO obtenerCuentaPorNumeroCuenta(String numeroCuenta) {
        log.info("Obteniendo cuenta con número: {}", numeroCuenta);
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> {
                    log.warn("Cuenta con número {} no encontrada", numeroCuenta);
                    return new CuentaNotFoundException(numeroCuenta);
                });
        return entityToResponseDTO(cuenta);
    }

    public CuentaResponseDTO crearCuenta(CuentaRequestDTO cuentaRequest) {
        log.info("Creando cuenta con número: {}", cuentaRequest.getNumeroCuenta());

        if (cuentaRepository.findByNumeroCuenta(cuentaRequest.getNumeroCuenta()).isPresent()) {
            log.warn("Número de cuenta {} ya existe", cuentaRequest.getNumeroCuenta());
            throw new CuentaYaExisteException("Cuenta con número " + cuentaRequest.getNumeroCuenta() + " ya existe");
        }

        log.debug("Validando cliente {} asincronicamente...", cuentaRequest.getClienteId());
        CompletableFuture<Boolean> validacionFuture = clientesServiceClient.validarClienteActivoAsync(cuentaRequest.getClienteId());
        boolean clienteActivo = validacionFuture.join();

        if (!clienteActivo) {
            log.warn("Cliente {} no existe o no está activo", cuentaRequest.getClienteId());
            throw new RuntimeException("Cliente " + cuentaRequest.getClienteId() +
                    " no existe o no está activo");
        }
        log.debug("Cliente {} validado como activo", cuentaRequest.getClienteId());

        return guardarCuenta(cuentaRequest);
    }

    @Transactional
    private CuentaResponseDTO guardarCuenta(CuentaRequestDTO cuentaRequest) {
        Cuenta cuenta = requestDTOToEntity(cuentaRequest);
        cuenta.setEstado("ACTIVA");
        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);

        log.info("Cuenta {} creada exitosamente con cliente validado",
                cuentaGuardada.getNumeroCuenta());

        return entityToResponseDTO(cuentaGuardada);
    }

    @Transactional
    public CuentaResponseDTO actualizarCuenta(String numeroCuenta, CuentaRequestDTO cuentaRequest) {
        log.info("Actualizando cuenta con número: {}", numeroCuenta);

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> {
                    log.warn("Cuenta con número {} no encontrada", numeroCuenta);
                    return new CuentaNotFoundException(numeroCuenta);
                });

        cuenta.setTipoCuenta(cuentaRequest.getTipoCuenta());
        cuenta.setSaldoInicial(cuentaRequest.getSaldoInicial());
        cuenta.setSaldoDisponible(cuentaRequest.getSaldoDisponible());
        cuenta.setClienteId(cuentaRequest.getClienteId());

        Cuenta cuentaActualizada = cuentaRepository.save(cuenta);

        log.info("Cuenta {} actualizada exitosamente", numeroCuenta);
        return entityToResponseDTO(cuentaActualizada);
    }

    @Transactional
    public void eliminarCuenta(String numeroCuenta) {
        log.info("Eliminando cuenta con número: {}", numeroCuenta);

        if (!cuentaRepository.findByNumeroCuenta(numeroCuenta).isPresent()) {
            log.warn("Cuenta con número {} no encontrada para eliminación", numeroCuenta);
            throw new CuentaNotFoundException(numeroCuenta);
        }

        cuentaRepository.deleteByNumeroCuenta(numeroCuenta);
        log.info("Cuenta con número {} eliminada exitosamente", numeroCuenta);
    }

    private CuentaResponseDTO entityToResponseDTO(Cuenta cuenta) {
        return CuentaResponseDTO.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .saldoDisponible(cuenta.getSaldoDisponible())
                .clienteId(cuenta.getClienteId())
                .build();
    }

    private Cuenta requestDTOToEntity(CuentaRequestDTO request) {
        return Cuenta.builder()
                .numeroCuenta(request.getNumeroCuenta())
                .tipoCuenta(request.getTipoCuenta())
                .saldoInicial(request.getSaldoInicial())
                .saldoDisponible(request.getSaldoDisponible())
                .clienteId(request.getClienteId())
                .estado("ACTIVA")
                .build();
    }
}
