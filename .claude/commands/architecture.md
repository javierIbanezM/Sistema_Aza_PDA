# /architecture

## Qué hace

Diseña o revisa una decisión de arquitectura, con trade-offs explícitos y, si el impacto es alto, documentada como ADR.

## Invoca

- Agente: [`../agents/software-architect.md`](../agents/software-architect.md).
- Si la decisión ya tiene una comparativa resuelta en el framework, se aplica directamente en lugar de reanalizar desde cero: ver [`../decisions/`](../decisions/).
- Si el cambio tiene implicaciones de seguridad: complementa con [`../knowledge/security/secure-architecture-review.md`](../knowledge/security/secure-architecture-review.md) (checklist de 8 puntos, nunca "cumple OWASP" como criterio suficiente).
- Cierre con [`../checklists/architecture.md`](../checklists/architecture.md).
- Decisiones de alto impacto se documentan con [`../templates/ADR.md`](../templates/ADR.md) — ver [`../knowledge/architecture/adr/`](../knowledge/architecture/adr/) como ejemplo del nivel de rigor esperado.

## Cuándo usarlo

Antes de comprometerse a una decisión difícil de revertir (elección de base de datos, topología de red, patrón de comunicación entre servicios) — el agente clasifica primero si la decisión es reversible o no antes de invertir el esfuerzo de análisis proporcional.

## Equivalente manual

Consultar [`../agents/software-architect.md`](../agents/software-architect.md) directamente.
