# Rolling Update vs. Blue/Green

## Contexto

Ambas reemplazan la versión en ejecución sin downtime completo; difieren en cómo gestionan la coexistencia temporal de versiones.

## Comparativa

| Criterio | Rolling Update | Blue/Green |
|---|---|---|
| Coexistencia de versiones | Sí, durante el despliegue ambas atienden tráfico simultáneamente | No — la conmutación es completa e instantánea |
| Coste de infraestructura | Mínimo — reemplazo incremental sobre la misma capacidad | Alto — el doble de infraestructura durante el despliegue |
| Requisito de compatibilidad de contrato | Alto — el contrato debe soportar ambas versiones simultáneamente (ver [`../knowledge/integration/api-versioning.md`](../knowledge/integration/api-versioning.md)) | Bajo — solo una versión sirve tráfico en cada momento |
| Velocidad de rollback | Requiere revertir el despliegue incremental (algo más lento) | Instantánea |
| Soporte nativo en orquestadores | Sí, por defecto en Kubernetes/ECS | Requiere configuración adicional (dos entornos + mecanismo de conmutación) |
| Apropiado para cambios de esquema de base de datos incompatibles | No directamente — requiere el patrón expansivo/contractivo (ver [`../knowledge/devops/rollback.md`](../knowledge/devops/rollback.md)) | Más simple si el cambio de datos también puede conmutarse atómicamente |

## Cuándo elegir Rolling Update

- El caso estándar para la mayoría de despliegues — bajo coste, soporte nativo del orquestador, sin necesidad de infraestructura adicional.
- El equipo ya sigue la disciplina de contratos compatibles hacia atrás (ver `CLAUDE.md` sección 15) que Rolling Update requiere durante la coexistencia temporal.

## Cuándo elegir Blue/Green

- El cambio no puede coexistir de forma segura entre versiones (una migración de datos incompatible, un cambio de protocolo no retrocompatible).
- Se necesita garantía de rollback instantáneo sin la ventana de tiempo que un Rolling Update revertido requiere.
- El coste de infraestructura duplicada temporal es aceptable para el beneficio de conmutación atómica.

## Recomendación por defecto de este framework

**Rolling Update como estrategia por defecto** para la gran mayoría de despliegues — es la opción nativa de los orquestadores que este framework ya usa (ECS/Kubernetes, ver `CLAUDE.md` sección 7.2), de menor coste operativo, y compatible con la disciplina de contratos retrocompatibles que el framework exige de todas formas (ver [`../standards/api.md`](../standards/api.md)); **Blue/Green solo ante un requisito específico** que Rolling Update no puede satisfacer (incompatibilidad genuina entre versiones, requisito estricto de rollback instantáneo).

Ver también: [`../knowledge/devops/deployment-strategies.md`](../knowledge/devops/deployment-strategies.md), [`blue-green-vs-canary.md`](./blue-green-vs-canary.md), [`../recipes/blue-green-deployment.md`](../recipes/blue-green-deployment.md).
