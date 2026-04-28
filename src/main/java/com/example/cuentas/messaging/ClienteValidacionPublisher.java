package com.example.cuentas.messaging;

import com.example.cuentas.config.RabbitMQConfig;
import com.example.cuentas.dto.ClienteValidacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer de ms-cuentas.
 *
 * Responsabilidad: publicar una solicitud de validación en RabbitMQ
 * cada vez que se crea una cuenta nueva, para que ms-clientes confirme
 * si el clienteId recibido realmente existe.
 *
 * Flujo:
 *   CuentaService.crearCuenta()
 *       └──▶ ClienteValidacionPublisher.solicitarValidacion()
 *                └──▶ RabbitMQ [clientes.solicitudes.validacion]
 *                         └──▶ ms-clientes (ClienteValidacionConsumer)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteValidacionPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica un mensaje solicitando que ms-clientes valide si el clienteId existe.
     *
     * @param clienteId    ID del cliente que se quiere asociar a la cuenta
     * @param cuentaId     ID interno de la cuenta recién guardada
     * @param numeroCuenta Número de cuenta (para trazabilidad en logs)
     */
    public void solicitarValidacion(Long clienteId, Long cuentaId, String numeroCuenta) {

        ClienteValidacionDTO mensaje = ClienteValidacionDTO.builder()
                .clienteId(clienteId)
                .cuentaId(cuentaId)
                .numeroCuenta(numeroCuenta)
                .clienteExiste(null) // aún no se sabe, ms-clientes lo completará
                .build();

        log.info("Publicando solicitud de validación para clienteId={}, cuentaId={}",
                clienteId, cuentaId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CLIENTES_EXCHANGE,
                RabbitMQConfig.CLIENTE_VALIDACION_ROUTING_KEY,
                mensaje
        );

        log.info("Solicitud de validación publicada exitosamente");
    }
}
