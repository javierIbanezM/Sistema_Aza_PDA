# Playbook: Deploy Production

## Cuándo usar este playbook

Cualquier despliegue de una versión nueva a producción — el proceso estándar que todo cambio (funcionalidad, corrección, infraestructura) sigue para llegar a producción de forma segura.

## Fases

### 1. Tech Lead / Agente técnico correspondiente

- **Hace**: confirma que el código ha pasado por [`new-feature.md`](./new-feature.md)/[`bug-fix.md`](./bug-fix.md) completo, incluida la aprobación de [`senior-code-reviewer.md`](../agents/senior-code-reviewer.md).
- **Produce**: artefacto/imagen construido y publicado por el pipeline (ver [`../knowledge/devops/artifact-management.md`](../knowledge/devops/artifact-management.md)).
- **Entrega a**: Senior DevOps Engineer.
- **Terminada cuando**: el artefacto pasó el escaneo de vulnerabilidades y está publicado con tag inmutable.

### 2. Senior DevOps Engineer (despliegue a staging)

- **Hace**: despliega a staging siguiendo [`../recipes/deploy-kubernetes-application.md`](../recipes/deploy-kubernetes-application.md) o [`deploy-docker-application.md`](../recipes/deploy-docker-application.md) según la plataforma.
- **Produce**: despliegue en staging verificado con smoke tests.
- **Entrega a**: equipo QA/Tech Lead para validación funcional en staging.
- **Terminada cuando**: staging refleja el comportamiento esperado, sin regresiones detectadas.

### 3. Senior DevOps Engineer (estrategia de despliegue a producción)

- **Hace**: elige la estrategia (Rolling/Blue-Green/Canary, ver [`../knowledge/devops/deployment-strategies.md`](../knowledge/devops/deployment-strategies.md)) según el riesgo del cambio, ejecuta el despliegue con observabilidad activa.
- **Produce**: despliegue en producción, monitorizado durante la ventana crítica post-despliegue.
- **Entrega a**: equipo completo (notificación del despliegue).
- **Terminada cuando**: las métricas post-despliegue están dentro de los umbrales esperados durante el periodo de observación definido.

### 4. Senior Debugger (solo si algo falla)

- **Hace**: si las métricas post-despliegue indican un problema, aplica [`rollback-production.md`](./rollback-production.md).
- **Produce**: servicio estabilizado.
- **Entrega a**: post-mortem si el rollback fue necesario.

## Diagrama de flujo

```
Tech Lead / Agente técnico (artefacto listo — ver docs/orchestrator.md Paso 2b)
        ↓
Senior DevOps Engineer (despliegue a staging + validación)
        ↓
Senior DevOps Engineer (estrategia + despliegue a producción + observación)
        ↓
   ¿métricas dentro de umbral?
        │
   Sí ──┴── No → rollback-production.md → post-mortem
        ↓
   [Despliegue completado]
```

## Validaciones obligatorias antes de avanzar de fase

- Artefacto con escaneo de vulnerabilidades aprobado (fase 1→2).
- Smoke tests en staging exitosos (fase 2→3).
- Métricas post-despliegue dentro de umbral durante la ventana de observación (fase 3→cierre).

## Criterios de finalización

El despliegue se considera completo cuando el 100% del tráfico sirve la nueva versión, las métricas están estables durante el periodo de observación acordado, y el despliegue queda registrado con trazabilidad completa (ver [`../knowledge/devops/release-management.md`](../knowledge/devops/release-management.md)).
