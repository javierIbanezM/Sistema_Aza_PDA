# Docker

> Ver [`../devops/containerization.md`](../devops/containerization.md) para los principios de contenerización (multi-stage, usuario no privilegiado, cacheo de capas) — este documento cubre la sintaxis y herramientas específicas de Docker que implementan esos principios. Ver [`../../decisions/docker-compose-vs-kubernetes.md`](../../decisions/docker-compose-vs-kubernetes.md) para cuándo Docker Compose es suficiente frente a un orquestador completo.

## Dockerfile completo aplicando los principios del framework

```dockerfile
# Etapa de build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline          # cachea dependencias antes de copiar código, ver devops/containerization.md
COPY src ./src
RUN mvn package -DskipTests

# Etapa de runtime, mínima
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O- http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Docker Compose: orquestación local para desarrollo

```yaml
services:
  app:
    build: .
    ports: ["8080:8080"]
    environment:
      - DB_HOST=db
    depends_on:
      db: { condition: service_healthy }

  db:
    image: postgres:16
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD}  # nunca hardcodeado, ver devops/secrets-management.md
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
```

`depends_on` con `condition: service_healthy` espera a que la dependencia esté realmente lista (no solo iniciada) — evita condiciones de carrera al arrancar el entorno local completo.

## `.dockerignore`: reducir el contexto de build

```
node_modules
target
.git
*.log
.env
```

Ver `CLAUDE.md` sección 7.1 — excluir archivos innecesarios (y especialmente sensibles como `.env`) del contexto enviado al daemon de Docker durante el build, reduciendo tiempo de build y evitando que un secreto local termine accidentalmente en la imagen.

## Volúmenes: persistencia y desarrollo con hot-reload

```yaml
volumes:
  - ./src:/app/src        # bind mount para desarrollo: cambios locales reflejados sin rebuild
  - db-data:/var/lib/postgresql/data  # volumen nombrado: persiste datos entre reinicios del contenedor
```

Bind mounts apropiados solo para desarrollo local (nunca en producción, donde la imagen debe ser autocontenida); volúmenes nombrados para persistencia de datos que debe sobrevivir a la recreación del contenedor.

## Checklist

- [ ] ¿El Dockerfile usa multi-stage build según [`../devops/containerization.md`](../devops/containerization.md)?
- [ ] ¿`.dockerignore` excluye archivos sensibles y no necesarios del contexto de build?
- [ ] ¿Docker Compose usa healthchecks con `depends_on: condition: service_healthy` para evitar condiciones de carrera?

Ver también: [`../devops/containerization.md`](../devops/containerization.md), [`../../decisions/docker-compose-vs-kubernetes.md`](../../decisions/docker-compose-vs-kubernetes.md), [`../../recipes/deploy-docker-application.md`](../../recipes/deploy-docker-application.md).
