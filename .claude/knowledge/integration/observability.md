# Observability (de integraciones)

> Ver [`../architecture/logging.md`](../architecture/logging.md) para los fundamentos generales de logging/métricas/trazas — este documento cubre qué observar **específicamente en los puntos de integración con sistemas externos**, donde los fallos suelen ser menos visibles que los fallos internos.

## Por qué la observabilidad de integraciones es distinta

Un fallo interno del propio sistema suele producir una excepción visible inmediatamente en los logs propios; un fallo de integración (el ERP responde lento pero no falla, un evento se pierde silenciosamente en una cola mal configurada, un Circuit Breaker se abre y nadie lo nota) puede degradarse silenciosamente durante horas o días antes de que alguien lo detecte por una queja de negocio — la observabilidad de integraciones necesita ser proactiva, no solo reactiva a errores explícitos.

## Métricas esenciales por integración

| Métrica | Por qué importa |
|---|---|
| Latencia (p50/p95/p99) por sistema externo | Detecta degradación gradual antes de que se convierta en timeout/fallo |
| Tasa de error por sistema externo | Distingue "todo va bien" de "está fallando pero con reintentos silenciosos absorbiendo el problema" |
| Estado del Circuit Breaker (cerrado/abierto/semi-abierto) | Ver [`circuit-breaker.md`](./circuit-breaker.md) — visibilidad directa de degradación |
| Profundidad de la Dead Letter Queue | Un crecimiento indica mensajes que fallan sistemáticamente, no solo casos aislados |
| Antigüedad del mensaje más viejo en la cola | Detecta un consumidor atascado o caído, no solo lento |
| Última sincronización exitosa (para batch) | Ver [`data-synchronization.md`](./data-synchronization.md) — detecta un job de sincronización silenciosamente roto |

## Health checks que verifican dependencias reales, no solo el propio proceso

```java
@Component
public class ErpHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            erpClient.ping(); // verificación real y ligera de conectividad, no solo "el proceso está vivo"
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withDetail("erp", "No disponible").build();
        }
    }
}
```

Ver [`../spring/spring-actuator.md`](../spring/spring-actuator.md) — un health check que solo verifica que el propio proceso responde, sin verificar sus dependencias externas críticas, puede reportar "saludable" mientras la integración con el ERP lleva horas caída.

## Alertas basadas en SLO de la integración, no umbrales arbitrarios

En lugar de "alertar si la latencia supera 500ms" (arbitrario), definir un SLO explícito por integración ("el 99% de las llamadas al ERP deben completar en menos de 2 segundos, medido en ventanas de 30 días", ver [`../architecture/logging.md`](../architecture/logging.md)) y alertar cuando el SLO está en riesgo de incumplirse — evita tanto alertas ruidosas por variabilidad normal como la ausencia de alerta ante una degradación real pero por debajo de un umbral fijo mal calibrado.

## Dashboard consolidado de todas las integraciones

Un dashboard único que muestre, de un vistazo, el estado de todas las integraciones activas (latencia, tasa de error, estado de circuit breaker, profundidad de colas) es más valioso que dashboards dispersos por sistema — permite a cualquier persona del equipo, no solo a quien construyó cada integración, entender rápidamente si algo está degradado antes de que un usuario de negocio lo reporte.

## Checklist

- [ ] ¿Existen métricas de latencia y tasa de error específicas por cada sistema externo integrado, no solo agregadas?
- [ ] ¿Los health checks verifican conectividad real con las dependencias externas críticas, no solo el propio proceso?
- [ ] ¿Las alertas están basadas en SLOs definidos explícitamente, no en umbrales arbitrarios sin calibrar?
- [ ] ¿Existe un dashboard consolidado visible para todo el equipo, no solo para quien construyó cada integración?

Ver también: [`../architecture/logging.md`](../architecture/logging.md), [`distributed-tracing.md`](./distributed-tracing.md), [`circuit-breaker.md`](./circuit-breaker.md).
