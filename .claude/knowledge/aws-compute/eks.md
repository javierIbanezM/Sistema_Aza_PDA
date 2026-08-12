# EKS (Elastic Kubernetes Service)

> Ver [`../../decisions/ecs-vs-eks.md`](../../decisions/ecs-vs-eks.md) para la comparativa con ECS.

## Qué es: Kubernetes gestionado

AWS gestiona el plano de control de Kubernetes (API server, etcd) con alta disponibilidad; el equipo gestiona los nodos worker (o usa Fargate para EKS, sin gestionar nodos en absoluto) y despliega cargas siguiendo el modelo estándar de Kubernetes — Deployments, Services, Ingress, ConfigMaps, mismo modelo que `CLAUDE.md` sección 7.2 ya documenta a nivel de framework, independiente de que el clúster corra en AWS, on-premise o cualquier otro proveedor.

## Por qué elegir EKS sobre ECS

- El equipo ya tiene experiencia y tooling Kubernetes (Helm, operadores, GitOps) que no quiere reconstruir en un modelo propietario.
- Estrategia multi-cloud o portabilidad explícita entre proveedores — el mismo manifiesto Kubernetes funciona en EKS, GKE, AKS con ajustes mínimos, a diferencia de ECS que es específico de AWS.
- Necesidad de operadores/CRDs del ecosistema Kubernetes (cert-manager, Istio, Argo CD) sin equivalente directo en ECS.

## Coste operativo real a considerar

EKS tiene un coste fijo por clúster (plano de control) además del coste de los nodos — y requiere mantener actualizado el conocimiento operativo de Kubernetes (upgrades de versión, gestión de manifiestos, troubleshooting de un sistema más complejo) que ECS no exige en la misma medida. Evaluar si ese coste operativo adicional está justificado por los beneficios reales del ecosistema Kubernetes para el caso concreto, no elegido por defecto/familiaridad sin ese análisis.

## Nodos: gestionados, autogestionados o Fargate

| Modo | Quién gestiona el parcheo/escalado de nodos |
|---|---|
| Managed Node Groups | AWS gestiona el ciclo de vida, el equipo elige tipo de instancia |
| Self-managed | El equipo gestiona todo el ciclo de vida de los nodos EC2 |
| Fargate para EKS | Sin nodos que gestionar, cada pod corre en su propio entorno serverless |

Managed Node Groups como punto de partida razonable para la mayoría de casos — Fargate para EKS cuando se quiere evitar completamente la gestión de nodos, con el mismo trade-off de coste que Fargate en ECS.

## IRSA (IAM Roles for Service Accounts): permisos por pod, no por nodo

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: pedidos-service-account
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789:role/pedidos-service-role
```

Sin IRSA, los permisos IAM se aplican a nivel de nodo, compartidos por todos los pods que corren en él — violando el principio de menor privilegio (ver `CLAUDE.md` sección 12, y [`../aws-security/iam.md`](../aws-security/iam.md)). IRSA asigna un rol IAM específico por Service Account de Kubernetes, permitiendo que cada aplicación tenga exactamente los permisos que necesita, no los de todo lo que comparte el mismo nodo.

## Checklist

- [ ] ¿Se ha justificado explícitamente EKS sobre ECS (portabilidad, ecosistema Kubernetes específico), no elegido por defecto?
- [ ] ¿El equipo tiene o va a adquirir el conocimiento operativo de Kubernetes que EKS requiere?
- [ ] ¿Se usa IRSA para permisos por pod, no permisos amplios compartidos a nivel de nodo?

Ver también: [`ecs.md`](./ecs.md), [`../../decisions/ecs-vs-eks.md`](../../decisions/ecs-vs-eks.md), [`../aws-security/iam.md`](../aws-security/iam.md), `CLAUDE.md` sección 7.2.
