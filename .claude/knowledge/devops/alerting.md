# Alerting

> Ver [`observability.md`](./observability.md) para la estrategia general y [`monitoring.md`](./monitoring.md) para las métricas sobre las que se definen alertas. Este documento cubre específicamente el diseño de alertas y la gestión de guardias (on-call).

## Alertar sobre síntomas visibles al usuario, no solo causas internas

```
Alerta preferida: "tasa de error de /api/pedidos > 5%" (síntoma, impacto directo en usuarios)
Alerta secundaria: "CPU del nodo > 90%" (causa interna, no siempre implica impacto real)
```

Mismo principio que [`../architecture/logging.md`](../architecture/logging.md) — una causa interna alta no siempre se traduce en impacto real (el sistema puede seguir cumpliendo su SLO con CPU alta); alertar primero sobre el síntoma que sí importa al negocio, usando las causas internas como contexto de diagnóstico una vez la alerta de síntoma ya se disparó.

## Fatiga de alertas: el enemigo silencioso de la observabilidad

Una alerta que se dispara constantemente sin acción posible (o que resulta ser un falso positivo recurrente) entrena al equipo a ignorarla — y esa costumbre de ignorar se extiende, sin darse cuenta, a alertas genuinamente importantes que llegan en el mismo canal. Cada alerta debe ser accionable; si no lo es, se elimina o se convierte en un dashboard de solo consulta, no en una alerta activa.

## Severidad y canal de notificación diferenciados

```
Crítica (impacto activo en producción): página al on-call inmediatamente, cualquier hora
Advertencia (riesgo, no impacto aún): notificación en canal de equipo, revisión en horario laboral
Informativa: solo visible en dashboard, sin notificación activa
```

No todo lo que merece registrarse merece despertar a alguien a las 3 de la madrugada — clasificar explícitamente cada alerta por severidad real, y enrutar cada nivel al canal apropiado.

## Runbooks vinculados a cada alerta

```
Alerta: "Circuit Breaker erp-sap abierto"
Runbook vinculado: pasos de diagnóstico específicos (ver knowledge/integration/circuit-breaker.md),
                    a quién escalar si persiste más de 15 minutos
```

Una alerta sin un runbook vinculado obliga a quien recibe la notificación a reconstruir el contexto de diagnóstico desde cero, bajo presión, posiblemente sin haber visto nunca ese sistema concreto — vincular explícitamente cada alerta crítica a su procedimiento de respuesta documentado (ver [`../../playbooks/incident-response.md`](../../playbooks/incident-response.md)).

## Escalado automático ante falta de respuesta

```
Alerta crítica sin acknowledge en 5 min → escala automáticamente al siguiente nivel de guardia
```

Un sistema de guardias sin escalado automático depende de que la primera persona notificada esté disponible y despierta — el escalado automático es la red de seguridad ante el caso, estadísticamente inevitable, de que eso no ocurra.

## Checklist

- [ ] ¿Las alertas priorizan síntomas visibles al usuario sobre causas internas aisladas?
- [ ] ¿Cada alerta activa es genuinamente accionable, sin ruido de falsos positivos recurrentes?
- [ ] ¿La severidad determina el canal/urgencia de notificación, no todo al mismo canal?
- [ ] ¿Las alertas críticas tienen un runbook vinculado y escalado automático ante falta de respuesta?

Ver también: [`observability.md`](./observability.md), [`../../playbooks/incident-response.md`](../../playbooks/incident-response.md).
