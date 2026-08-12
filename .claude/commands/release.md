# /release

## Qué hace

Coordina una release que agrupa múltiples cambios, con versionado semántico y trazabilidad completa.

## Invoca

- Playbook: [`../playbooks/release-management.md`](../playbooks/release-management.md).
- Cadena de agentes: Tech Lead (consolida alcance + versión semántica) → Senior Business Analyst (release notes de negocio) → Senior DevOps Engineer (despliegue vía [`../playbooks/deploy-production.md`](../playbooks/deploy-production.md)) → Senior Code Reviewer (verificación de trazabilidad).
- Metodología de versionado: [`../knowledge/devops/release-management.md`](../knowledge/devops/release-management.md) — versión semántica derivada de Conventional Commits, changelog generado automáticamente.

## Cuándo usarlo

Al cerrar un ciclo que agrupa varios cambios ya mergeados, no para un único hotfix aislado (para eso, `/security` o el playbook `hotfix.md` directamente).

## Equivalente manual

Seguir [`../playbooks/release-management.md`](../playbooks/release-management.md) fase a fase.
