# X-Ray

> Ver [`../integration/distributed-tracing.md`](../integration/distributed-tracing.md) para los fundamentos de trazas distribuidas del framework — X-Ray es la implementación nativa de AWS.

## Qué es: trazas distribuidas nativas de AWS

Rastrea una petición a través de múltiples servicios AWS (API Gateway → Lambda → DynamoDB → servicio externo), visualizando el tiempo consumido en cada salto — mismo concepto que OpenTelemetry documentado en [`../architecture/logging.md`](../architecture/logging.md), con integración nativa a servicios AWS sin instrumentación manual extensa.

## Instrumentación automática vs. manual

```java
// Instrumentación automática: el SDK de X-Ray envuelve llamadas a servicios AWS automáticamente
AWSXRay.beginSegment("pedidos-service");

// Instrumentación manual para lógica de negocio específica que no es una llamada AWS
Subsegment subsegment = AWSXRay.beginSubsegment("calcular-total-pedido");
try {
    calcularTotal(pedido);
} finally {
    AWSXRay.endSubsegment();
}
```

Las llamadas entre servicios AWS (Lambda invocando DynamoDB, por ejemplo) se instrumentan automáticamente con el SDK de X-Ray sin código adicional — lógica de negocio propia o llamadas a sistemas externos no-AWS requieren subsegmentos manuales, mismo principio que los spans manuales de [`../integration/distributed-tracing.md`](../integration/distributed-tracing.md).

## Service Map: visualización del grafo de dependencias real

X-Ray construye automáticamente un mapa visual de qué servicios llaman a qué otros servicios, con latencia y tasa de error por cada conexión — valioso para entender la arquitectura real en producción (que a menudo diverge del diagrama de arquitectura original con el tiempo) y para identificar rápidamente qué salto concreto es el cuello de botella en una petición lenta.

## Sampling: no trazar el 100% del tráfico en alto volumen

```
Regla de sampling: 1 traza por segundo fija + 5% del tráfico adicional por encima de esa tasa
```

Trazar cada petición individual en un sistema de alto volumen genera coste y overhead significativos — una regla de sampling captura una muestra representativa (garantizando siempre un mínimo de trazas para detectar problemas raros) sin el coste de trazar el 100% del tráfico.

## Correlación con CloudWatch Logs

```
trace-id de X-Ray incluido en cada log de CloudWatch → permite saltar de una traza lenta
directamente a los logs detallados de esa petición específica
```

Ver [`../integration/distributed-tracing.md`](../integration/distributed-tracing.md) — la correlación entre trazas y logs es lo que permite pasar de "esta petición fue lenta" (visible en X-Ray) a "por qué fue lenta" (visible en los logs detallados de esa traza específica), sin tener que buscar manualmente en un volumen de logs no filtrado.

## Checklist

- [ ] ¿Las llamadas a sistemas no-AWS (bases de datos externas, APIs de terceros) tienen subsegmentos manuales instrumentados?
- [ ] ¿La regla de sampling está calibrada para el volumen real, sin trazar el 100% innecesariamente en alto tráfico?
- [ ] ¿El trace-id se propaga a los logs de CloudWatch, permitiendo correlacionar una traza lenta con sus logs detallados?

Ver también: [`../integration/distributed-tracing.md`](../integration/distributed-tracing.md), [`cloudwatch.md`](./cloudwatch.md), [`../architecture/logging.md`](../architecture/logging.md).
