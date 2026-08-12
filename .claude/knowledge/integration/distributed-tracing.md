# Distributed Tracing (en integración de sistemas)

> Ver [`../architecture/logging.md`](../architecture/logging.md) para los fundamentos de trazas distribuidas y correlación de peticiones — este documento cubre la propagación de contexto **a través de sistemas externos que no siempre soportan el mismo estándar de trazado**.

## El problema añadido por sistemas externos heterogéneos

Dentro de servicios propios, propagar un `trace-id` es sencillo — todos los servicios comparten la misma convención (ver [`../architecture/logging.md`](../architecture/logging.md)). Un ERP/WMS de terceros puede no soportar `traceparent` (W3C Trace Context) ni ningún estándar de trazado — la traza se "rompe" en la frontera con el sistema externo, salvo que se diseñe explícitamente cómo preservar la correlación.

## Preservar correlación incluso sin soporte nativo del sistema externo

```java
// Aunque el ERP no entienda trace-id, se envía como campo de negocio adicional
// que el ERP simplemente almacena y devuelve tal cual en sus respuestas/callbacks
ErpRequest request = ErpRequest.builder()
    .datos(pedido)
    .referenciaExterna(MDC.get("traceId")) // el ERP lo trata como un ID de referencia opaco
    .build();
```

Muchos sistemas externos, aunque no entiendan el concepto de trazado distribuido, sí soportan un campo de "referencia externa"/"ID de correlación de negocio" que devuelven en sus respuestas — usar ese campo para transportar el `trace-id` propio permite, al recibir la respuesta o un callback posterior, reconstruir a qué traza original pertenece, incluso sin soporte nativo del estándar.

## Spans para cada llamada a sistema externo

```java
Span span = tracer.spanBuilder("erp.crear-pedido").startSpan();
try (Scope scope = span.makeCurrent()) {
    span.setAttribute("sistema.externo", "sap-erp");
    span.setAttribute("pedido.id", pedido.getId());
    erpClient.crearPedido(pedido);
    span.setStatus(StatusCode.OK);
} catch (Exception e) {
    span.recordException(e);
    span.setStatus(StatusCode.ERROR);
    throw e;
} finally {
    span.end();
}
```

Ver [`../architecture/logging.md`](../architecture/logging.md) para el mecanismo base de spans con OpenTelemetry — cada llamada a un sistema externo debe ser su propio span, con atributos que identifiquen claramente qué sistema externo es, permitiendo en la visualización de la traza distinguir de un vistazo cuánto tiempo se pasó en cada integración durante el procesamiento de una petición.

## Trazas asíncronas: propagar contexto a través de colas

```java
// Al publicar: adjuntar el trace-id al mensaje
producer.send(new ProducerRecord<>("pedidos.eventos", evento)
    .headers().add("trace-id", MDC.get("traceId").getBytes()));

// Al consumir: recuperar y restaurar el contexto antes de procesar
String traceId = new String(record.headers().lastHeader("trace-id").value());
MDC.put("traceId", traceId); // el procesamiento asíncrono queda correlacionado con la petición original
```

Sin este mecanismo, cualquier procesamiento asíncrono disparado por un evento pierde la conexión con la traza que lo originó — dificultando enormemente diagnosticar un flujo que cruza de síncrono a asíncrono (una petición HTTP que publica un evento, consumido minutos después por otro proceso).

## Correlación con logs de sistemas externos (cuando es posible)

Si el sistema externo expone sus propios logs/auditoría (algunos ERPs modernos lo permiten), incluir el mismo campo de referencia externa en las búsquedas de ambos lados permite reconstruir el flujo completo cruzando los logs propios con los del sistema externo — valioso en incidentes donde la causa raíz está del lado del sistema externo, no del propio.

## Checklist

- [ ] ¿Se preserva algún identificador de correlación incluso al integrar con sistemas que no soportan el estándar de trazado nativo?
- [ ] ¿Cada llamada a un sistema externo genera su propio span, identificable claramente en la visualización de la traza?
- [ ] ¿El contexto de traza se propaga a través de mensajes de cola, no se pierde al pasar de síncrono a asíncrono?

Ver también: [`../architecture/logging.md`](../architecture/logging.md), [`observability.md`](./observability.md), [`message-queues.md`](./message-queues.md).
