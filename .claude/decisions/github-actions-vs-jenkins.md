# GitHub Actions vs. Jenkins

## Contexto

Ambos ejecutan pipelines de CI/CD (ver [`../knowledge/devops/ci-cd.md`](../knowledge/devops/ci-cd.md)); GitHub Actions es un SaaS integrado con GitHub, Jenkins es autogestionado y agnóstico del proveedor de repositorio.

## Comparativa

| Criterio | GitHub Actions | Jenkins |
|---|---|---|
| Gestión de infraestructura | Ninguna (runners gestionados) o propia (self-hosted runners opcional) | El equipo gestiona el servidor Jenkins completo |
| Integración con el repositorio | Nativa, sin configuración adicional si ya se usa GitHub | Requiere webhooks/plugins de integración |
| Coste | Incluido con GitHub (con límites en el plan gratuito), pago por minutos adicionales | Sin coste de licencia, pero coste de infraestructura + mantenimiento propio |
| Ecosistema de plugins/actions | Marketplace amplio y creciente | Ecosistema de plugins muy maduro y extenso, décadas de desarrollo |
| Portabilidad si se cambia de proveedor de repositorio | Baja — específico de GitHub | Alta — funciona independientemente del proveedor de repositorio |
| Curva de aprendizaje | Baja, YAML declarativo simple | Media-alta, especialmente Jenkinsfile con Groovy para pipelines complejos |
| Mantenimiento operativo | Ninguno (SaaS gestionado) | Significativo — actualizaciones, plugins, escalado del propio Jenkins |

## Cuándo elegir GitHub Actions

- El repositorio ya vive en GitHub — la integración nativa elimina fricción de configuración.
- Se prioriza cero mantenimiento operativo de la plataforma de CI/CD en sí.
- Equipos pequeños/medianos sin necesidad de pipelines extremadamente complejos o personalizados más allá de lo que el modelo de Actions soporta bien.

## Cuándo elegir Jenkins

- Necesidad de portabilidad del sistema de CI/CD independiente del proveedor de repositorio (migración futura, múltiples repositorios en distintas plataformas).
- Pipelines muy complejos y altamente personalizados donde el ecosistema maduro de plugins de Jenkins resuelve necesidades específicas no cubiertas por Actions.
- Restricciones que exigen CI/CD completamente autogestionado on-premise, sin dependencia de un servicio SaaS externo.

## Recomendación por defecto de este framework

**GitHub Actions por defecto** para proyectos que ya usan GitHub como repositorio (la mayoría dentro de este framework, ver `CLAUDE.md` sección 9.2) — menor fricción, cero mantenimiento operativo de la plataforma de CI en sí; **Jenkins** solo cuando existe un requisito confirmado de portabilidad multi-plataforma de repositorio o de CI/CD completamente on-premise sin dependencia SaaS.

Ver también: [`../knowledge/github-actions/github-actions.md`](../knowledge/github-actions/github-actions.md), [`../knowledge/devops/ci-cd.md`](../knowledge/devops/ci-cd.md).
