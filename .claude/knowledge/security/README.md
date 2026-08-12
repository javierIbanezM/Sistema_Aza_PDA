# Knowledge — Security

Índice del dominio de **criterio y toma de decisiones de seguridad** — threat modeling, evaluación de riesgo, arquitectura segura. Controles técnicos específicos ya documentados en otros dominios (IAM, secretos, SQL Injection, escaneo de dependencias) **no se duplican aquí**, se enlazan.

| Documento | Descripción |
|---|---|
| [`threat-modeling.md`](./threat-modeling.md) | STRIDE, diagrama de flujo de datos, límites de confianza |
| [`risk-assessment.md`](./risk-assessment.md) | Probabilidad × impacto, matriz de riesgo, riesgo aceptado explícito |
| [`attack-surface-analysis.md`](./attack-surface-analysis.md) | Inventario, minimizar antes de defender |
| [`defense-in-depth.md`](./defense-in-depth.md) | Ninguna capa suficiente por sí sola, coste proporcional al activo |
| [`security-by-design.md`](./security-by-design.md) | Fail secure, menor privilegio desde el diseño |
| [`secure-sdlc.md`](./secure-sdlc.md) | Shift left, gates de seguridad en cada fase del ciclo de vida |
| [`zero-trust.md`](./zero-trust.md) | Microsegmentación, identidad fuerte incluso servicio-a-servicio |
| [`supply-chain-security.md`](./supply-chain-security.md) | SBOM, procedencia verificable, pinning de dependencias |
| [`vulnerability-management.md`](./vulnerability-management.md) | Triage por riesgo real, SLA de remediación, mitigación compensatoria |
| [`authentication-vs-authorization.md`](./authentication-vs-authorization.md) | Distinción AuthN/AuthZ, BOLA, RBAC vs. ABAC |
| [`api-security.md`](./api-security.md) | BOLA, Mass Assignment, rate limiting específico, exposición de datos |
| [`secure-architecture-review.md`](./secure-architecture-review.md) | Checklist de revisión ordenado, nunca "cumple OWASP" como criterio suficiente |
| [`best-practices.md`](./best-practices.md) | Síntesis de las reglas más importantes del dominio |
| [`pitfalls.md`](./pitfalls.md) | Errores frecuentes específicos de seguridad |
| [`examples.md`](./examples.md) | Casos de uso completos |

## Dominios relacionados (no duplicados aquí)

| Tema | Dónde vive |
|---|---|
| IAM, KMS, Cognito, Secrets Manager | [`../aws-security/`](../aws-security/) |
| Gestión de secretos en pipeline/despliegue | [`../devops/secrets-management.md`](../devops/secrets-management.md) |
| Escaneo de vulnerabilidades en el pipeline | [`../devops/artifact-management.md`](../devops/artifact-management.md) |
| SQL Injection | [`../../anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md) |
| JWT vs. sesiones | [`../../decisions/jwt-vs-session.md`](../../decisions/jwt-vs-session.md) |
| Proceso de respuesta a incidentes | [`../../playbooks/security-issue.md`](../../playbooks/security-issue.md), [`incident-response.md`](../../playbooks/incident-response.md) |
| Validación de entrada | [`../../standards/validation.md`](../../standards/validation.md) |
| Marco general OWASP | `CLAUDE.md` sección 12 |

Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí ni en los dominios relacionados (regla de oro nº2).
