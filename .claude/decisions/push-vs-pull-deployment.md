# Push vs. Pull Deployment

## Contexto

Dos modelos opuestos de quién inicia el despliegue: el pipeline de CI "empuja" el cambio hacia el entorno destino, o un agente dentro del entorno destino "tira" (pull) el cambio cuando detecta una nueva versión disponible.

## Comparativa

| Criterio | Push (CI despliega directamente) | Pull (GitOps: un agente en el clúster sincroniza) |
|---|---|---|
| Credenciales de despliegue | El pipeline de CI necesita acceso directo al entorno destino | El clúster solo necesita acceso de lectura al repositorio de manifiestos, ninguna credencial de despliegue sale del clúster |
| Superficie de ataque | Mayor — credenciales de despliegue en el sistema de CI, potencialmente externo | Menor — ninguna credencial privilegiada fuera del propio clúster |
| Fuente de verdad del estado desplegado | El historial de ejecuciones del pipeline de CI | El repositorio Git de manifiestos, siempre reflejando el estado real (reconciliación continua) |
| Detección de drift (cambios manuales no versionados) | No detecta — el pipeline solo actúa cuando se ejecuta | Sí — el agente reconcilia continuamente, revirtiendo cualquier cambio manual no versionado |
| Complejidad de configuración inicial | Menor | Mayor — requiere un agente GitOps (Argo CD, Flux) desplegado y configurado |
| Velocidad de despliegue | Inmediata al ejecutar el pipeline | Depende del intervalo de sincronización del agente (configurable, normalmente segundos) |

## Cuándo elegir Push

- Equipos pequeños o proyectos donde la simplicidad de configuración pesa más que la reducción de superficie de ataque.
- Entornos donde la latencia de sincronización de un modelo pull no es aceptable y se necesita control preciso del momento exacto de despliegue.

## Cuándo elegir Pull (GitOps)

- Seguridad como prioridad: eliminar credenciales de despliegue privilegiadas del sistema de CI, especialmente si es un SaaS externo (ver `CLAUDE.md` sección 12.2).
- Se valora la detección/corrección automática de drift — cualquier cambio manual no versionado en el clúster se revierte automáticamente a lo que el repositorio Git declara.
- El repositorio Git de manifiestos como única fuente de verdad auditable de qué está desplegado, con historial completo vía `git log`.

## Recomendación por defecto de este framework

**Pull (GitOps) para clústeres Kubernetes de producción** — la reducción de superficie de ataque (ninguna credencial de despliegue privilegiada sale del clúster) y la detección automática de drift son beneficios significativos que compensan la complejidad adicional de configurar un agente GitOps; **Push para el resto de casos** (Lambda, ECS sin GitOps configurado, entornos no-Kubernetes) donde el modelo pull no aporta el mismo beneficio proporcional a su complejidad.

Ver también: [`../knowledge/devops/ci-cd.md`](../knowledge/devops/ci-cd.md), [`../knowledge/kubernetes/kubernetes.md`](../knowledge/kubernetes/kubernetes.md), [`../knowledge/devops/secrets-management.md`](../knowledge/devops/secrets-management.md).
