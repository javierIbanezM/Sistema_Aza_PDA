# Skill: Security

## Rol

Senior Security Engineer. Guía threat modeling, evaluación de riesgo, arquitectura segura y respuesta a incidentes — nunca una lista de controles OWASP sin contexto, siempre criterio: Threat Model → Riesgo → Mitigación → Implementación → Verificación.

## Cuándo usar / no usar

**Usar** ante: diseño de un sistema nuevo o cambio arquitectónico significativo, threat modeling, evaluación de riesgo de una amenaza identificada, revisión de arquitectura segura, gestión de vulnerabilidades, respuesta a un incidente de seguridad.

**No usar** ante: implementación de un control ya decidido y bien documentado en otro dominio (ir directamente a `knowledge/aws-security/`, `knowledge/devops/secrets-management.md`, o la Skill técnica correspondiente), tareas sin ambigüedad de riesgo real.

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial.
2. **Nunca empezar por herramientas** — consultar primero `knowledge/security/threat-modeling.md` si no existe ya un threat model para el sistema/cambio en cuestión.
3. Priorizar con `knowledge/security/risk-assessment.md` antes de decidir qué mitigar primero.
4. Consultar el documento específico (`zero-trust.md`, `api-security.md`, `secure-sdlc.md`...) y, para controles técnicos concretos, el dominio correspondiente (`knowledge/aws-security/`, `knowledge/devops/secrets-management.md`, `anti-patterns/sql-injection.md`).
5. Usar la `recipes/security-*.md` correspondiente si la tarea coincide con una receta ya definida.
6. Para incidentes activos, usar `playbooks/security-issue.md` o `playbooks/incident-response.md` — no reinventar el proceso.
7. Ejecutar `checklists/architecture.md` antes de cerrar la tarea. **Nunca aceptar "cumple OWASP" como criterio de cierre suficiente** — verificar contra el threat model específico.

## Documentación a consultar

[`knowledge/security/`](../../knowledge/security/) — 12 documentos de criterio (ver README). Controles técnicos específicos: [`knowledge/aws-security/`](../../knowledge/aws-security/), [`knowledge/devops/secrets-management.md`](../../knowledge/devops/secrets-management.md), [`knowledge/devops/artifact-management.md`](../../knowledge/devops/artifact-management.md).

## Decisions relevantes

[`decisions/`](../../decisions/): `sast-vs-dast.md`, `jwt-vs-session.md`, `secrets-manager-vs-parameter-store.md`.

## Anti-patrones a consultar

[`anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md).

## Standards a aplicar

[`standards/validation.md`](../../standards/validation.md), [`standards/logging-standard.md`](../../standards/logging-standard.md), [`standards/api.md`](../../standards/api.md), [`standards/dto.md`](../../standards/dto.md).

## Recipes a utilizar

[`recipes/`](../../recipes/): `security-threat-model.md`, `security-scan-dependencies.md`, `security-review-architecture.md`.

## Snippets a reutilizar

[`snippets/security/`](../../snippets/security/).

## Playbooks a ejecutar

[`playbooks/security-issue.md`](../../playbooks/security-issue.md), [`playbooks/incident-response.md`](../../playbooks/incident-response.md).

## Ejemplos

[`knowledge/security/examples.md`](../../knowledge/security/examples.md).
