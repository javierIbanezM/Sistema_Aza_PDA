# SQS vs. EventBridge

## Contexto

Ambos son mecanismos de mensajería asíncrona gestionados en AWS — ver [`../knowledge/integration/message-queues.md`](../knowledge/integration/message-queues.md) y [`../knowledge/integration/event-driven.md`](../knowledge/integration/event-driven.md) para los fundamentos del framework que esta comparativa aplica al contexto AWS.

## Comparativa

| Criterio | SQS (+ SNS para fan-out) | EventBridge |
|---|---|---|
| Modelo | Cola punto a punto (SQS) o fan-out simple (SNS) | Bus de eventos con enrutamiento basado en contenido |
| Enrutamiento condicional | Filter Policies simples por atributo (SNS) | Reglas ricas sobre el contenido completo del evento |
| Integración con SaaS de terceros | Ninguna nativa | Amplia (decenas de partners integrados nativamente) |
| Schema Registry integrado | No | Sí, con inferencia automática y versionado |
| Coste | Menor | Ligeramente mayor por mensaje |
| Curva de aprendizaje | Baja, modelo simple y directo | Media, requiere entender reglas de enrutamiento |
| Orden garantizado | Sí, con SQS FIFO | No garantizado nativamente |

## Cuándo elegir SQS (+ SNS si se necesita fan-out)

- El patrón es una cola de trabajo simple (un productor, uno o varios consumidores que procesan sin necesidad de enrutamiento condicional).
- Se necesita orden garantizado (SQS FIFO) o deduplicación nativa.
- Prioridad en simplicidad y coste mínimo sobre flexibilidad de enrutamiento avanzada.

## Cuándo elegir EventBridge

- Se necesita enrutar el mismo evento a destinos distintos según su contenido (no solo un atributo simple), sin que el productor conozca esa lógica de enrutamiento.
- Integración con aplicaciones SaaS de terceros vía conectores nativos.
- Se valora el Schema Registry integrado para mantener el contrato de eventos versionado sin trabajo manual adicional (ver [`../knowledge/integration/api-versioning.md`](../knowledge/integration/api-versioning.md)).
- El catálogo de eventos de negocio va a crecer significativamente y se beneficia de un bus centralizado con reglas de enrutamiento explícitas y auditable.

## Recomendación por defecto de este framework

**SQS (+ SNS si hay múltiples consumidores) para el caso simple de desacoplo productor-consumidor** — la mayoría de integraciones internas del framework (ver [`../knowledge/integration/`](../knowledge/integration/)); **EventBridge** cuando el enrutamiento condicional complejo o la integración con SaaS externos son requisitos reales confirmados, no anticipados especulativamente.

Ver también: [`../knowledge/aws-integration/sqs.md`](../knowledge/aws-integration/sqs.md), [`../knowledge/aws-integration/sns.md`](../knowledge/aws-integration/sns.md), [`../knowledge/aws-integration/eventbridge.md`](../knowledge/aws-integration/eventbridge.md), [`../knowledge/integration/message-queues.md`](../knowledge/integration/message-queues.md).
