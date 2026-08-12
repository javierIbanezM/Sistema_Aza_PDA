# Deployment Strategies

> Ver [`../../decisions/rolling-vs-blue-green.md`](../../decisions/rolling-vs-blue-green.md) y comparativas relacionadas para la elección entre estrategias concretas — este documento cubre el criterio general de selección.

## El objetivo común: desplegar sin downtime perceptible, con reversión rápida

Toda estrategia de despliegue moderna busca lo mismo por caminos distintos: que un despliegue no interrumpa el servicio, y que si algo falla, revertir sea rápido y de bajo riesgo — la elección entre estrategias es un trade-off entre coste de infraestructura, complejidad operativa y velocidad de reversión.

## Rolling Update: reemplazo gradual

```
v1 v1 v1 v1 → v2 v1 v1 v1 → v2 v2 v1 v1 → v2 v2 v2 v1 → v2 v2 v2 v2
```

Reemplaza instancias/pods de la versión antigua por la nueva de forma incremental, verificando health checks en cada paso — el enfoque por defecto de Kubernetes y ECS (ver `CLAUDE.md` sección 7.2, [`../aws-compute/ecs.md`](../aws-compute/ecs.md)). Ambas versiones coexisten brevemente durante el despliegue, lo que exige compatibilidad hacia atrás del contrato (ver [`../integration/api-versioning.md`](../integration/api-versioning.md)) si distintas instancias sirven peticiones simultáneamente contra el mismo cliente.

## Blue/Green: dos entornos completos, conmutación instantánea

```
Blue (v1, activo) ←─┐
                     │ conmutación de tráfico instantánea (DNS/Load Balancer)
Green (v2, en espera) ←┘
```

Ver [`../../decisions/rolling-vs-blue-green.md`](../../decisions/rolling-vs-blue-green.md) — rollback prácticamente instantáneo (revertir la conmutación), a costa de mantener el doble de infraestructura activa durante el despliegue.

## Canary: exposición gradual a un subconjunto de tráfico real

```
95% tráfico → v1 (estable)
 5% tráfico → v2 (canary) — se monitoriza intensivamente antes de aumentar el porcentaje
```

Ver [`../../decisions/blue-green-vs-canary.md`](../../decisions/blue-green-vs-canary.md) — permite detectar problemas con exposición real pero acotada, antes de un despliegue completo; requiere capacidad de enrutamiento ponderado (ver [`../aws-network/route53.md`](../aws-network/route53.md), políticas Weighted) y observabilidad para comparar métricas entre versiones.

## Feature Flags: desacoplar despliegue de activación

```java
if (featureFlags.isEnabled("nuevo-checkout", usuario)) {
    return nuevoFlujoCheckout();
}
return flujoCheckoutActual();
```

El código nuevo se despliega (inactivo) independientemente de cuándo se activa para los usuarios — permite desplegar con frecuencia sin exponer funcionalidad incompleta, y activar/desactivar sin un nuevo despliegue ante un problema, complementario (no sustituto) de las estrategias anteriores.

## Checklist

- [ ] ¿La estrategia elegida corresponde al trade-off real entre coste de infraestructura y velocidad de reversión necesaria?
- [ ] ¿El contrato es compatible hacia atrás durante la coexistencia temporal de versiones (Rolling/Canary)?
- [ ] ¿Existe observabilidad suficiente para comparar métricas entre versiones en un despliegue Canary?

Ver también: [`rollback.md`](./rollback.md), [`../../decisions/rolling-vs-blue-green.md`](../../decisions/rolling-vs-blue-green.md), [`../../decisions/blue-green-vs-canary.md`](../../decisions/blue-green-vs-canary.md).
