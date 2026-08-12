# Recipe: Canary Deployment

Aplica: [`../knowledge/devops/deployment-strategies.md`](../knowledge/devops/deployment-strategies.md), [`../decisions/blue-green-vs-canary.md`](../decisions/blue-green-vs-canary.md), [`../knowledge/devops/monitoring.md`](../knowledge/devops/monitoring.md).

## Pasos

### 1. Definir explícitamente las métricas de comparación ANTES de empezar

```
Métricas técnicas: tasa de error, latencia p95, saturación de CPU/memoria
Métricas de negocio: conversión, valor medio de pedido (ver ejemplo en knowledge/devops/examples.md)
Umbral de rollback automático: tasa de error > 2x la de la versión estable
```

Decidir el criterio de éxito/fallo antes de exponer tráfico real evita la tentación de "mover la portería" durante el despliegue si los números no son claramente buenos ni claramente malos.

### 2. Desplegar la versión candidata con capacidad mínima

```yaml
# Kubernetes con Argo Rollouts, o Route 53 Weighted policy (ver knowledge/aws-network/route53.md)
apiVersion: argoproj.io/v1alpha1
kind: Rollout
spec:
  strategy:
    canary:
      steps:
        - setWeight: 5
        - pause: { duration: 30m }
        - setWeight: 25
        - pause: { duration: 30m }
        - setWeight: 100
```

### 3. Enrutamiento ponderado (sin Argo Rollouts, vía Route 53)

```hcl
resource "aws_route53_record" "canary" {
  weighted_routing_policy { weight = 5 }  # 5% del tráfico
  set_identifier = "canary"
  # ...
}
```

### 4. Monitorización activa durante cada fase de exposición

```bash
# Comparar métricas de la versión candidata vs. estable en el mismo dashboard, ver monitoring.md
watch -n 30 'curl -s https://api.empresa.com/metrics/canary-comparison'
```

Nunca dejar un Canary avanzando "desatendido" sin verificación activa entre cada incremento de peso — el propósito completo de esta estrategia es la detección temprana, que requiere atención activa durante la ventana de exposición.

### 5. Avanzar solo si las métricas están dentro de los umbrales definidos en el paso 1

Avanzar automáticamente (si se usa Argo Rollouts con `AnalysisTemplate`) o manualmente tras revisión explícita — nunca avanzar "por defecto" sin haber verificado los números contra el criterio predefinido.

### 6. Rollback automático ante umbral superado

```yaml
analysis:
  templates: [{ templateName: error-rate-check }]
  args: [{ name: service-name, value: pedidos-service }]
```

Ver [`../knowledge/devops/rollback.md`](../knowledge/devops/rollback.md) — el rollback automático basado en métricas reduce el tiempo de reacción de minutos a segundos.

### 7. Completar al 100% solo tras la última fase exitosa

## Checklist final

- [ ] ¿Las métricas de comparación y umbrales se definieron antes de exponer tráfico real?
- [ ] ¿Existe monitorización activa durante cada incremento de peso, no solo al final?
- [ ] ¿El avance entre fases está condicionado explícitamente a los umbrales, no automático sin verificación?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../decisions/blue-green-vs-canary.md`](../decisions/blue-green-vs-canary.md), [`blue-green-deployment.md`](./blue-green-deployment.md).
