# Docker Compose vs. Kubernetes

## Contexto

Ambos orquestan múltiples contenedores, con alcance y complejidad radicalmente distintos.

## Comparativa

| Criterio | Docker Compose | Kubernetes |
|---|---|---|
| Cuándo usar | Desarrollo local, entornos pequeños de un único host | Producción con múltiples nodos, alta disponibilidad |
| Complejidad | Baja | Alta |
| Escalabilidad | Limitada a un único host | Horizontal, multi-nodo, multi-región |
| Coste operativo | Mínimo | Significativo (aprendizaje + operación continua) |
| Alta disponibilidad | No — un único host es un único punto de fallo | Nativa, diseñada para ello |
| Autoescalado | No | Nativo (HPA, ver [`../knowledge/devops/orchestration.md`](../knowledge/devops/orchestration.md)) |
| Seguridad de red | Básica (red de Docker) | Avanzada (NetworkPolicy, service mesh opcional) |
| Mantenimiento | Mínimo | Requiere actualizaciones de versión, gestión de manifiestos |

## Cuándo usar Docker Compose

- Entorno de desarrollo local — levantar la aplicación y sus dependencias (base de datos, caché) con un único comando.
- Entornos de demostración/prueba de concepto de corta duración, sin requisito de alta disponibilidad.
- Equipos pequeños sin necesidad ni presupuesto para la complejidad operativa de un orquestador completo.

## Cuándo usar Kubernetes

- Producción con requisito real de alta disponibilidad, autoescalado, y tolerancia a fallo de nodo individual.
- Múltiples servicios que necesitan orquestación coordinada (despliegues, descubrimiento de servicio, gestión de configuración) a escala.
- El equipo ya tiene o va a adquirir la experiencia operativa necesaria (ver [`../knowledge/aws-compute/eks.md`](../knowledge/aws-compute/eks.md) para el mismo análisis aplicado a EKS específicamente).

## Recomendación por defecto de este framework

**Docker Compose para desarrollo local, siempre** — ningún proyecto debería requerir un clúster Kubernetes solo para que un desarrollador levante el entorno en su máquina. **Kubernetes (o ECS, ver [`ecs-vs-eks.md`](./ecs-vs-eks.md)) para producción**, nunca Docker Compose como solución de despliegue productivo — no está diseñado para ese propósito y carece de las garantías de disponibilidad que producción requiere.

Ver también: [`../knowledge/docker/docker.md`](../knowledge/docker/docker.md), [`../knowledge/kubernetes/kubernetes.md`](../knowledge/kubernetes/kubernetes.md), [`ecs-vs-eks.md`](./ecs-vs-eks.md).
