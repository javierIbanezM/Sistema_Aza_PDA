# ECS vs. EKS

## Contexto

Ambos orquestan contenedores en AWS; la elección determina el modelo operativo, la portabilidad y la curva de aprendizaje del equipo durante años, no solo en el momento de la decisión inicial.

## Comparativa

| Criterio | ECS | EKS |
|---|---|---|
| Modelo | Propietario de AWS | Kubernetes estándar, gestionado |
| Curva de aprendizaje | Baja si el equipo ya conoce AWS | Alta si el equipo no tiene experiencia Kubernetes previa |
| Portabilidad entre proveedores cloud | Ninguna — específico de AWS | Alta — el mismo manifiesto funciona en GKE/AKS con ajustes mínimos |
| Ecosistema de herramientas | Más limitado, integrado con AWS | Vastísimo (Helm, operadores, GitOps, service mesh) |
| Coste del plano de control | Sin coste adicional | Coste fijo por clúster además de los nodos |
| Complejidad operativa | Menor | Mayor (upgrades de versión, gestión de manifiestos) |

## Cuándo elegir ECS

- El equipo prioriza simplicidad operativa e integración nativa profunda con el resto del ecosistema AWS (IAM, CloudWatch, ALB) sin necesidad de portabilidad multi-cloud.
- No existe experiencia Kubernetes previa en el equipo y no hay una razón de negocio que justifique adquirirla.
- El proyecto es exclusivamente AWS sin planes de multi-cloud ni necesidad de herramientas específicas del ecosistema Kubernetes.

## Cuándo elegir EKS

- Estrategia multi-cloud explícita o requisito de portabilidad entre proveedores.
- El equipo ya tiene experiencia y tooling Kubernetes consolidado (Helm charts, operadores) que no quiere reconstruir en un modelo propietario.
- Se necesitan operadores/CRDs específicos del ecosistema Kubernetes sin equivalente directo en ECS (cert-manager, Istio, Argo CD).

## Recomendación por defecto de este framework

**ECS con Fargate por defecto** para equipos sin experiencia Kubernetes previa que operan exclusivamente en AWS — menor complejidad operativa para el mismo resultado funcional en la mayoría de casos de aplicaciones empresariales estándar; **EKS** cuando la portabilidad multi-cloud o el ecosistema Kubernetes específico son requisitos confirmados, no aspiraciones especulativas (ver [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).

Ver también: [`../knowledge/aws-compute/ecs.md`](../knowledge/aws-compute/ecs.md), [`../knowledge/aws-compute/eks.md`](../knowledge/aws-compute/eks.md), [`ec2-vs-lambda.md`](./ec2-vs-lambda.md).
