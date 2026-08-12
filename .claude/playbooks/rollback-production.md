# Playbook: Rollback Production

## Cuándo usar este playbook

Un despliegue reciente causa degradación o fallo en producción y debe revertirse — versión detallada y orientada a agentes del proceso técnico de [`../recipes/rollback-production.md`](../recipes/rollback-production.md).

## Fases

### 1. Senior Debugger

- **Hace**: confirma correlación temporal entre el problema reportado y un despliegue reciente (ver [`../knowledge/devops/rollback.md`](../knowledge/devops/rollback.md)) — nunca rollback sin esta confirmación.
- **Produce**: confirmación de causa (despliegue reciente) o descarte (el problema es anterior/no relacionado).
- **Entrega a**: Senior DevOps Engineer si se confirma.
- **Terminada cuando**: existe evidencia clara (no solo coincidencia temporal casual) de que el despliegue es la causa.

### 2. Senior DevOps Engineer

- **Hace**: ejecuta el rollback siguiendo [`../recipes/rollback-production.md`](../recipes/rollback-production.md), verificando si hay implicaciones de datos (migraciones) que requieran el patrón expansivo/contractivo.
- **Produce**: servicio revertido a la versión anterior estable.
- **Entrega a**: Senior Business Analyst (comunicación de impacto) y Tech Lead (planificación del fix).
- **Terminada cuando**: las métricas confirman que el síntoma original ha desaparecido tras el rollback.

### 3. Senior Business Analyst (en paralelo)

- **Hace**: evalúa y comunica el impacto de negocio del incidente y del periodo de degradación.
- **Produce**: resumen de impacto para stakeholders.

### 4. Tech Lead

- **Hace**: planifica la corrección del problema que causó el rollback, siguiendo [`bug-fix.md`](./bug-fix.md) o [`hotfix.md`](./hotfix.md) según urgencia.
- **Produce**: plan de corrección antes de reintentar el despliegue.

## Diagrama de flujo

```
Senior Debugger (confirma correlación despliegue-problema)
        ↓
Senior DevOps Engineer (ejecuta rollback)
        │
        ├──→ Senior Business Analyst (comunica impacto)
        └──→ Tech Lead (planifica corrección) → bug-fix.md / hotfix.md
```

## Validaciones obligatorias

- Correlación temporal confirmada antes de ejecutar (no "por si acaso").
- Verificación post-rollback con las mismas métricas que detectaron el problema original.
- Comunicación explícita al equipo, nunca rollback silencioso.

## Criterios de finalización

El rollback se considera completo cuando las métricas confirman la resolución del síntoma, el equipo ha sido informado, y existe un plan (con propietario asignado) para corregir la causa raíz antes de reintentar el despliegue.
