# Containerization

> Ver `CLAUDE.md` sección 7.1 para las reglas base de Docker ya fijadas a nivel de framework — este documento profundiza en la disciplina de contenerización como parte de la estrategia DevOps. Ver [`../docker/docker.md`](../docker/docker.md) para la sintaxis y herramientas específicas de Docker.

## Por qué contenerizar: consistencia entre entornos

Un contenedor empaqueta la aplicación junto con sus dependencias exactas de runtime — elimina la clase de problema "funciona en mi máquina" al garantizar que el mismo artefacto (imagen) se ejecuta de forma idéntica en dev, staging y producción, mismo principio que [`../devops/artifact-management.md`](./artifact-management.md) exige para cualquier artefacto desplegable.

## Multi-stage builds: separar build-time de runtime

```
Etapa 1 (build): imagen completa con compilador/build tools, genera el artefacto
Etapa 2 (runtime): imagen mínima, copia SOLO el artefacto generado de la etapa 1
```

Ver `CLAUDE.md` sección 7.1 — la imagen final de producción no contiene compiladores, herramientas de build ni código fuente, solo lo estrictamente necesario para ejecutar — reduce superficie de ataque y tamaño de imagen significativamente.

## Imágenes base: minimalismo con criterio

```
distroless / alpine (mínima superficie de ataque, sin shell ni herramientas innecesarias)
vs. imagen completa de la distribución (más fácil de depurar interactivamente, mayor superficie)
```

Preferir imágenes base mínimas para producción; una imagen completa con herramientas de depuración puede ser aceptable temporalmente en un contenedor de diagnóstico separado, nunca como base permanente de la imagen de producción.

## Usuario no privilegiado: nunca root en el contenedor

```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

Ver `CLAUDE.md` sección 7.1 — ejecutar como root dentro del contenedor amplía innecesariamente el impacto potencial de una vulnerabilidad de escape de contenedor; crear y usar un usuario no privilegiado dedicado es una práctica estándar sin coste real.

## Capas de imagen: orden importa para el cacheo de build

```dockerfile
# Copiar primero lo que cambia con MENOS frecuencia (dependencias)
COPY package.json package-lock.json ./
RUN npm ci
# Copiar DESPUÉS lo que cambia con MÁS frecuencia (código fuente)
COPY . .
```

Docker cachea cada capa (instrucción) del Dockerfile — si las dependencias se instalan antes de copiar el código fuente, un cambio de código no invalida la capa de instalación de dependencias, acelerando significativamente builds incrementales.

## Health checks integrados en la imagen

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/health || exit 1
```

Complementa (no sustituye) el health check a nivel de orquestador (ver [`orchestration.md`](./orchestration.md), ECS/Kubernetes) — permite que herramientas que interactúan directamente con Docker (sin pasar por el orquestador) también puedan verificar el estado del contenedor.

## Checklist

- [ ] ¿La imagen usa multi-stage build, sin herramientas de compilación en la imagen final de producción?
- [ ] ¿El contenedor ejecuta como usuario no privilegiado, nunca root?
- [ ] ¿El orden de las instrucciones del Dockerfile aprovecha el cacheo de capas (dependencias antes que código)?
- [ ] ¿Existe un `HEALTHCHECK` integrado en la imagen?

Ver también: [`../docker/docker.md`](../docker/docker.md), [`orchestration.md`](./orchestration.md), `CLAUDE.md` sección 7.1.
