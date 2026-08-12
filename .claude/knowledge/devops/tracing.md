# Tracing (plataforma de trazas)

> Ver [`../integration/distributed-tracing.md`](../integration/distributed-tracing.md) para cómo debe **instrumentarse** una aplicación/integración — este documento cubre la **plataforma de recolección** de esas trazas a escala de toda la organización. Ver [`../aws-monitoring/xray.md`](../aws-monitoring/xray.md) para la implementación nativa en AWS.

## Colector de trazas: punto único de recepción

```
Aplicaciones instrumentadas (OpenTelemetry SDK) → OpenTelemetry Collector → Backend de trazas (Jaeger/Tempo/X-Ray)
```

Un colector intermedio desacopla las aplicaciones del backend concreto de almacenamiento de trazas — permite cambiar de backend (o exportar a varios simultáneamente) sin modificar la instrumentación de cada aplicación individual, mismo principio de desacoplo que [`../../patterns/adapter.md`](../../patterns/adapter.md).

## Sampling: la decisión de plataforma más importante

```
Head-based sampling: se decide si trazar ANTES de conocer el resultado (ej. 10% de todas las peticiones)
Tail-based sampling: se decide DESPUÉS de completar la traza, priorizando trazas con error o latencia alta
```

Head-based es más simple y barato de implementar; tail-based garantiza capturar las trazas realmente interesantes (las que fallaron o fueron lentas) incluso si son una fracción pequeña del tráfico total — a costa de mayor complejidad de infraestructura (el colector debe retener la traza completa hasta decidir si conservarla). Ver [`../aws-monitoring/xray.md`](../aws-monitoring/xray.md) para el sampling configurable de X-Ray como ejemplo de head-based con ajuste de tasa.

## Propagación de contexto a través de fronteras heterogéneas

Ver [`../integration/distributed-tracing.md`](../integration/distributed-tracing.md) para el detalle de propagar contexto incluso a través de sistemas externos sin soporte nativo — la plataforma DevOps es responsable de que el estándar de propagación (W3C Trace Context) esté disponible y configurado consistentemente en todos los servicios internos, para que esa propagación funcione sin esfuerzo adicional de cada equipo.

## Retención de trazas: mucho más corta que logs/métricas

```
Trazas detalladas: retención de días, no meses — volumen y coste desproporcionados a mayor plazo
Métricas agregadas derivadas de trazas (latencia p95 por servicio): retención larga, mucho más barata
```

El valor de una traza individual decae rápidamente con el tiempo (es útil para diagnosticar un incidente reciente, no para análisis histórico) — retener trazas detalladas más allá de lo necesario para diagnóstico es coste sin beneficio proporcional; las métricas derivadas y agregadas sí se retienen a largo plazo.

## Checklist

- [ ] ¿Existe un colector intermedio que desacopla las aplicaciones del backend de trazas concreto?
- [ ] ¿La estrategia de sampling está calibrada conscientemente (head vs. tail-based) según el volumen y el valor de capturar trazas anómalas?
- [ ] ¿El estándar de propagación de contexto está disponible consistentemente en todos los servicios?
- [ ] ¿La retención de trazas detalladas es proporcionalmente más corta que la de métricas agregadas?

Ver también: [`../integration/distributed-tracing.md`](../integration/distributed-tracing.md), [`observability.md`](./observability.md), [`../aws-monitoring/xray.md`](../aws-monitoring/xray.md).
