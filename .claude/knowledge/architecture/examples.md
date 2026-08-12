# Ejemplos — Arquitectura de Software

Casos de uso completos que muestran cómo se combinan la documentación, los patrones, los anti-patrones y los prompts de esta Skill en tareas reales. Cada ejemplo referencia los documentos aplicados en lugar de reexplicar su contenido.

---

## Ejemplo 1 — Diseñar un módulo nuevo con Clean Architecture

**Contexto**: se pide implementar la gestión de reservas de un sistema de restaurantes desde cero.

**Flujo aplicado**:

1. Se identifica que "Reservas" es un Bounded Context propio, distinto de "Inventario" o "Facturación" (ver [`ddd.md`](./ddd.md)).
2. Se modela el dominio: `Reserva` como Raíz de Agregado, con invariantes propias (no se puede reservar una mesa ya ocupada en ese horario, una reserva no puede confirmarse sin al menos un comensal).
3. Se define el puerto de salida `ReservaRepository` en la capa de Application, y el caso de uso `CrearReservaUseCase` que lo consume (ver [`clean-architecture.md`](./clean-architecture.md)).
4. Se implementa el adaptador `ReservaRepositoryTypeOrm` en Infrastructure, y el controlador REST en Interface, con DTOs propios distintos de la entidad de dominio.
5. Antes de cerrar la tarea, se pasa el [`checklists/architecture.md`](../../checklists/architecture.md): se confirma que `domain/` no importa nada de TypeORM/Express, que existen tests de caso de uso con Fakes de `ReservaRepository`, y que no hay indicios de [Anemic Domain Model](../../anti-patterns/anemic-domain-model.md) (la entidad `Reserva` valida sus propias invariantes, no un servicio externo).

**Documentos consultados**: [`ddd.md`](./ddd.md), [`clean-architecture.md`](./clean-architecture.md), [`patterns/Repository.md`](../../patterns/repository.md), [`testing.md`](./testing.md).

---

## Ejemplo 2 — Modelado DDD de un dominio de pedidos con descuentos variables

**Contexto**: el negocio pide soportar múltiples políticas de descuento (regular, VIP, empleado) que crecerán con el tiempo según nuevas campañas comerciales confirmadas en el roadmap.

**Flujo aplicado**:

1. Se identifica que el número de políticas de descuento va a crecer de forma confirmada (no especulativa) — esto justifica aplicar el patrón Strategy en lugar de un `if-else` simple (ver [`dry-kiss-yagni.md`](./dry-kiss-yagni.md) sobre cuándo SÍ anticiparse está justificado).
2. Se define la interfaz `PoliticaDescuento` y una implementación por cada tipo existente, siguiendo [`patterns/Strategy.md`](../../patterns/strategy.md).
3. `Dinero` se modela como Value Object (ver [`ddd.md`](./ddd.md)) en lugar de un `BigDecimal` desnudo, para encapsular la invariante "el monto nunca es negativo" y evitar que se valide de forma dispersa en cada política de descuento.
4. Se generan tests unitarios por cada política de descuento de forma aislada, siguiendo [`prompts/generate-tests.md`](../../prompts/generate-tests.md): camino feliz (descuento aplicado correctamente), caso límite (pedido con total cero).

**Documentos consultados**: [`ddd.md`](./ddd.md), [`patterns/Strategy.md`](../../patterns/strategy.md), [`dry-kiss-yagni.md`](./dry-kiss-yagni.md), [`testing.md`](./testing.md).

---

## Ejemplo 3 — Plan de refactor de un "God Service"

**Contexto**: existe una clase `PedidoManager` de más de 800 líneas que gestiona creación de pedidos, envío de emails, facturación, actualización de inventario y generación de reportes, y cada sprint alguien tiene que tocarla por un motivo distinto, generando conflictos de merge constantes.

**Flujo aplicado** (siguiendo [`prompts/refactor.md`](../../prompts/refactor.md)):

1. Se identifica el anti-patrón [God Object](../../anti-patterns/god-object.md): la clase acumula responsabilidades de al menos cinco actores distintos (negocio de pedidos, comunicaciones, contabilidad, inventario, BI).
2. Se escriben tests de caracterización sobre el comportamiento actual de `PedidoManager` antes de tocar nada (ver [`refactoring.md`](./refactoring.md)), dado el alto riesgo de una clase con tanta superficie.
3. Se agrupan los métodos por motivo de cambio y se extraen, uno por uno, verificando tests tras cada extracción:
   - `CrearPedidoUseCase` / `ConfirmarPedidoUseCase` (lógica de negocio de pedidos).
   - `NotificadorPedidoEmail` (comunicaciones).
   - `ServicioFacturacion` (contabilidad).
   - `ServicioInventario` (stock).
4. Se sustituyen las llamadas directas entre estos servicios por Domain Events (`PedidoConfirmadoEvent`, ver [`patterns/Observer.md`](../../patterns/observer.md) y [`ddd.md`](./ddd.md)): al confirmar un pedido, se publica el evento, y `NotificadorPedidoEmail`, `ServicioFacturacion` y `ServicioInventario` reaccionan de forma independiente, sin que el caso de uso de creación de pedidos los conozca directamente.
5. Se verifica con el [`checklists/architecture.md`](../../checklists/architecture.md) que cada clase resultante tiene un único motivo de cambio identificable.

**Documentos consultados**: [`anti-patterns/GodObject.md`](../../anti-patterns/god-object.md), [`refactoring.md`](./refactoring.md), [`patterns/Observer.md`](../../patterns/observer.md), [`ddd.md`](./ddd.md), [`prompts/refactor.md`](../../prompts/refactor.md).

---

## Ejemplo 4 — Diagnóstico de un problema de rendimiento reportado por soporte

**Contexto**: el endpoint `GET /pedidos` empieza a tardar varios segundos en clientes con muchos pedidos históricos, tras funcionar bien en desarrollo con datos de prueba pequeños.

**Flujo aplicado** (siguiendo [`prompts/performance.md`](../../prompts/performance.md)):

1. Se activa logging de queries SQL en staging y se reproduce la petición con un volumen de datos representativo (no los 3 registros de desarrollo).
2. Se observa un patrón de N+1 queries: una query para los pedidos, y una query adicional por cada pedido para cargar su cliente asociado — coincide con el anti-patrón [N+1 Query](../../anti-patterns/n-plus-one.md).
3. Se aplica la corrección documentada: `JOIN FETCH`/`@EntityGraph` (Java/JPA) o `relations` (TypeORM) para traer pedidos y clientes en una única query.
4. Se vuelve a medir con la misma metodología (mismo volumen de datos, mismo entorno): el número de queries pasa de N+1 a 1, y la latencia baja de varios segundos a un rango aceptable.

**Documentos consultados**: [`anti-patterns/N+1Query.md`](../../anti-patterns/n-plus-one.md), [`prompts/performance.md`](../../prompts/performance.md), sección 11 y 6 de `CLAUDE.md`.
