# Best Practices (Security)

> Síntesis de las reglas más importantes del dominio de criterio de seguridad. Para controles técnicos específicos ya documentados (IAM, secretos, SQL Injection), ver [`../aws-security/`](../aws-security/), [`../devops/secrets-management.md`](../devops/secrets-management.md), [`../../anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md) — no se duplican aquí.

## Proceso: siempre Threat Model → Riesgo → Mitigación → Implementación → Verificación

- Nunca empezar por herramientas o controles — primero entender qué puede salir mal para ESTE sistema (ver [`threat-modeling.md`](./threat-modeling.md)).
- Priorizar por riesgo real (probabilidad × impacto), no tratar toda amenaza con la misma urgencia (ver [`risk-assessment.md`](./risk-assessment.md)).
- Todo riesgo no mitigado está explícitamente aceptado por un responsable, nunca simplemente ignorado.

## Diseño

- Seguridad como requisito de diseño desde el inicio, nunca capa añadida después (ver [`security-by-design.md`](./security-by-design.md)).
- Fail secure: el estado por defecto ante error/ambigüedad es siempre el más restrictivo.
- Defense in depth: cada capa se diseña asumiendo que las anteriores pueden fallar (ver [`defense-in-depth.md`](./defense-in-depth.md)).

## Identidad y acceso

- Autenticación y autorización son verificaciones independientes — autenticar no basta, verificar permiso sobre el recurso específico (ver [`authentication-vs-authorization.md`](./authentication-vs-authorization.md)).
- Zero Trust: ninguna llamada, ni siquiera interna, se confía implícitamente por su origen (ver [`zero-trust.md`](./zero-trust.md)).

## Cadena de suministro y vulnerabilidades

- SBOM y versiones fijadas con lockfile, nunca rangos abiertos (ver [`supply-chain-security.md`](./supply-chain-security.md)).
- Triage por explotabilidad y exposición real, no solo puntuación CVSS bruta (ver [`vulnerability-management.md`](./vulnerability-management.md)).

## APIs

- Verificar autorización de recurso específico en cada endpoint (BOLA), DTOs explícitos de entrada/salida (ver [`api-security.md`](./api-security.md)).

## Nunca aceptar "cumplir OWASP" como objetivo suficiente

Una checklist genérica es un punto de partida, no una garantía — todo control se justifica contra el threat model propio del sistema, nunca contra una lista universal sin contexto.

Ver [`pitfalls.md`](./pitfalls.md) y [`examples.md`](./examples.md).
