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
 * Topología de mensajería (espejo de la vista desde ms-cuentas):
 *
 *   ms-cuentas  ──(producer)──▶  Exchange: clientes.events
 *                                      │
 *                                      └──▶  Queue: clientes.solicitudes.validacion
 *                                                 │
 *                                           ms-clientes (consumer)
 *                                           valida si el clienteId existe
 *
 *   ms-cuentas  ◀─(consumer)──  Queue: clientes.eventos.cuentas
 *                                      │
 *                                 ms-clientes (producer)
 *                                 notifica cambios de estado de clientes
 *
 *   ms-cuentas  ──(producer)──▶  Exchange: cuentas.events
 *                                      │
 *                                      └──▶  Queue: cuentas.eventos  (uso futuro)
 *
 * Nota: ms-cuentas declara también las colas de clientes para garantizar
 * que existan en RabbitMQ independientemente del orden de arranque.
 * RabbitMQ es idempotente en la declaración de exchanges/queues.
 */
@Configuration
public class RabbitMQConfig {

    // ── Exchanges ──────────────────────────────────────────────────────────

    /** Exchange del microservicio de clientes (declarado también aquí por idempotencia) */
    public static final String CLIENTES_EXCHANGE = "clientes.events";

    /** Exchange propio del microservicio de cuentas */
    public static final String CUENTAS_EXCHANGE = "cuentas.events";

    // ── Queues ─────────────────────────────────────────────────────────────

    /** Cola que ms-cuentas CONSUME: eventos de cambios en clientes */
    public static final String CLIENTES_EVENTOS_CUENTAS_QUEUE = "clientes.eventos.cuentas";

    /** Cola donde ms-cuentas PUBLICA: solicitudes de validación de clienteId */
    public static final String CLIENTES_VALIDACION_QUEUE = "clientes.solicitudes.validacion";

    /** Cola de eventos propios de cuentas (para uso futuro o extensiones) */
    public static final String CUENTAS_EVENTOS_QUEUE = "cuentas.eventos";

    // ── Routing Keys ───────────────────────────────────────────────────────

    public static final String CLIENTE_EVENTO_ROUTING_KEY = "cliente.evento.#";
    public static final String CLIENTE_VALIDACION_ROUTING_KEY = "cliente.validacion.solicitud";
    public static final String CUENTA_EVENTO_ROUTING_KEY = "cuenta.evento.#";

    // ── Beans: Exchanges ───────────────────────────────────────────────────

    @Bean
    public TopicExchange clientesExchange() {
        return ExchangeBuilder
                .topicExchange(CLIENTES_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange cuentasExchange() {
        return ExchangeBuilder
                .topicExchange(CUENTAS_EXCHANGE)
                .durable(true)
                .build();
    }

    // ── Beans: Queues ──────────────────────────────────────────────────────

    @Bean
    public Queue clientesEventosCuentasQueue() {
        return QueueBuilder
                .durable(CLIENTES_EVENTOS_CUENTAS_QUEUE)
                .build();
    }

    @Bean
    public Queue clientesValidacionQueue() {
        return QueueBuilder
                .durable(CLIENTES_VALIDACION_QUEUE)
                .build();
    }

    @Bean
    public Queue cuentasEventosQueue() {
        return QueueBuilder
                .durable(CUENTAS_EVENTOS_QUEUE)
                .build();
    }

    // ── Beans: Bindings ────────────────────────────────────────────────────

    @Bean
    public Binding bindingClientesEventosCuentas() {
        return BindingBuilder
                .bind(clientesEventosCuentasQueue())
                .to(clientesExchange())
                .with(CLIENTE_EVENTO_ROUTING_KEY);
    }

    @Bean
    public Binding bindingClientesValidacion() {
        return BindingBuilder
                .bind(clientesValidacionQueue())
                .to(clientesExchange())
                .with(CLIENTE_VALIDACION_ROUTING_KEY);
    }

    @Bean
    public Binding bindingCuentasEventos() {
        return BindingBuilder
                .bind(cuentasEventosQueue())
                .to(cuentasExchange())
                .with(CUENTA_EVENTO_ROUTING_KEY);
    }

    // ── Beans: Serialización ───────────────────────────────────────────────

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
