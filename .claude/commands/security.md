# /security

## Qué hace

Ejecuta el proceso de seguridad del framework — threat modeling para diseño nuevo, o respuesta a incidente para una vulnerabilidad ya detectada.

## Invoca

Según el contexto:

- **Diseño nuevo o cambio arquitectónico**: [`../recipes/security-threat-model.md`](../recipes/security-threat-model.md) → [`../recipes/security-review-architecture.md`](../recipes/security-review-architecture.md), con [`../agents/senior-security-engineer.md`](../agents/senior-security-engineer.md) como Arquitecto de seguridad.
- **Vulnerabilidad o incidente activo**: [`../playbooks/security-issue.md`](../playbooks/security-issue.md), con `senior-security-engineer.md` como Incident Commander, coordinando con [`../agents/senior-debugger.md`](../agents/senior-debugger.md) y el agente técnico correspondiente.
- **Cierre de cualquiera de los dos**: [`../checklists/security.md`](../checklists/security.md).

Metodología en ambos casos, sin excepción: **Threat Model → Riesgo → Mitigación → Implementación → Verificación** — nunca empieza por herramientas, nunca acepta "cumple OWASP" como criterio suficiente.

## Cuándo usarlo

Diseño de cualquier componente que maneje datos sensibles o exposición pública nueva; o inmediatamente ante una vulnerabilidad detectada o sospecha de acceso no autorizado.

## Equivalente manual

Ver [`../knowledge/security/README.md`](../knowledge/security/README.md) y elegir el documento/playbook específico al caso.
