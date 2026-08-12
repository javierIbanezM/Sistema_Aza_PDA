# Blue/Green vs. Canary

## Contexto

Ambas son estrategias de despliegue orientadas a minimizar riesgo (ver [`../knowledge/devops/deployment-strategies.md`](../knowledge/devops/deployment-strategies.md)), con enfoques distintos de exposición al cambio.

## Comparativa

| Criterio | Blue/Green | Canary |
|---|---|---|
| Exposición al cambio | Instantánea (100% de golpe tras conmutar) | Gradual (empieza con un % pequeño) |
| Velocidad de rollback | Instantánea (revertir la conmutación) | Rápida, pero requiere revertir el enrutamiento gradual |
| Coste de infraestructura | Alto — doble infraestructura activa durante el despliegue | Bajo — no requiere duplicar toda la infraestructura |
| Detección temprana de problemas | Baja — el problema se manifiesta con el 100% del tráfico de golpe | Alta — el problema se detecta con una fracción pequeña de tráfico real |
| Complejidad de implementación | Media — requiere mecanismo de conmutación de tráfico completo | Alta — requiere enrutamiento ponderado y comparación de métricas entre versiones |
| Apropiado para cambios de alto riesgo | Menos — no hay "prueba" gradual con tráfico real | Más — permite limitar el radio de impacto de un problema no detectado en tests |

## Cuándo elegir Blue/Green

- Se prioriza velocidad de rollback instantánea sobre coste de infraestructura duplicada.
- El cambio ya tiene alta confianza (extensamente testeado) y el riesgo residual es bajo — no se necesita "probar" gradualmente con tráfico real.
- Cambios que no se prestan bien a coexistencia parcial (ej. una migración de esquema de base de datos incompatible entre versiones).

## Cuándo elegir Canary

- Cambios de alto riesgo donde detectar un problema con el 5% del tráfico es preferible a descubrirlo con el 100%.
- Se dispone de observabilidad suficiente (ver [`../knowledge/devops/monitoring.md`](../knowledge/devops/monitoring.md)) para comparar métricas entre la versión estable y la candidata de forma fiable.
- El coste de mantener infraestructura duplicada completa (Blue/Green) no está justificado, y el enrutamiento ponderado gradual sí es viable técnicamente.

## Recomendación por defecto de este framework

**Rolling Update (ver [`rolling-vs-blue-green.md`](./rolling-vs-blue-green.md)) para el caso estándar de bajo-medio riesgo**; **Canary para cambios de alto riesgo de negocio** (ver ejemplo en [`../knowledge/devops/examples.md`](../knowledge/devops/examples.md)) donde vale la pena la complejidad adicional de enrutamiento ponderado; **Blue/Green** cuando se necesita rollback instantáneo garantizado y el coste de infraestructura duplicada temporal es aceptable, típicamente para despliegues poco frecuentes de alto impacto.

Ver también: [`../knowledge/devops/deployment-strategies.md`](../knowledge/devops/deployment-strategies.md), [`rolling-vs-blue-green.md`](./rolling-vs-blue-green.md), [`../recipes/blue-green-deployment.md`](../recipes/blue-green-deployment.md), [`../recipes/canary-deployment.md`](../recipes/canary-deployment.md).
