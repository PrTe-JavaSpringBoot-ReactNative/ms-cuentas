/**
 * Paquete de mensajería asíncrona con RabbitMQ.
 *
 * Clases a implementar en este paquete:
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  ClienteEventoConsumer  (Consumer)                                  │
 * │                                                                     │
 * │  Escucha eventos de cambio de estado de clientes publicados        │
 * │  por ms-clientes. Reacciona bloqueando cuentas si el cliente       │
 * │  fue desactivado.                                                   │
 * │                                                                     │
 * │  Queue : clientes.eventos.cuentas                                   │
 * │                                                                     │
 * │  @Component                                                         │
 * │  public class ClienteEventoConsumer {                               │
 * │      @RabbitListener(queues =                                       │
 * │          RabbitMQConfig.CLIENTES_EVENTOS_CUENTAS_QUEUE)             │
 * │      public void manejarEvento(ClienteEventoDTO evento) {           │
 * │          // reaccionar al cambio de estado del cliente              │
 * │      }                                                              │
 * │  }                                                                  │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  ClienteValidacionPublisher  (Producer)                             │
 * │                                                                     │
 * │  Publica una solicitud de validación a ms-clientes antes de        │
 * │  crear una cuenta, para verificar que el clienteId existe.         │
 * │                                                                     │
 * │  Exchange : clientes.events                                         │
 * │  Routing  : cliente.validacion.solicitud                            │
 * │                                                                     │
 * │  @Component                                                         │
 * │  public class ClienteValidacionPublisher {                          │
 * │      @Autowired RabbitTemplate rabbitTemplate;                      │
 * │      public void solicitarValidacion(Long clienteId) {              │
 * │          rabbitTemplate.convertAndSend(                             │
 * │              RabbitMQConfig.CLIENTES_EXCHANGE,                      │
 * │              RabbitMQConfig.CLIENTE_VALIDACION_ROUTING_KEY,         │
 * │              clienteId);                                            │
 * │      }                                                              │
 * │  }                                                                  │
 * └─────────────────────────────────────────────────────────────────────┘
 */
package com.example.cuentas.messaging;
