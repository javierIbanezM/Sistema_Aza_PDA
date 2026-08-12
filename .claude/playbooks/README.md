# Playbooks — Índice

Procesos multi-paso completos, normalmente cruzando varios dominios y varios agentes (a diferencia de `recipes/`, que son tareas técnicas concretas de un único dominio, y de `agents/`, que definen metodología por rol pero no el orden entre roles). Un playbook responde a "¿cómo llevo esta feature/incidente/proceso de principio a fin, y quién hace qué en qué orden?".

Cada playbook especifica, por fase: **qué agente participa, qué produce, a quién se lo entrega, y cuándo se considera terminada esa fase.**

## Playbooks construidos

| Playbook | Cuándo usarlo |
|---|---|
| [`new-feature.md`](./new-feature.md) | Funcionalidad nueva no trivial, de principio a fin |
| [`bug-fix.md`](./bug-fix.md) | Corrección de un defecto no urgente |
| [`hotfix.md`](./hotfix.md) | Incidente en producción con impacto activo, corrección inmediata |
| [`refactor.md`](./refactor.md) | Cambio estructural sin alterar comportamiento observable |
| [`performance.md`](./performance.md) | Diagnóstico y resolución de un problema de rendimiento |
| [`security-issue.md`](./security-issue.md) | Vulnerabilidad detectada o incidente de seguridad activo |
| [`new-api.md`](./new-api.md) | Diseño y publicación de un contrato de API nuevo |
| [`database-change.md`](./database-change.md) | Cambio de esquema de base de datos, con o sin downtime |
| [`deploy-production.md`](./deploy-production.md) | Despliegue estándar de una versión nueva a producción |
| [`rollback-production.md`](./rollback-production.md) | Reversión de un despliegue reciente que causa degradación |
| [`release-management.md`](./release-management.md) | Coordinar una release que agrupa múltiples cambios |
| [`incident-response.md`](./incident-response.md) | Respuesta coordinada a un incidente de producción |
| [`disaster-recovery.md`](./disaster-recovery.md) | Recuperación ante un fallo catastrófico (región caída, pérdida de datos extendida) |

## Flujo típico de agentes (ver [`../agents/README.md`](../agents/README.md))

```
Senior Business Analyst → Software Architect → Tech Lead → Agente técnico (según tecnología, ver `docs/orchestrator.md` Paso 2b) → Senior Code Reviewer
                                                                                    ↓ (si hay incidencias)
                                                                            Senior Debugger
```

Cada playbook adapta este flujo base a su caso concreto (comprimido en `hotfix.md`, con foco de seguridad en `security-issue.md`, con validación de consumidores externos en `new-api.md`...).

## Próximos playbooks

```
pull-request.md
```

Cada playbook nuevo debe listar explícitamente, en orden, qué agente(s), Skill(s), `knowledge/`, `patterns/`/`anti-patterns/`, `standards/`, `prompts/`, `recipes/` y `checklists/` utiliza en cada fase — nunca duplicar su contenido, solo secuenciarlo.
