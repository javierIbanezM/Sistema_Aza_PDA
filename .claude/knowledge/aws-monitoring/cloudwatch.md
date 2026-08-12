# CloudWatch

> Ver [`../architecture/logging.md`](../architecture/logging.md) para los fundamentos generales de observabilidad del framework — CloudWatch es la implementación nativa de AWS de logs, métricas y alertas.

## Logs, Métricas, Alarmas: los tres pilares

```
CloudWatch Logs: logs estructurados centralizados de todos los servicios
CloudWatch Metrics: series temporales (latencia, errores, CPU, custom business metrics)
CloudWatch Alarms: umbrales/SLOs sobre métricas que disparan notificaciones/acciones automáticas
```

Mismo modelo de los "cuatro golden signals" que documenta [`../architecture/logging.md`](../architecture/logging.md) — CloudWatch es dónde se instrumenta y visualiza ese modelo dentro de AWS.

## Log Groups y retención: coste vs. necesidad de auditoría

```
Log Group "/ecs/pedidos-service": retención 30 días (no "Never Expire" por defecto)
```

Sin retención configurada explícitamente, los logs se retienen indefinidamente por defecto, acumulando coste — definir retención según necesidad real (30-90 días para operación normal, más para requisitos de auditoría/cumplimiento específicos), nunca "para siempre" sin haberlo decidido conscientemente.

## Métricas custom: eventos de negocio, no solo técnicas

```java
cloudWatchClient.putMetricData(request -> request
    .namespace("Pedidos")
    .metricData(datum -> datum.metricName("PedidosConfirmados").value(1.0).unit(StandardUnit.COUNT)));
```

Ver [`../architecture/logging.md`](../architecture/logging.md) — las métricas técnicas (CPU, memoria) rara vez cuentan la historia completa; instrumentar métricas de negocio (pedidos confirmados, tasa de conversión, pagos fallidos) permite alertar y dashboardar sobre lo que realmente le importa al negocio, no solo a la infraestructura.

## Alarmas basadas en SLO, no umbrales arbitrarios

```
Alarma: p95 de latencia de /api/pedidos > 500ms durante 3 periodos consecutivos de 5 min → notificar
```

Mismo principio que [`../integration/observability.md`](../integration/observability.md) — definir el umbral basándose en un SLO de negocio acordado, no en un número arbitrario sin calibrar contra el comportamiento real observado del sistema.

## CloudWatch Dashboards: visibilidad consolidada

Un dashboard por servicio/dominio con los golden signals visibles de un vistazo (ver [`../architecture/logging.md`](../architecture/logging.md)) — evita que el equipo tenga que construir consultas ad-hoc cada vez que necesita entender el estado de un sistema, especialmente durante un incidente donde la velocidad de diagnóstico importa.

## Checklist

- [ ] ¿Los Log Groups tienen retención explícita, no "Never Expire" por defecto sin decisión consciente?
- [ ] ¿Existen métricas custom de negocio, no solo métricas técnicas de infraestructura?
- [ ] ¿Las alarmas están basadas en SLOs acordados, no en umbrales arbitrarios?
- [ ] ¿Existe un dashboard consolidado por servicio, no solo métricas dispersas sin agrupar?

Ver también: [`../architecture/logging.md`](../architecture/logging.md), [`cloudtrail.md`](./cloudtrail.md), [`xray.md`](./xray.md).
