# Event-Driven Integration

> Ver [`../../patterns/observer.md`](../../patterns/observer.md) para el patrón de diseño subyacente, y [`../spring/spring-events.md`](../spring/spring-events.md) para la variante in-process. Este documento cubre eventos **entre sistemas distintos**, no dentro de un mismo proceso.

## Eventos de dominio como contrato de integración

```json
{
  "tipo": "pedido.confirmado",
  "version": 1,
  "timestamp": "2026-07-31T10:15:00Z",
  "pedidoId": "p-123",
  "clienteId": "c-456",
  "datos": { "total": 149.99, "lineas": [...] }
}
```

Un evento de dominio publicado hacia otros sistemas es, en la práctica, un **contrato público** igual que un endpoint de API — requiere el mismo rigor de diseño (ver [`api-design.md`](./api-design.md)) y versionado (ver [`api-versioning.md`](./api-versioning.md)) que cualquier API, aunque no sea un endpoint HTTP request/response tradicional.

## Notificación fina vs. transferencia de estado completo

```json
// Notificación fina: el consumidor debe consultar el detalle si lo necesita
{ "tipo": "pedido.confirmado", "pedidoId": "p-123" }

// Transferencia de estado completo: el consumidor tiene todo lo necesario sin una llamada adicional
{ "tipo": "pedido.confirmado", "pedido": { "id": "p-123", "lineas": [...], "total": 149.99 } }
```

La notificación fina reduce el acoplamiento al esquema de datos (el evento cambia menos) pero obliga al consumidor a hacer una llamada de vuelta al productor para obtener el detalle — introduciendo de nuevo una dependencia síncrona que el patrón asíncrono pretendía evitar. La transferencia de estado completo evita esa llamada de vuelta pero acopla el evento al esquema completo del agregado. La elección depende de cuántos consumidores necesitan el detalle completo vs. solo saber "que algo ocurrió".

## Desacoplar productor y consumidores: quién conoce a quién

```
PedidoService --publica--> Topic "pedidos.eventos" <--consume-- WmsService
                                                     <--consume-- ErpService
                                                     <--consume-- NotificacionesService
```

El productor publica a un topic/exchange sin conocer quién consume — añadir un nuevo consumidor (un nuevo sistema que necesita reaccionar a "pedido confirmado") no requiere ningún cambio en el productor, solo una nueva suscripción. Ver [`message-queues.md`](./message-queues.md) para el mecanismo de transporte.

## Eventos vs. comandos: distinción semántica importante

```json
// Evento: algo que YA ocurrió, en pasado, el productor no espera ni exige una reacción concreta
{ "tipo": "pedido.confirmado", ... }

// Comando: una instrucción explícita de que algo DEBE ocurrir, dirigida a un consumidor específico
{ "tipo": "reservar-stock", "destinatario": "wms-service", ... }
```

Un evento no debería nombrarse con un verbo imperativo (`crear-pedido` como evento es una señal de confusión conceptual) — los eventos se nombran en pasado (`pedido.confirmado`), representan hechos ya ocurridos que cualquier número de consumidores puede o no decidir procesar, sin que el productor dependa de que lo hagan.

## Checklist

- [ ] ¿Los eventos publicados hacia otros sistemas están versionados y documentados igual que un contrato de API?
- [ ] ¿Se ha decidido conscientemente entre notificación fina y transferencia de estado completo, no por defecto?
- [ ] ¿Los eventos están nombrados en pasado, representando hechos ya ocurridos, no instrucciones imperativas?
- [ ] ¿El productor puede añadir nuevos consumidores sin necesidad de modificarse a sí mismo?

Ver también: [`message-queues.md`](./message-queues.md), [`../../patterns/observer.md`](../../patterns/observer.md), [`saga.md`](./saga.md).
