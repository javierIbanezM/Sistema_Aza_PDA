# ECS (Elastic Container Service)

> Ver [`../../decisions/ecs-vs-eks.md`](../../decisions/ecs-vs-eks.md) para la comparativa con EKS.

## Qué es: orquestación de contenedores nativa de AWS

Ejecuta contenedores Docker sin la complejidad operativa de gestionar Kubernetes — apropiado para equipos que quieren orquestación de contenedores con integración nativa profunda al resto del ecosistema AWS (IAM, CloudWatch, ALB) sin asumir la curva de aprendizaje de Kubernetes.

## Fargate vs. EC2 como tipo de lanzamiento

| | Fargate (serverless) | EC2 (instancias gestionadas por el usuario) |
|---|---|---|
| Gestión de infraestructura subyacente | Ninguna — AWS la gestiona | El equipo gestiona parcheo/escalado de las instancias |
| Coste | Mayor por unidad de cómputo | Menor, especialmente con Reserved/Spot |
| Control granular (kernel, drivers) | Limitado | Total |
| Apropiado para | La mayoría de cargas de aplicación estándar | Cargas con requisitos específicos de hardware/kernel, o alta densidad de contenedores por coste |

Fargate como opción por defecto salvo una razón concreta (coste a gran escala, requisito de hardware específico) que justifique gestionar las instancias EC2 subyacentes.

## Task Definition: la unidad de despliegue

```json
{
  "family": "pedidos-service",
  "containerDefinitions": [{
    "name": "app",
    "image": "123456789.dkr.ecr.region.amazonaws.com/pedidos:v1.4.2",
    "memory": 512,
    "cpu": 256,
    "healthCheck": { "command": ["CMD-SHELL", "curl -f http://localhost:8080/health || exit 1"] }
  }]
}
```

`memory`/`cpu` explícitos por contenedor (ver `CLAUDE.md` sección 7.2, mismo principio de requests/limits de Kubernetes) — nunca sin límites, y `healthCheck` obligatorio para que ECS pueda detectar y reemplazar contenedores no saludables automáticamente.

## Service: mantiene el número deseado de tasks en ejecución

```
Servicio "pedidos-service": desiredCount=3, con Auto Scaling basado en CPU/memoria/métrica custom
Rolling deployment: reemplaza tasks gradualmente, nunca todas a la vez
```

Un Service de ECS es el equivalente al Deployment de Kubernetes — mantiene el número deseado de instancias del contenedor, reemplaza automáticamente las que fallan el health check, y gestiona despliegues graduales.

## Checklist

- [ ] ¿Se ha evaluado Fargate como opción por defecto antes de gestionar instancias EC2 manualmente?
- [ ] ¿Cada Task Definition especifica `memory`/`cpu` explícitos, sin dejar sin límites?
- [ ] ¿Existe un `healthCheck` que permita a ECS detectar y reemplazar contenedores no saludables?
- [ ] ¿El servicio tiene `desiredCount` ≥ 2 para alta disponibilidad, no una única task sin redundancia?

Ver también: [`eks.md`](./eks.md), [`../../decisions/ecs-vs-eks.md`](../../decisions/ecs-vs-eks.md), `CLAUDE.md` sección 7.
