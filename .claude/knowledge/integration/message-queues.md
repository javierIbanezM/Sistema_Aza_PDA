# Message Queues

> Ver `CLAUDE.md` sección 16 para las reglas base de RabbitMQ/Kafka del framework, y [`event-driven.md`](./event-driven.md) para el patrón de diseño que estas colas transportan. Este documento profundiza en consideraciones de integración con sistemas externos.

## Cuándo colas y cuándo llamada síncrona directa

| Criterio | Cola | Llamada síncrona |
|---|---|---|
| El llamador necesita el resultado para continuar | No | Sí |
| El sistema destino puede estar caído temporalmente sin bloquear al productor | Sí (la cola amortigua) | No (falla inmediatamente) |
| Se necesita garantía de entrega incluso ante caída del consumidor | Sí | No |
| Latencia crítica (respuesta en milisegundos) | No apropiado | Sí |
| Múltiples consumidores del mismo evento | Sí (pub/sub natural) | Requiere fan-out manual |

## Colas como amortiguador ante sistemas externos lentos/inestables

```java
@RabbitListener(queues = "erp.sincronizacion")
public void procesarSincronizacionErp(SincronizacionMessage mensaje) {
    // el ERP externo puede tardar segundos en responder — la cola absorbe ese tiempo
    // sin bloquear al sistema que generó el mensaje originalmente
    erpClient.sincronizar(mensaje.getDatos());
}
```

Integrar con un sistema externo lento o con SLA pobre (típico en ERPs/WMS legados) de forma síncrona propaga esa lentitud a cualquier llamador — desacoplar con una cola permite que el productor continúe inmediatamente, mientras el consumidor procesa contra el sistema lento a su propio ritmo, incluida la aplicación de reintentos (ver [`retries-and-backoff.md`](./retries-and-backoff.md)) sin que el usuario final espere.

## Dead Letter Queue: qué hacer con mensajes que fallan repetidamente

```
Cola principal → (falla N veces) → Dead Letter Queue → alerta al equipo + revisión manual
```

Ver `CLAUDE.md` sección 16.1 — todo consumidor de producción necesita una DLQ configurada; un mensaje que falla indefinidamente sin ir a una DLQ, o bien bloquea la cola principal reintentando sin fin, o se pierde silenciosamente según la configuración — ninguno de los dos es aceptable sin decisión explícita.

## Orden de mensajes: cuándo importa y cómo garantizarlo

```java
// Kafka: la clave de partición determina el orden garantizado DENTRO de esa partición
producer.send(new ProducerRecord<>("pedidos.eventos", pedido.getId(), evento)); // pedido.getId() como clave
```

El orden **entre particiones/colas distintas no está garantizado** — si el orden de eventos de un mismo agregado (todos los eventos de un pedido concreto) importa, usar el identificador de ese agregado como clave de partición asegura que todos sus eventos van a la misma partición y se procesan en orden; eventos de agregados distintos pueden procesarse en cualquier orden relativo entre sí, lo cual normalmente es aceptable.

## Contratos de mensaje versionados con Schema Registry

```
Productor → valida contra schema v2 → publica → Schema Registry
Consumidor → obtiene schema v2 → deserializa correctamente, incluso si el consumidor es v1-aware
```

Ver `CLAUDE.md` sección 16.2 — un Schema Registry (Avro/Protobuf) centraliza y versiona el esquema de los mensajes, permitiendo verificar compatibilidad hacia atrás/adelante automáticamente antes de publicar un cambio, evitando que un productor rompa a consumidores existentes sin que nadie lo detecte hasta que falla en producción.

## Checklist

- [ ] ¿Se ha elegido cola vs. llamada síncrona según si el llamador necesita el resultado inmediato?
- [ ] ¿Existe una Dead Letter Queue configurada para mensajes que fallan repetidamente?
- [ ] ¿Los eventos de un mismo agregado usan una clave de partición consistente si su orden relativo importa?
- [ ] ¿Los esquemas de mensaje están versionados y verificados de compatibilidad antes de publicar cambios?

Ver también: `CLAUDE.md` sección 16, [`event-driven.md`](./event-driven.md), [`retries-and-backoff.md`](./retries-and-backoff.md).
