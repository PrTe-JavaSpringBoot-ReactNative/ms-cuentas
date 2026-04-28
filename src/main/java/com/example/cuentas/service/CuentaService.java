package com.example.cuentas.service;

import com.example.cuentas.dto.CuentaResponseDTO;
import com.example.cuentas.dto.CuentaRequestDTO;
import com.example.cuentas.entity.Cuenta;
import com.example.cuentas.repository.CuentaRepository;
import com.example.cuentas.exception.CuentaNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaService {

    private final CuentaRepository cuentaRepository;

    public List<CuentaResponseDTO> obtenerTodasLasCuentas() {
        log.info("Obteniendo todas las cuentas...");
        // Lógica para obtener las cuentas desde el repositorio y transformarlas a DTOs
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
        return new entityToResponseDTO(cuenta);
    }

    @Transactional
    public CuentaResponseDTO crearCuenta(CuentaRequestDTO cuentaRequest) {
        log.info("Creando una nueva cuenta para el cuenta con numero de cuenta: {}", cuentaRequest.getNumeroCuenta());
        if(cuentaRepository.findByNumeroCuenta(cuentaRequest.getNumeroCuenta()).isPresent()) {
            log.warn("Número de cuenta {} ya existe", cuentaRequest.getNumeroCuenta());
            throw new CuentaYaExisteException("Cuenta con número de cuenta " + cuentaRequest.getNumeroCuenta() + " ya existe");
        }
        Cuenta cuenta = requestDTOToEntity(cuentaRequest);
        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);

        return new entityToResponseDTO(cuentaGuardada);
    }

    @Transactional
    public CuentaResponseDTO actualizarCuenta(String numeroCuenta, CuentaRequestDTO cuentaRequest) {
        log.info("Actualizando cuenta con número: {}", numeroCuenta);
        
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(cuentaRequest.getNumeroCuenta())
                .orElseThrow(() -> {
                    log.warn("Cuenta con numero {} no encontrado", cuentaRequest.getNumeroCuenta());
                    return new CuentaNotFoundException(cuentaRequest.getNumeroCuenta());
                });

        cuenta.setTipoCuenta(cuentaRequest.getTipoCuenta());
        cuenta.setSaldoInicial(cuentaRequest.getSaldoInicial());
        cuenta.setSaldoDisponible(cuentaRequest.getSaldoDisponible());
        cuenta.setCliente(cuentaRequest.getClienteId());

        Cuenta cuentaActualizada = cuentaRepository.save(cuenta);

        return new entityToResponseDTO(cuentaActualizada);
    }

    @Transactional
    public void eliminarCuenta(String numeroCuenta) {
        log.info("Eliminando cuenta con número: {}", numeroCuenta);
        if (!cuentaRepository.findByNumeroCuenta(numeroCuenta).isPresent()) {
            log.warn("Cuenta con número {} no encontrada para eliminación", numeroCuenta);
            return new CuentaNotFoundException(numeroCuenta);
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
                .clienteId(cuenta.getCliente().getId())
                .build();
    }

    private Cuenta requestDTOToEntity(CuentaRequestDTO cuentaResponseDTO) {
        return Cuenta.builder()
                .numeroCuenta(cuentaResponseDTO.getNumeroCuenta())
                .tipoCuenta(cuentaResponseDTO.getTipoCuenta())
                .saldoInicial(cuentaResponseDTO.getSaldoInicial())
                .saldoDisponible(cuentaResponseDTO.getSaldoDisponible())
                .clienteId(cuentaResponseDTO.getClienteId())
                .build();
    }
}