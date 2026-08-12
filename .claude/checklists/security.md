# Checklist — Seguridad

> Verificar antes de dar por terminada cualquier tarea con implicaciones de seguridad (nuevo endpoint, cambio de autenticación/autorización, integración externa, gestión de secretos). Cada ítem enlaza al documento donde se explica el criterio en profundidad — esta checklist no repite esa teoría, solo la convierte en verificación accionable.

## Proceso (nunca empezar por herramientas)

- [ ] Existe un threat model para este cambio, o se ha confirmado explícitamente que no es necesario para su alcance — [`threat-modeling.md`](../knowledge/security/threat-modeling.md)
- [ ] Las amenazas identificadas están priorizadas por riesgo real (probabilidad × impacto), no tratadas todas con la misma urgencia — [`risk-assessment.md`](../knowledge/security/risk-assessment.md)
- [ ] Ningún control se ha propuesto sin la amenaza específica que justifica su necesidad

## Diseño

- [ ] El sistema falla de forma segura (deniega) ante errores/excepciones inesperadas, nunca permite por defecto — [`security-by-design.md`](../knowledge/security/security-by-design.md)
- [ ] Cada capa de seguridad se diseña asumiendo que las anteriores pueden fallar, sin depender únicamente de una — [`defense-in-depth.md`](../knowledge/security/defense-in-depth.md)
- [ ] El tráfico interno servicio-a-servicio se autentica explícitamente, sin confiar implícitamente en el origen "interno" — [`zero-trust.md`](../knowledge/security/zero-trust.md)

## Identidad y acceso

- [ ] Cada endpoint que expone un recurso identificado por ID verifica autorización sobre ese recurso específico, no solo autenticación general (BOLA) — [`authentication-vs-authorization.md`](../knowledge/security/authentication-vs-authorization.md)
- [ ] Los endpoints de escritura usan DTOs de entrada explícitos, nunca mapeo directo del body a la entidad completa (Mass Assignment) — [`api-security.md`](../knowledge/security/api-security.md)
- [ ] Los endpoints sensibles (login, recuperación de contraseña) tienen rate limiting específico más estricto que el global — [`api-security.md`](../knowledge/security/api-security.md)

## Datos y secretos

- [ ] Ningún secreto está hardcodeado en código, configuración versionada, o embebido en una imagen de contenedor — [`../knowledge/devops/secrets-management.md`](../knowledge/devops/secrets-management.md)
- [ ] Los datos sensibles están cifrados en tránsito y en reposo — [`../knowledge/aws-security/kms.md`](../knowledge/aws-security/kms.md)
- [ ] Ningún log incluye datos sensibles en claro — [`../standards/logging-standard.md`](../standards/logging-standard.md)
- [ ] Toda entrada SQL está parametrizada, sin concatenación de input de usuario — [`../anti-patterns/sql-injection.md`](../anti-patterns/sql-injection.md)

## Cadena de suministro y vulnerabilidades

- [ ] Las dependencias usan versiones exactas con lockfile, no rangos abiertos — [`supply-chain-security.md`](../knowledge/security/supply-chain-security.md)
- [ ] El escaneo de vulnerabilidades es bloqueante en el pipeline antes de publicar el artefacto — [`../knowledge/devops/artifact-management.md`](../knowledge/devops/artifact-management.md)
- [ ] Ante un hallazgo, el triage considera explotabilidad y exposición real, no solo la puntuación CVSS bruta — [`vulnerability-management.md`](../knowledge/security/vulnerability-management.md)

## Cierre

- [ ] La revisión no se ha cerrado aceptando "cumple OWASP Top 10" como criterio suficiente — la verificación es contra el threat model específico de este sistema — [`secure-architecture-review.md`](../knowledge/security/secure-architecture-review.md)
- [ ] Todo riesgo identificado y no mitigado está explícitamente aceptado por un responsable nombrado, nunca simplemente omitido — [`risk-assessment.md`](../knowledge/security/risk-assessment.md)

Ver también: [`../knowledge/security/README.md`](../knowledge/security/README.md), [`../playbooks/security-issue.md`](../playbooks/security-issue.md), [`../agents/senior-security-engineer.md`](../agents/senior-security-engineer.md).
