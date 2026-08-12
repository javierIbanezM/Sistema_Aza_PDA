# Playbook: Incident Response

## Cuándo usar este playbook

Cualquier incidente de producción con impacto real o potencial en usuarios — complementa [`hotfix.md`](./hotfix.md) (que cubre específicamente la corrección de código) con el proceso completo de gestión del incidente en sí, incluida coordinación y comunicación.

## Fases

### 1. Quien detecta (alerta automática o reporte humano)

- **Hace**: declara el incidente formalmente (canal dedicado, herramienta de gestión de incidentes) con severidad inicial estimada.
- **Produce**: incidente declarado, con timestamp de inicio registrado.
- **Entrega a**: on-call / Senior DevOps Engineer.
- **Terminada cuando**: el incidente tiene un responsable asignado (Incident Commander) claramente identificado.

### 2. Senior DevOps Engineer (Incident Commander) + Senior Debugger

- **Hace**: coordina la respuesta técnica — diagnóstico (ver [`../knowledge/devops/observability.md`](../knowledge/devops/observability.md)), decide si aplica rollback ([`rollback-production.md`](./rollback-production.md)) o mitigación alternativa.
- **Produce**: mitigación aplicada (rollback, escalado de capacidad, circuit breaker manual) y comunicación de estado cada 15-30 minutos mientras el incidente esté activo.
- **Entrega a**: Senior Business Analyst para comunicación externa si el impacto lo requiere.
- **Terminada cuando**: el impacto en usuarios ha cesado, confirmado con métricas.

### 3. Senior Business Analyst (en paralelo)

- **Hace**: evalúa impacto de negocio y coordina comunicación a clientes/stakeholders si es necesaria.
- **Produce**: comunicación externa (si aplica) y registro de impacto de negocio para el post-mortem.

### 4. Post-mortem (obligatorio, sin excepción, dentro de 1-2 días laborables)

- **Hace**: Tech Lead + Software Architect + quien fue Incident Commander reconstruyen la línea temporal completa, identifican causa raíz y por qué las salvaguardas existentes (tests, alertas, checklists) no lo previnieron.
- **Produce**: documento de post-mortem sin culpabilización individual (blameless), con acciones de seguimiento concretas y propietarios asignados.
- **Entrega a**: [`new-feature.md`](./new-feature.md)/[`refactor.md`](./refactor.md) para cada acción de seguimiento.

## Diagrama de flujo

```
Detección → Declaración del incidente
        ↓
Incident Commander (DevOps) + Senior Debugger (diagnóstico + mitigación)
        │
        ├──→ Senior Business Analyst (comunicación externa si aplica)
        ↓
   [Impacto cesado, confirmado con métricas]
        ↓
Post-mortem (obligatorio, blameless, con acciones de seguimiento)
```

## Validaciones obligatorias

- Un Incident Commander claramente asignado desde el inicio, sin ambigüedad de quién coordina.
- Comunicación de estado periódica mientras el incidente está activo, no silencio hasta la resolución.
- Post-mortem realizado sin excepción, incluso si el incidente se resolvió rápido.

## Criterios de finalización

El incidente se cierra formalmente cuando el impacto ha cesado (confirmado con métricas, no solo percepción), y se considera completo cuando el post-mortem está documentado con acciones de seguimiento asignadas — un incidente sin post-mortem no está verdaderamente cerrado desde la perspectiva de este framework.
