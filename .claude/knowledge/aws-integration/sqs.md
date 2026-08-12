# SQS (Simple Queue Service)

> Ver [`../../decisions/sqs-vs-eventbridge.md`](../../decisions/sqs-vs-eventbridge.md) para la comparativa con EventBridge. Ver [`../integration/message-queues.md`](../integration/message-queues.md) para los fundamentos de colas del framework, que SQS implementa de forma gestionada.

## Qué es: cola de mensajes punto a punto, totalmente gestionada

Un productor envía mensajes a la cola; uno o más consumidores los procesan y los eliminan tras completarlos — sin gestión de infraestructura de broker, con escalado automático y alta durabilidad nativa.

## Standard vs. FIFO: orden y duplicados

| | Standard | FIFO |
|---|---|---|
| Orden | No garantizado (aunque generalmente se preserva) | Garantizado dentro de un Message Group |
| Duplicados | Posibles (semántica "al menos una vez") | Deduplicación automática (5 min de ventana) |
| Throughput | Prácticamente ilimitado | Limitado (300-3000 msg/s según configuración) |

Standard por defecto salvo que el orden estricto o la deduplicación nativa sean requisitos reales del caso de uso — el throughput mucho mayor de Standard suele compensar la necesidad de idempotencia propia (ver [`../integration/idempotency.md`](../integration/idempotency.md)) en la mayoría de casos.

## Visibility Timeout: cuánto tiempo un consumidor tiene para procesar

```
Visibility Timeout: 30s → si el consumidor no elimina el mensaje en 30s, vuelve a ser visible para otro consumidor
```

Debe ser mayor que el tiempo real de procesamiento esperado — un timeout demasiado corto hace que el mismo mensaje se procese por duplicado (otro consumidor lo toma antes de que el primero termine), reforzando la necesidad de idempotencia (ver [`../integration/idempotency.md`](../integration/idempotency.md)) incluso en el caso "normal" sin fallos.

## Dead Letter Queue: configuración obligatoria en producción

```
maxReceiveCount: 3 → tras 3 intentos fallidos de procesamiento, el mensaje se mueve a la DLQ
```

Ver [`../integration/message-queues.md`](../integration/message-queues.md) — sin DLQ configurada, un mensaje que falla indefinidamente permanece reintentándose en la cola principal sin límite, potencialmente bloqueando el procesamiento de mensajes posteriores válidos.

## Long Polling: reducir coste y latencia de recepción

```
ReceiveMessageWaitTimeSeconds: 20 (long polling) en lugar de 0 (short polling, consulta constante)
```

Long polling mantiene la conexión abierta esperando hasta que llega un mensaje (o expira el timeout), reduciendo tanto el número de llamadas API (coste) como la latencia de recepción frente a hacer polling constante con intervalos cortos.

## Checklist

- [ ] ¿Se ha elegido Standard vs. FIFO según si el orden estricto/deduplicación nativa es un requisito real, no por defecto?
- [ ] ¿El Visibility Timeout es mayor que el tiempo real de procesamiento esperado?
- [ ] ¿Existe una Dead Letter Queue configurada con `maxReceiveCount` explícito?
- [ ] ¿El consumidor es idempotente, incluso con Visibility Timeout bien calibrado?

Ver también: [`sns.md`](./sns.md), [`../integration/message-queues.md`](../integration/message-queues.md), [`../../decisions/sqs-vs-eventbridge.md`](../../decisions/sqs-vs-eventbridge.md).
