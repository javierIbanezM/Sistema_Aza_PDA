# Playbook: Release Management

## Cuándo usar este playbook

Coordinar una release que agrupa múltiples cambios (no un único hotfix o feature aislada) — típico en cadencia de release programada (ver [`../knowledge/devops/release-management.md`](../knowledge/devops/release-management.md)).

## Fases

### 1. Tech Lead

- **Hace**: consolida qué cambios (features/fixes ya mergeados a main) entran en la release, verificando que cada uno completó su propio playbook individual ([`new-feature.md`](./new-feature.md)/[`bug-fix.md`](./bug-fix.md)).
- **Produce**: alcance de la release definido, con versión semántica calculada (ver [`../knowledge/devops/release-management.md`](../knowledge/devops/release-management.md)).
- **Entrega a**: Senior Business Analyst para las release notes de negocio.

### 2. Senior Business Analyst

- **Hace**: redacta release notes en lenguaje de negocio para los cambios relevantes de cara a usuarios/stakeholders.
- **Produce**: release notes legibles, complementando el changelog técnico autogenerado.
- **Entrega a**: Senior DevOps Engineer.

### 3. Senior DevOps Engineer

- **Hace**: ejecuta [`deploy-production.md`](./deploy-production.md) para la release completa, con la estrategia de despliegue apropiada al riesgo agregado de todos los cambios incluidos.
- **Produce**: release desplegada, changelog publicado, tag de versión creado en el repositorio.
- **Entrega a**: equipo completo (anuncio de release).
- **Terminada cuando**: la release está en producción, estable, y documentada.

### 4. Senior Code Reviewer (verificación de trazabilidad, no bloqueante)

- **Hace**: verifica que la release es completamente trazable (qué commits, qué pipeline, qué aprobaciones) antes de cerrar formalmente el ciclo.
- **Produce**: confirmación de trazabilidad completa o hallazgo de un gap en el proceso.

## Diagrama de flujo

```
Tech Lead (consolida alcance + versión semántica)
        ↓
Senior Business Analyst (release notes de negocio)
        ↓
Senior DevOps Engineer (despliegue vía deploy-production.md)
        ↓
Senior Code Reviewer (verificación de trazabilidad)
        ↓
   [Release cerrada y documentada]
```

## Validaciones obligatorias

- Cada cambio incluido en la release completó su playbook individual antes de agruparse.
- El versionado semántico se deriva automáticamente de Conventional Commits, no asignado manualmente sin criterio.
- Trazabilidad completa verificada antes de cerrar el ciclo.

## Criterios de finalización

La release se considera cerrada cuando está desplegada y estable en producción, el changelog y las release notes están publicados, y la trazabilidad completa (commits, pipeline, aprobaciones) es verificable sin reconstrucción manual.
