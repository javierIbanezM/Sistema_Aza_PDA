# Monorepo vs. Multirepo

## Contexto

Decisión sobre cómo organizar el código fuente de múltiples servicios/aplicaciones — afecta directamente al diseño del pipeline de CI/CD (ver [`../knowledge/devops/ci-cd.md`](../knowledge/devops/ci-cd.md)) y a la gestión de versiones entre servicios.

## Comparativa

| Criterio | Monorepo | Multirepo |
|---|---|---|
| Cambios que cruzan múltiples servicios | Un único commit/PR, atómico | Requiere coordinar múltiples PRs en distintos repositorios |
| Visibilidad del código de otros equipos | Total, todo en el mismo repositorio | Limitada a lo que cada equipo decide exponer |
| Aislamiento de permisos de acceso | Más difícil (requiere herramientas de permisos granulares por carpeta) | Nativo por repositorio |
| Complejidad del pipeline de CI | Alta — necesita detectar qué partes cambiaron para no rebuildar todo | Baja — cada repositorio tiene su propio pipeline independiente |
| Versionado entre servicios internos | Implícito — todos viven en el mismo commit/versión del repositorio | Explícito — cada servicio versiona y publica sus artefactos independientemente |
| Escalabilidad de herramientas (tiempo de clone, indexado del IDE) | Puede degradarse a gran escala sin herramientas especializadas (Bazel, Nx) | Cada repositorio permanece manejable individualmente |

## Cuándo elegir Monorepo

- Cambios frecuentes que cruzan múltiples servicios relacionados (ej. un cambio de contrato entre un servicio y sus consumidores internos) se benefician de la atomicidad de un único commit.
- El equipo prioriza visibilidad total del código y facilidad de refactors cruzados sobre aislamiento estricto de repositorios.
- Se dispone (o se está dispuesto a adoptar) de herramientas de build incrementales (Bazel, Nx, Turborepo) que evitan rebuildar todo el monorepo ante cualquier cambio pequeño.

## Cuándo elegir Multirepo

- Equipos con alto grado de autonomía, cada uno dueño completo de su ciclo de vida (versionado, despliegue) sin coordinación necesaria con otros.
- Requisitos de aislamiento de acceso estrictos entre equipos/servicios (código que un equipo no debe poder ver ni modificar).
- El número de servicios y su interdependencia es lo suficientemente bajo como para que la coordinación entre repositorios no sea una fricción significativa.

## Recomendación por defecto de este framework

**Multirepo por defecto** — más simple de operar sin herramientas adicionales especializadas, aislamiento natural de permisos, y pipelines de CI más simples por repositorio (mismo principio de simplicidad que [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)); **Monorepo** cuando el número de cambios cruzados entre servicios es alto y genuinamente se beneficia de atomicidad, y el equipo está dispuesto a invertir en las herramientas de build incrementales necesarias para que escale.

Ver también: [`../knowledge/devops/ci-cd.md`](../knowledge/devops/ci-cd.md), [`../standards/git.md`](../standards/git.md).
