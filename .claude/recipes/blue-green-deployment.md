# Recipe: Blue/Green Deployment

Aplica: [`../knowledge/devops/deployment-strategies.md`](../knowledge/devops/deployment-strategies.md), [`../decisions/rolling-vs-blue-green.md`](../decisions/rolling-vs-blue-green.md).

## Pasos

### 1. Desplegar la versión nueva (Green) en paralelo a la actual (Blue), sin tráfico aún

```bash
aws ecs create-service --cluster main --service-name pedidos-service-green \
  --task-definition pedidos-service:NEW_REVISION --desired-count 2
# Green corre en paralelo a Blue, pero sin recibir tráfico de producción todavía
```

### 2. Verificar Green de forma aislada antes de exponerlo

```bash
curl -s https://green-internal.empresa.com/health
# smoke tests contra Green directamente, sin afectar al tráfico real servido por Blue
```

### 3. Conmutar el tráfico de forma atómica

```hcl
# Terraform: cambiar el target group del listener del ALB de Blue a Green
resource "aws_lb_listener" "main" {
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.green.arn  # antes apuntaba a blue
  }
}
```

O vía Route 53 con política Weighted (ver [`../knowledge/aws-network/route53.md`](../knowledge/aws-network/route53.md)) para una transición más gradual dentro del propio modelo Blue/Green.

### 4. Monitorizar Green intensivamente tras la conmutación

```
Ventana de observación crítica: 15-30 minutos con dashboards abiertos,
comparando métricas de Green contra el comportamiento histórico de Blue
```

### 5. Mantener Blue activo como red de seguridad

Nunca desmantelar Blue inmediatamente tras conmutar — mantenerlo activo (sin tráfico) durante un periodo de observación permite rollback instantáneo con solo revertir el paso 3, sin necesidad de re-desplegar nada.

### 6. Rollback instantáneo si es necesario

```bash
# Revertir el listener a target_group_arn = aws_lb_target_group.blue.arn
```

### 7. Desmantelar Blue solo tras confirmar estabilidad de Green

```bash
aws ecs update-service --cluster main --service pedidos-service-blue --desired-count 0
```

## Checklist final

- [ ] ¿Green se verificó de forma aislada antes de recibir tráfico real?
- [ ] ¿La conmutación de tráfico es atómica (un único cambio, no gradual sin control)?
- [ ] ¿Blue permanece activo como red de seguridad tras la conmutación, no desmantelado inmediatamente?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../decisions/rolling-vs-blue-green.md`](../decisions/rolling-vs-blue-green.md), [`canary-deployment.md`](./canary-deployment.md).
