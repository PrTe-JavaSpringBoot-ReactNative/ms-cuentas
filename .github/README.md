# ms-cuentas — Microservicio de Cuentas y Movimientos

Microservicio responsable del dominio **Cuenta** y **Movimiento** dentro de la arquitectura de la Prueba Técnica de Microservicios (2023).

---

## 🎯 Responsabilidad de este microservicio

Este servicio es el dueño exclusivo de los datos de cuentas y movimientos. Para operar necesita saber a qué cliente pertenece cada cuenta, pero **no accede directamente a la base de datos de ms-clientes**. La validación de clientes se realiza mediante mensajería asíncrona con RabbitMQ.

**Entidades de dominio a implementar:**
- `Cuenta` → número de cuenta, tipo, saldo inicial, estado, clienteId (referencia lógica)
- `Movimiento` → fecha, tipo de movimiento, valor, saldo resultante

---

## 🛠️ Tecnologías

| Tecnología | Versión | Rol |
|---|---|---|
| **Java** | 17 (LTS) | Lenguaje principal |
| **Spring Boot** | 3.2.5 | Framework |
| **Spring Data JPA / Hibernate** | (incluido) | ORM / acceso a datos |
| **Spring AMQP** | (incluido) | Cliente RabbitMQ |
| **PostgreSQL** | 15 | Base de datos (producción) |
| **H2** | (incluido) | Base de datos en tests |
| **Lombok** | (incluido) | Reducción de boilerplate |
| **JUnit 5 + Mockito + MockMvc** | (incluido) | Testing |

---

## 📁 Estructura del proyecto

```
ms-cuentas/
├── src/
│   ├── main/
│   │   ├── java/com/example/cuentas/
│   │   │   ├── MsCuentasApplication.java        # Entry point
│   │   │   ├── controller/
│   │   │   │   └── HealthController.java         # ✅ GET /api/health
│   │   │   │   # TODO: CuentaController          → /api/cuentas
│   │   │   │   # TODO: MovimientoController      → /api/movimientos
│   │   │   │   # TODO: ReporteController         → /api/reportes
│   │   │   ├── service/
│   │   │   │   # TODO: CuentaService             → lógica de negocio
│   │   │   │   # TODO: MovimientoService         → registro y validación de saldo
│   │   │   │   # TODO: ReporteService            → estado de cuenta por fechas
│   │   │   ├── repository/
│   │   │   │   # TODO: CuentaRepository          → extends JpaRepository
│   │   │   │   # TODO: MovimientoRepository      → extends JpaRepository
│   │   │   ├── entity/
│   │   │   │   # TODO: Cuenta.java               → @Entity
│   │   │   │   # TODO: Movimiento.java           → @Entity, @ManyToOne Cuenta
│   │   │   ├── dto/
│   │   │   │   # TODO: CuentaRequestDTO / CuentaResponseDTO
│   │   │   │   # TODO: MovimientoRequestDTO / MovimientoResponseDTO
│   │   │   │   # TODO: EstadoCuentaDTO           → respuesta del /reportes
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java   # ✅ Manejador global
│   │   │   │   # TODO: SaldoInsuficienteException
│   │   │   ├── config/
│   │   │   │   └── RabbitMQConfig.java           # ✅ Exchange, queues, bindings
│   │   │   └── messaging/
│   │   │       # TODO: ClienteEventoConsumer     → reacciona a cambios de cliente
│   │   │       # TODO: ClienteValidacionPublisher → solicita validar clienteId
│   │   └── resources/
│   │       └── application.yml                   # ✅ Config PostgreSQL + RabbitMQ
│   └── test/
│       ├── java/com/example/cuentas/
│       │   ├── controller/
│       │   │   └── HealthControllerTest.java     # ✅ Tests del /health
│       │   └── service/
│       │       # TODO: MovimientoServiceTest     → validar lógica de saldo
│       └── resources/
│           └── application.yml                   # ✅ Config H2 para tests
├── .github/
│   └── README.md                                 # Este archivo
├── Dockerfile                                    # ✅ Multi-stage build
├── pom.xml                                       # ✅ Dependencias Maven
└── .gitignore
```

---

## 🏗️ Decisiones de arquitectura

### Referencia lógica a Cliente (sin FK entre bases de datos)

`Cuenta` necesita saber a qué cliente pertenece, pero como cada microservicio tiene su propia base de datos, **no es posible una FK a la tabla de clientes**. La solución es guardar el `clienteId` como un campo simple (Long) en la entidad `Cuenta`:

```java
@Entity
@Table(name = "cuentas")
public class Cuenta {
    // ...
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId; // referencia lógica, no FK de base de datos
}
```

La validación de que ese `clienteId` existe se realiza vía RabbitMQ antes de persistir.

### Lógica de saldo en Movimientos

Al registrar un movimiento:
1. Se obtiene el saldo actual de la cuenta.
2. Se calcula el nuevo saldo: `saldoActual + valorMovimiento` (puede ser negativo para retiros).
3. Si el nuevo saldo < 0 → lanzar `SaldoInsuficienteException` → mensaje "Saldo no disponible".
4. Si es válido → persistir el movimiento y actualizar el saldo de la cuenta.

### Reporte de estado de cuenta

El endpoint `/api/reportes?fecha=fechaInicio,fechaFin&clienteId=X` debe retornar las cuentas del cliente con sus movimientos en el rango de fechas. Toda la lógica va en `ReporteService`.

---

## 📨 Mensajería asíncrona (RabbitMQ)

```
ms-cuentas (CONSUMER) ◀── Queue: clientes.eventos.cuentas
                               Producer: ms-clientes
                               Payload: { clienteId, nuevoEstado }
                               Acción: si cliente desactivado → bloquear cuentas

ms-cuentas (PRODUCER) ──▶ Exchange: clientes.events
                                 │
                                 └──▶ [cliente.validacion.solicitud]
                                           └──▶ Queue: clientes.solicitudes.validacion
                                                     Consumer: ms-clientes
                                                     Valida si clienteId existe
```

**Clases a implementar en `messaging/`:**
- `ClienteEventoConsumer` → escucha cambios de estado del cliente
- `ClienteValidacionPublisher` → solicita validación de clienteId a ms-clientes

---

## 🚀 Levantar solo este microservicio (desarrollo local)

```bash
# Desde la raíz del monorepo
docker compose up postgres-cuentas rabbitmq -d

cd ms-cuentas
mvn spring-boot:run
```

La app estará en: `http://localhost:8081/api`

---

## 🧪 Ejecutar tests

```bash
cd ms-cuentas
mvn test
```

No requiere Docker (usa H2 en memoria).

---

## 🔌 Endpoints

| Método | Endpoint | Descripción | Estado |
|---|---|---|---|
| GET | `/api/health` | Verificación de vida | ✅ Implementado |
| GET | `/api/cuentas` | Listar cuentas | 🔲 Pendiente |
| GET | `/api/cuentas/{id}` | Obtener cuenta | 🔲 Pendiente |
| POST | `/api/cuentas` | Crear cuenta | 🔲 Pendiente |
| PUT | `/api/cuentas/{id}` | Actualizar cuenta | 🔲 Pendiente |
| DELETE | `/api/cuentas/{id}` | Eliminar cuenta | 🔲 Pendiente |
| GET | `/api/movimientos` | Listar movimientos | 🔲 Pendiente |
| POST | `/api/movimientos` | Registrar movimiento | 🔲 Pendiente |
| GET | `/api/reportes` | Estado de cuenta por fechas | 🔲 Pendiente |

---

## ⚙️ Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `DB_HOST` | `postgres-cuentas` | Host PostgreSQL |
| `DB_PORT` | `5432` | Puerto PostgreSQL |
| `DB_NAME` | `cuentas_db` | Nombre de la DB |
| `DB_USER` | `postgres` | Usuario DB |
| `DB_PASSWORD` | `postgres` | Contraseña DB |
| `RABBITMQ_HOST` | `rabbitmq` | Host RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Puerto AMQP |
| `RABBITMQ_USER` | `guest` | Usuario RabbitMQ |
| `RABBITMQ_PASSWORD` | `guest` | Contraseña RabbitMQ |
| `SERVER_PORT` | `8081` | Puerto HTTP |


```
Usuario                ms-cuentas              RabbitMQ            ms-clientes
  │                        │                       │                     │
  │── POST /cuentas ───────▶│                       │                     │
  │                        │── publica mensaje ────▶│                     │
  │                        │   { clienteId: 5,      │                     │
  │                        │     cuentaId: 10,      │── entrega ─────────▶│
  │                        │     accion: VALIDAR }  │                     │
  │◀─ 201 CREATED ─────────│                        │                     │
  │   (cuenta guardada)     │                        │   ms-clientes busca │
  │                        │                        │   el clienteId y    │
  │                        │◀─ respuesta ───────────│◀─ publica resultado │
  │                        │   { clienteId: 5,      │                     │
  │                        │     existe: false }    │                     │
  │                        │                        │                     │
  │                        │ (si no existe: marcar  │                     │
  │                        │  cuenta como INVALIDA  │                     │
  │                        │  o eliminarla)         │                     │
  ```