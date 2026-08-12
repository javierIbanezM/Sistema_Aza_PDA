# EventBridge

> Ver [`../../decisions/sqs-vs-eventbridge.md`](../../decisions/sqs-vs-eventbridge.md) para la comparativa completa con SQS/SNS.

## Qué es: bus de eventos con enrutamiento basado en contenido

Más allá de SNS (fan-out simple), EventBridge permite **enrutar eventos según reglas sobre su contenido** hacia múltiples destinos, con un Schema Registry integrado y conectores nativos a decenas de servicios AWS y aplicaciones SaaS de terceros.

## Reglas de enrutamiento basadas en contenido del evento

```json
{
  "source": ["pedidos.servicio"],
  "detail-type": ["PedidoConfirmado"],
  "detail": { "total": [{ "numeric": [">", 1000] }] }
}
```

A diferencia de las Filter Policies de SNS (filtrado simple por atributo), las reglas de EventBridge permiten lógica de enrutamiento más rica basada en el contenido completo del evento — por ejemplo, enrutar solo pedidos de alto valor hacia un flujo de revisión adicional, sin que el productor necesite saber nada de esa lógica de enrutamiento.

## Múltiples buses: default, personalizados, y de partners SaaS

```
Bus default: eventos nativos de servicios AWS de la cuenta
Bus personalizado "pedidos-eventos": eventos de dominio propios de la aplicación
Bus de partner: eventos de un SaaS de terceros integrado (ej. Salesforce, Zendesk)
```

Separar eventos propios de negocio en un bus personalizado, distinto del bus default de eventos de infraestructura AWS — mantiene el catálogo de eventos de negocio organizado y facilita aplicar reglas de acceso/retención específicas.

## Schema Registry: contrato de eventos versionado, integrado

EventBridge puede inferir y almacenar automáticamente el schema de los eventos publicados, con versionado — mismo principio que [`../integration/api-versioning.md`](../integration/api-versioning.md) aplica a eventos, pero con la ventaja de generación automática de tipos/código a partir del schema descubierto, reduciendo el trabajo manual de mantener el contrato sincronizado.

## Cuándo EventBridge en lugar de SNS+SQS

Ver [`../../decisions/sqs-vs-eventbridge.md`](../../decisions/sqs-vs-eventbridge.md) para el análisis completo — en resumen: EventBridge cuando se necesita enrutamiento basado en contenido rico, integración nativa con SaaS de terceros, o un catálogo de eventos con schema versionado gestionado; SNS+SQS cuando el patrón es un fan-out simple sin necesidad de lógica de enrutamiento condicional compleja.

## Checklist

- [ ] ¿Se necesita enrutamiento basado en contenido del evento, más allá de un fan-out simple que SNS ya resolvería?
- [ ] ¿Los eventos de negocio propios viven en un bus personalizado, separados de eventos de infraestructura AWS?
- [ ] ¿Se aprovecha el Schema Registry para mantener el contrato de eventos versionado sin trabajo manual duplicado?

Ver también: [`sqs.md`](./sqs.md), [`sns.md`](./sns.md), [`../../decisions/sqs-vs-eventbridge.md`](../../decisions/sqs-vs-eventbridge.md), [`../integration/event-driven.md`](../integration/event-driven.md).
