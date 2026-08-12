# SNS (Simple Notification Service)

> Ver [`sqs.md`](./sqs.md) para colas punto a punto — SNS cubre el patrón complementario de publicación/suscripción (fan-out).

## Qué es: publicación/suscripción con múltiples suscriptores

Un productor publica un mensaje a un Topic; **todos** los suscriptores activos (múltiples colas SQS, funciones Lambda, endpoints HTTP, email, SMS) lo reciben — a diferencia de SQS donde un mensaje lo consume un único consumidor de la cola.

## Patrón Fan-out: SNS + múltiples colas SQS

```
Evento "pedido.confirmado" → SNS Topic
        ↓ suscrito              ↓ suscrito              ↓ suscrito
   SQS: notificaciones     SQS: inventario         SQS: facturación
```

Combinar SNS con SQS (en lugar de suscribir directamente Lambdas al Topic) añade el buffering y control de reintentos de una cola a cada consumidor — cada suscriptor procesa a su propio ritmo, con su propia Dead Letter Queue independiente, sin que un consumidor lento afecte a los demás. Ver [`../integration/event-driven.md`](../integration/event-driven.md) para el razonamiento completo de este patrón de desacoplo productor-consumidores.

## Filter Policies: cada suscriptor recibe solo lo relevante

```json
{ "estado": ["CONFIRMADO", "CANCELADO"] }
```

Un suscriptor puede definir una política de filtro sobre los atributos del mensaje, recibiendo solo el subconjunto relevante — evita que cada consumidor tenga que filtrar mensajes irrelevantes después de recibirlos, reduciendo procesamiento innecesario.

## Múltiples protocolos de entrega desde un único Topic

SNS puede entregar el mismo mensaje simultáneamente a SQS, Lambda, HTTP/HTTPS, email y SMS — relevante cuando el mismo evento de negocio necesita tanto procesamiento automatizado (colas/Lambda) como notificación directa a personas (email/SMS) sin duplicar la lógica de publicación.

## Checklist

- [ ] ¿Se usa el patrón Fan-out (SNS + SQS por consumidor) en lugar de suscribir Lambdas directamente cuando se necesita buffering/reintentos independientes por consumidor?
- [ ] ¿Los suscriptores usan Filter Policies para recibir solo lo relevante, evitando procesamiento innecesario?

Ver también: [`sqs.md`](./sqs.md), [`../integration/event-driven.md`](../integration/event-driven.md), [`../../decisions/sqs-vs-eventbridge.md`](../../decisions/sqs-vs-eventbridge.md).
