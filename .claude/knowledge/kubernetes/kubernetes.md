# Kubernetes

> Ver [`../devops/orchestration.md`](../devops/orchestration.md) para los principios de orquestación (requests/limits, readiness/liveness, autoescalado) — este documento cubre los manifiestos y herramientas específicas de Kubernetes que implementan esos principios. Ver [`../aws-compute/eks.md`](../aws-compute/eks.md) para Kubernetes gestionado en AWS, y [`../../decisions/helm-vs-kustomize.md`](../../decisions/helm-vs-kustomize.md) para la gestión de manifiestos.

## Deployment con requests/limits y probes completos

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pedidos-service
spec:
  replicas: 2
  selector: { matchLabels: { app: pedidos-service } }
  template:
    metadata: { labels: { app: pedidos-service } }
    spec:
      containers:
        - name: app
          image: 123456789.dkr.ecr.region.amazonaws.com/pedidos-service:abc123f
          resources:
            requests: { cpu: "250m", memory: "256Mi" }
            limits:   { cpu: "500m", memory: "512Mi" }
          readinessProbe:
            httpGet: { path: /health/ready, port: 8080 }
            initialDelaySeconds: 5
          livenessProbe:
            httpGet: { path: /health/live, port: 8080 }
            initialDelaySeconds: 15
      securityContext:
        runAsNonRoot: true    # mismo principio que devops/containerization.md
```

## Service: descubrimiento estable dentro del clúster

```yaml
apiVersion: v1
kind: Service
metadata:
  name: pedidos-service
spec:
  selector: { app: pedidos-service }
  ports: [{ port: 80, targetPort: 8080 }]
```

Un `Service` provee un nombre DNS y una IP estables para un conjunto de pods que cambian dinámicamente (escalado, reemplazo por fallo) — el resto del clúster se conecta al Service, nunca directamente a la IP de un pod individual.

## ConfigMap y Secret: configuración según sensibilidad

```yaml
apiVersion: v1
kind: ConfigMap
metadata: { name: pedidos-config }
data:
  LOG_LEVEL: "INFO"
---
apiVersion: v1
kind: Secret
metadata: { name: pedidos-secrets }
type: Opaque
stringData:
  DB_PASSWORD: "${DB_PASSWORD}"  # inyectado en tiempo de despliegue, ver devops/secrets-management.md
```

`ConfigMap` para configuración no sensible, `Secret` para datos sensibles — ver `CLAUDE.md` sección 7.2. En producción, preferir Secrets externos gestionados (External Secrets Operator sincronizando desde AWS Secrets Manager) sobre `Secret` nativo, cuyo contenido solo está codificado en base64, no cifrado en reposo por defecto.

## HorizontalPodAutoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata: { name: pedidos-service }
spec:
  scaleTargetRef: { apiVersion: apps/v1, kind: Deployment, name: pedidos-service }
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource: { name: cpu, target: { type: Utilization, averageUtilization: 70 } }
```

## Ingress: enrutamiento HTTP hacia los Services

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: pedidos-ingress
  annotations: { kubernetes.io/ingress.class: alb }
spec:
  rules:
    - http:
        paths:
          - path: /api/pedidos
            pathType: Prefix
            backend: { service: { name: pedidos-service, port: { number: 80 } } }
```

## NetworkPolicy: aislamiento entre namespaces

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata: { name: solo-desde-gateway }
spec:
  podSelector: { matchLabels: { app: pedidos-service } }
  ingress:
    - from: [{ podSelector: { matchLabels: { app: api-gateway } } }]
```

Ver `CLAUDE.md` sección 7.2 — sin `NetworkPolicy`, cualquier pod del clúster puede conectarse a cualquier otro por defecto; restringir explícitamente el tráfico permitido entre servicios no relacionados.

## Checklist

- [ ] ¿Todo Deployment tiene `requests`/`limits` y `readinessProbe`/`livenessProbe` diferenciados?
- [ ] ¿Los datos sensibles usan `Secret` (idealmente sincronizado externamente), nunca `ConfigMap`?
- [ ] ¿Existe `NetworkPolicy` restringiendo tráfico entre servicios no relacionados?

Ver también: [`../devops/orchestration.md`](../devops/orchestration.md), [`../../decisions/helm-vs-kustomize.md`](../../decisions/helm-vs-kustomize.md), [`../../recipes/deploy-kubernetes-application.md`](../../recipes/deploy-kubernetes-application.md).
