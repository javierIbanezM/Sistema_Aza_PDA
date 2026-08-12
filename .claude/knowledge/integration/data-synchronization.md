# Data Synchronization

> Ver [`schema-mapping.md`](./schema-mapping.md) para traducir entre modelos, y [`event-driven.md`](./event-driven.md)/[`message-queues.md`](./message-queues.md) para el mecanismo de propagación de cambios entre sistemas.

## Quién es la fuente de verdad

Antes de sincronizar, decidir explícitamente **qué sistema es la fuente de verdad** para cada dato — un mismo campo (ej. "stock disponible") no puede tener dos fuentes de verdad simultáneas sin un mecanismo de resolución de conflictos; normalmente un sistema es autoritativo (el WMS para stock físico) y los demás mantienen una **réplica** de ese dato, no una copia independiente que pueda divergir sin control.

## Estrategias de sincronización

| Estrategia | Cuándo | Trade-off |
|---|---|---|
| Tiempo real (eventos) | Cambios que otros sistemas necesitan ver casi inmediatamente | Mayor complejidad, requiere infraestructura de eventos (ver [`event-driven.md`](./event-driven.md)) |
| Batch periódico | Datos que toleran cierto retraso (catálogos, reportes) | Más simple, pero introduce una ventana de inconsistencia conocida |
| Bajo demanda (pull) | El consumidor decide cuándo necesita los datos actualizados | El consumidor debe iniciar activamente la sincronización |

## Sincronización batch: idempotencia y detección de cambios

```sql
-- Sincronizar solo lo que cambió desde la última ejecución, no la tabla completa cada vez
SELECT * FROM productos WHERE fecha_modificacion > :ultima_sincronizacion_exitosa;
```

Sincronizar el dataset completo en cada ejecución es simple pero costoso e innecesario a partir de cierto volumen — usar una marca de tiempo de última modificación (o un log de cambios/CDC — Change Data Capture) permite sincronizar solo los deltas, reduciendo drásticamente el coste de cada ejecución periódica. Ver [`idempotency.md`](./idempotency.md) — cada registro sincronizado debe poder reprocesarse sin duplicar efectos si la sincronización se reintenta tras un fallo parcial.

## Resolución de conflictos: cuando dos sistemas modifican el mismo dato

```java
if (registroLocal.getFechaModificacion().isAfter(registroRemoto.getFechaModificacion())) {
    // last-write-wins: el más reciente gana — simple, pero puede perder cambios silenciosamente
    aplicarCambioRemoto = false;
}
```

*Last-write-wins* (basado en timestamp) es la estrategia más simple pero puede descartar silenciosamente un cambio legítimo si ambos sistemas modificaron el dato en una ventana cercana — para datos donde perder un cambio tiene coste de negocio relevante, considerar estrategias más explícitas: marcar el conflicto para revisión manual, o definir reglas de negocio específicas sobre qué campo de qué sistema prevalece (ver [`../../agents/senior-business-analyst.md`](../../agents/senior-business-analyst.md) — esta es una decisión de negocio, no solo técnica).

## Consistencia eventual: comunicar la ventana de inconsistencia

Cualquier sincronización no instantánea (batch, incluso eventos con cierta latencia de procesamiento) implica una ventana donde los sistemas están temporalmente desincronizados — esto debe comunicarse explícitamente a quien consume los datos ("el stock mostrado puede tener hasta 5 minutos de retraso"), no dejarse como una sorpresa quien descubre la inconsistencia por accidente. Ver [`../../patterns/cqrs.md`](../../patterns/cqrs.md) para el mismo principio aplicado a separación de lectura/escritura dentro de un único sistema.

## Checklist

- [ ] ¿Está definido explícitamente qué sistema es la fuente de verdad para cada dato sincronizado?
- [ ] ¿La sincronización batch usa detección de deltas, no el dataset completo en cada ejecución?
- [ ] ¿Existe una estrategia explícita de resolución de conflictos, no solo "el que llega último gana" por defecto sin haberlo decidido conscientemente?
- [ ] ¿La ventana de inconsistencia esperada está comunicada a los consumidores de los datos sincronizados?

Ver también: [`schema-mapping.md`](./schema-mapping.md), [`idempotency.md`](./idempotency.md), [`event-driven.md`](./event-driven.md).
