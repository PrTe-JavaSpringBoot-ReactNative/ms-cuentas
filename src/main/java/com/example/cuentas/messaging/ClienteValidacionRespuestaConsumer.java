package com.example.cuentas.messaging;

import com.example.cuentas.config.RabbitMQConfig;
import com.example.cuentas.dto.ClienteValidacionDTO;
import com.example.cuentas.entity.Cuenta;
import com.example.cuentas.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Consumer de ms-cuentas.
 *
 * Responsabilidad: escuchar la respuesta de ms-clientes sobre si
 * el clienteId de una cuenta recién creada es válido o no, y actuar
 * en consecuencia.
 *
 * Flujo:
 *   RabbitMQ [cuentas.validacion.respuesta]
 *       └──▶ ClienteValidacionRespuestaConsumer.manejarRespuesta()
 *                ├── clienteExiste = true  → no hace nada, la cuenta ya está activa
 *                └── clienteExiste = false → marca la cuenta como INVALIDA
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteValidacionRespuestaConsumer {

    private final CuentaRepository cuentaRepository;

    /**
     * Escucha la cola de respuestas de validación publicadas por ms-clientes.
     *
     * @param respuesta DTO con el resultado de la validación
     */
    @RabbitListener(queues = RabbitMQConfig.CUENTAS_VALIDACION_RESPUESTA_QUEUE)
    @Transactional
    public void manejarRespuesta(ClienteValidacionDTO respuesta) {

        log.info("Respuesta de validación recibida: clienteId={}, cuentaId={}, existe={}",
                respuesta.getClienteId(),
                respuesta.getCuentaId(),
                respuesta.getClienteExiste());

        if (Boolean.FALSE.equals(respuesta.getClienteExiste())) {

            // El clienteId no existe en ms-clientes
            // Buscamos la cuenta por su ID y la marcamos como INVALIDA
            Optional<Cuenta> cuentaOpt = cuentaRepository.findById(respuesta.getCuentaId());

            if (cuentaOpt.isPresent()) {
                Cuenta cuenta = cuentaOpt.get();
                cuenta.setEstado("INVALIDA");
                cuentaRepository.save(cuenta);

                log.warn("Cuenta {} marcada como INVALIDA: clienteId={} no existe en ms-clientes",
                        respuesta.getNumeroCuenta(),
                        respuesta.getClienteId());
            } else {
                log.error("No se encontró la cuenta con ID={} para marcarla como inválida",
                        respuesta.getCuentaId());
            }

        } else {
            log.info("Validación exitosa: clienteId={} confirmado para cuenta={}",
                    respuesta.getClienteId(),
                    respuesta.getNumeroCuenta());
        }
    }
}
