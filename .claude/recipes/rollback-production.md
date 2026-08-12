# Recipe: Rollback en Producción

Aplica: [`../knowledge/devops/rollback.md`](../knowledge/devops/rollback.md), [`../playbooks/rollback-production.md`](../playbooks/rollback-production.md).

## Pasos

### 1. Confirmar que el problema coincide temporalmente con un despliegue reciente

```bash
kubectl rollout history deployment/pedidos-service
# comparar timestamp del último rollout con el inicio del problema reportado en observability.md
```

Nunca hacer rollback "por si acaso" sin esa correlación temporal confirmada — ver [`../agents/senior-debugger.md`](../agents/senior-debugger.md), hipótesis antes que acción.

### 2. Ejecutar el rollback (Kubernetes)

```bash
kubectl rollout undo deployment/pedidos-service
kubectl rollout status deployment/pedidos-service  # esperar a que complete
```

### 3. Ejecutar el rollback (ECS)

```bash
aws ecs update-service --cluster main --service pedidos-service \
  --task-definition pedidos-service:PREVIOUS_REVISION
```

### 4. Verificar que el rollback resolvió el síntoma

```bash
# Confirmar con las mismas métricas que detectaron el problema — nunca asumir sin verificar
curl -s https://api.empresa.com/health
# revisar tasa de error en el dashboard tras 5 minutos del rollback completado
```

### 5. Rollback de datos, si aplica

Si el despliegue revertido incluía una migración de esquema, verificar que el código anterior sigue siendo compatible con el estado actual de los datos (ver [`../knowledge/devops/rollback.md`](../knowledge/devops/rollback.md), patrón expansivo/contractivo) — si no lo es, este es exactamente el escenario que ese patrón está diseñado para evitar; sin él, el rollback de código por sí solo puede no ser suficiente.

### 6. Comunicar y documentar

```
Canal de incidentes: "Rollback ejecutado a v2.4.0 tras detectar tasa de error del 12% en v2.4.1.
Causa raíz en investigación. Servicio estabilizado a las 10:47."
```

Ver [`../playbooks/hotfix.md`](../playbooks/hotfix.md) — nunca un rollback silencioso.

### 7. Post-mortem

Ver [`../playbooks/incident-response.md`](../playbooks/incident-response.md) — identificar qué verificación (test, canary, revisión) debería haber detectado el problema antes de llegar a producción.

## Checklist final

- [ ] ¿Se confirmó la correlación temporal con un despliegue reciente antes de actuar?
- [ ] ¿El rollback se verificó con las mismas métricas que detectaron el problema original?
- [ ] ¿Se comunicó explícitamente al equipo, sin ambigüedad sobre el estado actual?
- [ ] ¿Se ha programado o realizado el post-mortem?

Ver también: [`../knowledge/devops/rollback.md`](../knowledge/devops/rollback.md), [`../playbooks/rollback-production.md`](../playbooks/rollback-production.md).
