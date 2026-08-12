# Recipe: Desplegar una Aplicación en Kubernetes

Aplica: [`../knowledge/kubernetes/kubernetes.md`](../knowledge/kubernetes/kubernetes.md), [`../knowledge/devops/orchestration.md`](../knowledge/devops/orchestration.md), [`../decisions/helm-vs-kustomize.md`](../decisions/helm-vs-kustomize.md).

## Pasos

### 1. Deployment con requests/limits y probes diferenciados

```yaml
apiVersion: apps/v1
kind: Deployment
metadata: { name: pedidos-service }
spec:
  replicas: 2
  selector: { matchLabels: { app: pedidos-service } }
  template:
    metadata: { labels: { app: pedidos-service } }
    spec:
      securityContext: { runAsNonRoot: true }
      containers:
        - name: app
          image: 123456789.dkr.ecr.region.amazonaws.com/pedidos-service:abc123f
          resources:
            requests: { cpu: "250m", memory: "256Mi" }
            limits:   { cpu: "500m", memory: "512Mi" }
          readinessProbe: { httpGet: { path: /health/ready, port: 8080 }, initialDelaySeconds: 5 }
          livenessProbe:  { httpGet: { path: /health/live, port: 8080 }, initialDelaySeconds: 15 }
          envFrom:
            - secretRef: { name: pedidos-secrets }
```

### 2. Service para descubrimiento estable

```yaml
apiVersion: v1
kind: Service
metadata: { name: pedidos-service }
spec:
  selector: { app: pedidos-service }
  ports: [{ port: 80, targetPort: 8080 }]
```

### 3. HorizontalPodAutoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata: { name: pedidos-service }
spec:
  scaleTargetRef: { apiVersion: apps/v1, kind: Deployment, name: pedidos-service }
  minReplicas: 2
  maxReplicas: 10
  metrics: [{ type: Resource, resource: { name: cpu, target: { type: Utilization, averageUtilization: 70 } } }]
```

### 4. NetworkPolicy: solo tráfico esperado

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata: { name: pedidos-service-policy }
spec:
  podSelector: { matchLabels: { app: pedidos-service } }
  ingress: [{ from: [{ podSelector: { matchLabels: { app: api-gateway } } }] }]
```

### 5. Gestionar variaciones de entorno con Kustomize

```yaml
# base/kustomization.yaml
resources: [deployment.yaml, service.yaml, hpa.yaml, networkpolicy.yaml]

# overlays/production/kustomization.yaml
resources: [../../base]
patches:
  - patch: |-
      - op: replace
        path: /spec/replicas
        value: 4
    target: { kind: Deployment, name: pedidos-service }
```

Ver [`../decisions/helm-vs-kustomize.md`](../decisions/helm-vs-kustomize.md) — Kustomize para manifiestos propios, Helm si se instalan dependencias de terceros.

### 6. Aplicar y verificar

```bash
kubectl apply -k overlays/production/
kubectl rollout status deployment/pedidos-service
kubectl get pods -l app=pedidos-service  # verificar READY, no CrashLoopBackOff
```

## Checklist final

- [ ] ¿`requests`/`limits` y `readinessProbe`/`livenessProbe` están definidos y diferenciados?
- [ ] ¿Existe `NetworkPolicy` restringiendo tráfico entre servicios no relacionados?
- [ ] ¿Los secretos se referencian vía `secretRef`, nunca en texto plano en el manifiesto?
- [ ] ¿`kubectl rollout status` confirma el despliegue exitoso antes de dar la tarea por terminada?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/devops/deployment.yaml`](../snippets/devops/deployment.yaml), [`../snippets/devops/service.yaml`](../snippets/devops/service.yaml).
