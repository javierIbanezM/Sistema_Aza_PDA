# Observability (estrategia de plataforma)

> Ver [`../architecture/logging.md`](../architecture/logging.md) para cómo debe instrumentarse una **aplicación** (qué loguear, formato, correlación) — este documento cubre la **plataforma** que recolecta, agrega y expone esa telemetría a escala de toda la organización, no de un único servicio. Ver [`monitoring.md`](./monitoring.md), [`logging.md`](./logging.md), [`tracing.md`](./tracing.md), [`alerting.md`](./alerting.md) para cada pilar en detalle.

## El rol de DevOps en observabilidad: la plataforma, no la instrumentación

Cada equipo instrumenta su propia aplicación (logs estructurados, métricas, trazas) siguiendo [`../architecture/logging.md`](../architecture/logging.md) — DevOps es responsable de que exista **una plataforma centralizada** donde esa telemetría de todos los servicios converge, se correlaciona entre sí, y se hace consultable/alertable de forma consistente, sin que cada equipo tenga que construir su propia solución de observabilidad desde cero.

## Los tres pilares en la plataforma

```
Logs (logging.md)   → Agregación centralizada, búsqueda, retención
Metrics (monitoring.md) → Series temporales, dashboards, SLOs
Traces (tracing.md)  → Correlación entre servicios, cuellos de botella en el flujo completo
```

Una plataforma de observabilidad madura permite pivotar entre los tres: desde una métrica anómala, saltar a las trazas del periodo afectado, y desde una traza lenta, saltar directamente a los logs detallados de esa petición específica — sin esa correlación, cada pilar es una isla de información desconectada.

## Observabilidad como requisito de diseño, no añadido post-incidente

Ningún servicio se despliega a producción sin instrumentación mínima verificable (logs estructurados llegando a la plataforma, métricas de los cuatro golden signals, health check) — exigir esto como parte del Definition of Done (ver `CLAUDE.md` sección 21), no como una mejora "para más adelante" tras el primer incidente sin visibilidad.

## SLOs de plataforma vs. SLOs de servicio

```
SLO de servicio: "99% de peticiones a /api/pedidos en menos de 300ms" (responsabilidad del equipo del servicio)
SLO de plataforma: "99.9% de disponibilidad del sistema de logging/monitorización en sí"
```

La propia plataforma de observabilidad necesita sus propios SLOs — si el sistema de monitorización está caído durante un incidente del servicio que monitoriza, el equipo queda ciego exactamente cuando más lo necesita.

## Coste de la observabilidad: proporcional, no ilimitado

Retener el 100% de logs/trazas de todo el tráfico indefinidamente tiene un coste de almacenamiento que crece sin límite — aplicar retención escalonada (detalle completo reciente, agregados/muestreados a largo plazo, ver [`logging.md`](./logging.md) y [`tracing.md`](./tracing.md) para sampling) es una decisión de ingeniería consciente, no una limitación a ignorar.

## Checklist

- [ ] ¿Existe una plataforma centralizada donde converge la telemetría de todos los servicios, no soluciones aisladas por equipo?
- [ ] ¿Es posible correlacionar entre logs, métricas y trazas de la misma petición/incidente?
- [ ] ¿Ningún servicio se despliega sin instrumentación mínima verificable?
- [ ] ¿La propia plataforma de observabilidad tiene sus propios SLOs monitorizados?

Ver también: [`monitoring.md`](./monitoring.md), [`logging.md`](./logging.md), [`tracing.md`](./tracing.md), [`alerting.md`](./alerting.md), [`../architecture/logging.md`](../architecture/logging.md).
