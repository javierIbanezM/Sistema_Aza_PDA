# Examples (Java)

Casos de uso completos que combinan varios documentos de este dominio. Cada ejemplo enlaza lo que aplica en lugar de repetirlo.

---

## Ejemplo 1 — Endpoint REST completo con Clean Architecture

**Contexto**: exponer `POST /api/v1/pedidos` para crear un pedido, aplicando Clean Architecture, DTOs propios, manejo de errores y tests.

**Flujo aplicado**:

1. Dominio (`domain/`): `Pedido` con comportamiento propio (`agregarLinea()`, `confirmar()`) — ver [`../architecture/ddd.md`](../architecture/ddd.md).
2. Puerto y caso de uso (`application/`): `PedidoRepository` (interfaz) y `CrearPedidoUseCase`, inyectado por constructor — ver [`spring.md`](../spring/spring-core.md).
3. Adaptador de persistencia (`infrastructure/`): `PedidoRepositoryJpa` implementando el puerto sobre Spring Data JPA — ver [`jpa.md`](../hibernate/jpa.md).
4. DTOs (`interface/dto/`): `CrearPedidoRequest` con validación (`@Valid`, `@NotNull`), `PedidoResponse` sin campos internos — ver [`../../standards/dto.md`](../../standards/dto.md).
5. Mapper: `PedidoMapper` (MapStruct) entre DTO y dominio — ver [`mapstruct.md`](../mapstruct/mapstruct.md).
6. Controlador (`interface/`): `PedidoController` delegando en el caso de uso, sin lógica de negocio.
7. Manejo de errores: `@RestControllerAdvice` mapeando `PedidoSinLineasException` a `422`, siguiendo el formato de [`../../standards/api.md`](../../standards/api.md).
8. Tests: unitarios del caso de uso con Mockito (repositorio fake/mock), integration test del repositorio JPA con Testcontainers — ver [`junit.md`](../junit/junit.md), [`mockito.md`](../mockito/mockito.md).

**Recipe asociada**: [`../../recipes/java-create-rest-controller.md`](../../recipes/java-create-rest-controller.md).

---

## Ejemplo 2 — Corregir un N+1 Query detectado en producción

**Contexto**: `GET /api/v1/pedidos` empieza a tardar segundos con clientes de muchos pedidos históricos.

**Flujo aplicado**:

1. Activar `spring.jpa.show-sql=true` en staging, reproducir con volumen de datos representativo — ver [`performance.md`](./performance.md).
2. Confirmar el patrón: 1 query para pedidos + N queries para clientes asociados — coincide con [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md).
3. Corregir con `JOIN FETCH` explícito en el repositorio Spring Data — ver [`jpa.md`](../hibernate/jpa.md).
4. Medir de nuevo con la misma metodología: de N+1 a 1 query, latencia reducida.
5. Añadir un test de integración que falle si el número de queries vuelve a crecer (assertion sobre el contador de queries de Hibernate en el test, o revisión manual del log de SQL en el PR).

---

## Ejemplo 3 — Refactor de un servicio con exceso de responsabilidades

**Contexto**: `PedidoService` de 600 líneas gestiona creación, pago, notificación y actualización de inventario.

**Flujo aplicado** (siguiendo [`../../prompts/refactor.md`](../../prompts/refactor.md)):

1. Identificar el anti-patrón [God Object](../../anti-patterns/god-object.md).
2. Escribir tests de caracterización sobre el comportamiento actual antes de tocar nada — ver [`../architecture/refactoring.md`](../architecture/refactoring.md).
3. Extraer `CrearPedidoUseCase`, `ProcesarPagoPedidoUseCase`, `NotificadorPedidoEmail`, `ActualizadorInventario`, cada uno con constructor injection (ver [`spring.md`](../spring/spring-core.md)).
4. Comunicar entre ellos con un evento de dominio `PedidoConfirmadoEvent` publicado vía `ApplicationEventPublisher` de Spring, en lugar de llamadas directas encadenadas — ver [`../../patterns/observer.md`](../../patterns/observer.md).
5. Verificar con [`../../checklists/architecture.md`](../../checklists/architecture.md) que cada clase resultante tiene un único motivo de cambio.

---

## Ejemplo 4 — Migrar de un DTO anémico a un dominio rico

**Contexto**: `Pedido` es actualmente una clase con solo getters/setters; toda la validación vive dispersa en varios `@Service`.

**Flujo aplicado**:

1. Identificar el anti-patrón [Anemic Domain Model](../../anti-patterns/anemic-domain-model.md).
2. Mover `confirmar()`, `agregarLinea()` y sus invariantes a la propia clase `Pedido`, eliminando setters públicos — ver [`../architecture/ddd.md`](../architecture/ddd.md) y [`lombok.md`](../lombok/lombok.md) (evitar `@Data`).
3. Actualizar los `@Service` para invocar los métodos de negocio de `Pedido` en lugar de mutar campos directamente.
4. Añadir tests unitarios directamente sobre `Pedido` (sin mocks, sin Spring) que verifican las invariantes ahora protegidas por la propia entidad.
