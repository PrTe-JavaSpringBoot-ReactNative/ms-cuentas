package com.example.cuentas.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para ms-cuentas.
 *
 * Topología completa de mensajería:
 *
 *  [SOLICITUD DE VALIDACIÓN]
 *  ms-cuentas (producer) ──▶ Exchange: clientes.events
 *                                  └──▶ Queue: clientes.solicitudes.validacion
 *                                             └──▶ ms-clientes (consumer)
 *
 *  [RESPUESTA DE VALIDACIÓN]
 *  ms-clientes (producer) ──▶ Exchange: cuentas.events
 *                                  └──▶ Queue: cuentas.validacion.respuesta
 *                                             └──▶ ms-cuentas (consumer)
 */
@Configuration
public class RabbitMQConfig {

    // ── Exchanges ──────────────────────────────────────────────────────────

    public static final String CLIENTES_EXCHANGE = "clientes.events";
    public static final String CUENTAS_EXCHANGE = "cuentas.events";

    // ── Queues ─────────────────────────────────────────────────────────────

    /** ms-cuentas PUBLICA aquí: solicitudes de validación hacia ms-clientes */
    public static final String CLIENTES_VALIDACION_QUEUE = "clientes.solicitudes.validacion";

    /** ms-cuentas CONSUME aquí: respuestas de validación que envía ms-clientes */
    public static final String CUENTAS_VALIDACION_RESPUESTA_QUEUE = "cuentas.validacion.respuesta";

    /** Cola de eventos de clientes (cambios de estado) - para uso futuro */
    public static final String CLIENTES_EVENTOS_CUENTAS_QUEUE = "clientes.eventos.cuentas";

    // ── Routing Keys ───────────────────────────────────────────────────────

    public static final String CLIENTE_VALIDACION_ROUTING_KEY = "cliente.validacion.solicitud";
    public static final String CUENTA_VALIDACION_RESPUESTA_ROUTING_KEY = "cuenta.validacion.respuesta";
    public static final String CLIENTE_EVENTO_ROUTING_KEY = "cliente.evento.#";

    // ── Beans: Exchanges ───────────────────────────────────────────────────

    @Bean
    public TopicExchange clientesExchange() {
        return ExchangeBuilder.topicExchange(CLIENTES_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange cuentasExchange() {
        return ExchangeBuilder.topicExchange(CUENTAS_EXCHANGE).durable(true).build();
    }

    // ── Beans: Queues ──────────────────────────────────────────────────────

    @Bean
    public Queue clientesValidacionQueue() {
        return QueueBuilder.durable(CLIENTES_VALIDACION_QUEUE).build();
    }

    @Bean
    public Queue cuentasValidacionRespuestaQueue() {
        return QueueBuilder.durable(CUENTAS_VALIDACION_RESPUESTA_QUEUE).build();
    }

    @Bean
    public Queue clientesEventosCuentasQueue() {
        return QueueBuilder.durable(CLIENTES_EVENTOS_CUENTAS_QUEUE).build();
    }

    // ── Beans: Bindings ────────────────────────────────────────────────────

    /** ms-cuentas publica solicitudes de validación en este exchange/routing key */
    @Bean
    public Binding bindingClientesValidacion() {
        return BindingBuilder
                .bind(clientesValidacionQueue())
                .to(clientesExchange())
                .with(CLIENTE_VALIDACION_ROUTING_KEY);
    }

    /** ms-cuentas escucha las respuestas de validación en su propio exchange */
    @Bean
    public Binding bindingCuentasValidacionRespuesta() {
        return BindingBuilder
                .bind(cuentasValidacionRespuestaQueue())
                .to(cuentasExchange())
                .with(CUENTA_VALIDACION_RESPUESTA_ROUTING_KEY);
    }

    /** ms-cuentas escucha eventos de cambio de estado de clientes */
    @Bean
    public Binding bindingClientesEventosCuentas() {
        return BindingBuilder
                .bind(clientesEventosCuentasQueue())
                .to(clientesExchange())
                .with(CLIENTE_EVENTO_ROUTING_KEY);
    }

    // ── Serialización ──────────────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
