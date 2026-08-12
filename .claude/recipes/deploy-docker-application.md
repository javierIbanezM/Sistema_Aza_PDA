# Recipe: Desplegar una Aplicación con Docker

Aplica: [`../knowledge/docker/docker.md`](../knowledge/docker/docker.md), [`../knowledge/devops/containerization.md`](../knowledge/devops/containerization.md), [`../knowledge/devops/secrets-management.md`](../knowledge/devops/secrets-management.md).

## Pasos

### 1. Dockerfile multi-stage con usuario no privilegiado

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O- http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. `.dockerignore` antes de construir

```
target
.git
.env
*.log
```

### 3. Build con tag inmutable

```bash
docker build -t pedidos-service:$(git rev-parse --short HEAD) .
```

Ver [`../knowledge/devops/artifact-management.md`](../knowledge/devops/artifact-management.md) — nunca `latest` para el artefacto que se va a desplegar.

### 4. Escaneo de vulnerabilidades antes de publicar

```bash
trivy image pedidos-service:$(git rev-parse --short HEAD) --severity CRITICAL,HIGH --exit-code 1
```

### 5. Publicar al registro

```bash
docker tag pedidos-service:$(git rev-parse --short HEAD) $ECR_REPO:$(git rev-parse --short HEAD)
docker push $ECR_REPO:$(git rev-parse --short HEAD)
```

### 6. Desplegar inyectando configuración/secretos en tiempo de ejecución

```bash
docker run -d \
  -e DB_HOST=db.interno \
  -e DB_PASSWORD_FILE=/run/secrets/db_password \
  --secret db_password \
  -p 8080:8080 \
  $ECR_REPO:$(git rev-parse --short HEAD)
```

Ver [`../knowledge/devops/secrets-management.md`](../knowledge/devops/secrets-management.md) — nunca `-e DB_PASSWORD=valor` directo, que expondría el secreto en `docker inspect`/histórico de comandos.

### 7. Verificar el health check tras el despliegue

```bash
docker inspect --format='{{.State.Health.Status}}' <container_id>
```

## Checklist final

- [ ] ¿La imagen usa multi-stage build y usuario no privilegiado?
- [ ] ¿El tag es inmutable (SHA de commit), nunca `latest`?
- [ ] ¿Pasó el escaneo de vulnerabilidades antes de publicarse?
- [ ] ¿Los secretos se inyectan en tiempo de ejecución, nunca en variables de entorno planas expuestas?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/devops/Dockerfile`](../snippets/devops/Dockerfile), [`../snippets/devops/docker-compose.yml`](../snippets/devops/docker-compose.yml).
