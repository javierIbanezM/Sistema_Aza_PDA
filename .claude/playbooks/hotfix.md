# Playbook: Hotfix

## Cuándo usar este playbook

Un incidente en producción con impacto activo en usuarios reales, que requiere corrección **inmediata** — nunca se salta el pipeline de calidad "porque es urgente" (ver `CLAUDE.md` sección 9.3), pero el flujo se comprime respecto a [`bug-fix.md`](./bug-fix.md).

## Fases (comprimidas respecto al flujo estándar)

### 1. Senior Debugger (entra primero, sin esperar el análisis de negocio completo)

- **Hace**: diagnostica el incidente con la misma disciplina de siempre (reproducción, hipótesis, causa raíz) pero priorizando velocidad — usa [`../standards/logging-standard.md`](../standards/logging-standard.md) y las herramientas de diagnóstico en producción del stack correspondiente (ver `knowledge/<dominio>/debugging.md`).
- **Produce**: causa raíz identificada + fix mínimo viable con test de regresión.
- **Entrega a**: Senior Code Reviewer (revisión acelerada, pero sin omitirla).
- **Terminada cuando**: existe un fix con test de regresión, aunque el alcance sea deliberadamente mínimo (sin refactors adicionales "ya que estamos").

### 2. Senior Code Reviewer (revisión enfocada)

- **Hace**: revisión centrada en: ¿el fix ataca la causa raíz real? ¿introduce algún riesgo nuevo (especialmente de seguridad, ver [`../anti-patterns/`](../anti-patterns/))? ¿el alcance está deliberadamente acotado al incidente?
- **Produce**: aprobación o bloqueo — un hotfix con hallazgos bloqueantes no se despliega solo por la urgencia.
- **Entrega a**: despliegue inmediato tras aprobación (ver `CLAUDE.md` sección 9.3, mismo pipeline de CI/CD que cualquier cambio, nunca saltado).
- **Terminada cuando**: no quedan hallazgos bloqueantes.

### 3. Senior Business Analyst (en paralelo, no bloqueante)

- **Hace**: documenta el impacto de negocio del incidente mientras el Debugger trabaja, para tener el contexto listo para el post-mortem.
- **Produce**: resumen de impacto (usuarios afectados, duración, severidad).
- **Entrega a**: post-mortem (paso 4).

### 4. Post-mortem (tras resolver el incidente, no durante)

- **Hace**: Tech Lead + Software Architect revisan por qué el sistema permitió este fallo y por qué las salvaguardas existentes (tests, checklists, monitorización) no lo detectaron antes de producción.
- **Produce**: acción de seguimiento — ¿falta un test de regresión más amplio? ¿una alerta de monitorización? ¿una revisión de una decisión arquitectónica? (Ver [`../knowledge/architecture/technical-documentation.md`](../knowledge/architecture/technical-documentation.md) para el nivel de rigor esperado en el registro).
- **Entrega a**: [`new-feature.md`](./new-feature.md) o [`refactor.md`](./refactor.md) si la acción de seguimiento requiere trabajo planificado.

## Diagrama de flujo

```
Senior Debugger ──────────────────→ Senior Code Reviewer → [Producción, urgente]
        │                                                          │
        └──── (en paralelo) Senior Business Analyst ──────────────→ Post-mortem
                                                                     ↓
                                                        Tech Lead + Software Architect
                                                                     ↓
                                                    new-feature.md / refactor.md (si aplica)
```

## Regla de oro de este playbook

Urgente no significa sin revisión ni sin test de regresión — significa que el análisis de negocio detallado y las mejoras estructurales de fondo se posponen al post-mortem, **nunca** que se salta la verificación de que el fix es correcto y seguro.
