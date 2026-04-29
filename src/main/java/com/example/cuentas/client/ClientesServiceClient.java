package com.example.cuentas.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientesServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ms.clientes.url:http://localhost:8080/api}")
    private String clientesBaseUrl;

    /**
     * @param clienteId ID del cliente a validar
     * @return CompletableFuture<Boolean> true si cliente está activo, false si no existe/inactivo
     */
    @Async("asyncExecutor")
    public CompletableFuture<Boolean> validarClienteActivoAsync(Long clienteId) {
        try {
            log.debug("Validando asincronicamente cliente {} desde ms-cuentas", clienteId);

            String url = clientesBaseUrl + "/clientes/" + clienteId;
            var response = restTemplate.getForObject(url, ClienteDTO.class);

            if (response == null) {
                log.warn("Respuesta nula al validar cliente {}", clienteId);
                return CompletableFuture.completedFuture(false);
            }

            boolean activo = "ACTIVO".equalsIgnoreCase(response.getEstado());
            log.debug("Cliente {} validado. Estado: {}, Activo: {}",
                    clienteId, response.getEstado(), activo);

            return CompletableFuture.completedFuture(activo);
        } catch (RestClientException e) {
            log.error("Error al validar cliente {}: {}", clienteId, e.getMessage());
            return CompletableFuture.failedFuture(
                    new RuntimeException("No se pudo validar el cliente " + clienteId +
                            ". Verifique que ms-clientes está disponible", e)
            );
        }
    }

    /**
     * ASINCRÓNICO: Obtiene datos básicos del cliente para generar reportes.
     * Ejecuta en thread pool separado, liberando el hilo HTTP del servidor.
     *
     * @param clienteId ID del cliente a obtener
     * @return CompletableFuture<ClienteDTO> con datos del cliente
     */
    @Async("asyncExecutor")
    public CompletableFuture<ClienteDTO> obtenerClientePorIdAsync(Long clienteId) {
        try {
            log.debug("Obteniendo asincronicamente datos del cliente {}", clienteId);

            String url = clientesBaseUrl + "/clientes/" + clienteId;
            ClienteDTO cliente = restTemplate.getForObject(url, ClienteDTO.class);

            if (cliente == null) {
                return CompletableFuture.failedFuture(
                        new RuntimeException("Cliente " + clienteId + " no encontrado")
                );
            }

            log.debug("Cliente {} obtenido: {}", clienteId, cliente.getNombre());
            return CompletableFuture.completedFuture(cliente);
        } catch (RestClientException e) {
            log.error("Error al obtener cliente {}: {}", clienteId, e.getMessage());
            return CompletableFuture.failedFuture(
                    new RuntimeException("No se pudo obtener el cliente " + clienteId, e)
            );
        }
    }

    /**
     * SÍNCRONO (legacy): Valida si un cliente existe y está activo en ms-clientes.
     * Método bloqueante, usado solo internamente para compatibilidad.
     *
     * @deprecated Use validarClienteActivoAsync() para no bloquear hilos HTTP
     */
    @Deprecated
    public boolean validarClienteActivo(Long clienteId) {
        return validarClienteActivoAsync(clienteId).join();
    }

    /**
     * SÍNCRONO (legacy): Obtiene datos básicos del cliente.
     * Método bloqueante, usado solo internamente para compatibilidad.
     *
     * @deprecated Use obtenerClientePorIdAsync() para no bloquear hilos HTTP
     */
    @Deprecated
    public ClienteDTO obtenerClientePorId(Long clienteId) {
        return obtenerClientePorIdAsync(clienteId).join();
    }

    /**
     * DTO para recibir datos de cliente desde ms-clientes.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ClienteDTO {
        private Long clienteId;
        private String nombre;
        private String estado;
        private String identificacion;
    }
}
