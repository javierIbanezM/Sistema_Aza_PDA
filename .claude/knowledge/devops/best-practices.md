# Best Practices (DevOps)

> Síntesis de las reglas más importantes del dominio DevOps. Ver cada documento enlazado para el razonamiento completo.

## Pipeline y despliegue

- Ningún despliegue manual — el pipeline es la única vía a producción (ver [`ci-cd.md`](./ci-cd.md)).
- Builds reproducibles: versiones fijas, tag de imagen = SHA de commit, nunca `latest` en producción.
- Estrategia de despliegue elegida por trade-off real (coste vs. velocidad de rollback), no por defecto (ver [`deployment-strategies.md`](./deployment-strategies.md)).
- Migraciones expansivas antes que contractivas para permitir rollback seguro (ver [`rollback.md`](./rollback.md)).

## Infraestructura

- Infraestructura como código, sin recursos creados manualmente en consola (ver [`infrastructure-as-code.md`](./infrastructure-as-code.md)).
- Estado remoto con bloqueo, módulos reutilizables entre proyectos.
- Contenedores con multi-stage build, usuario no privilegiado, sin secretos embebidos (ver [`containerization.md`](./containerization.md), [`secrets-management.md`](./secrets-management.md)).
- Requests/limits explícitos, Readiness/Liveness diferenciados en todo despliegue orquestado (ver [`orchestration.md`](./orchestration.md)).

## Observabilidad

- Plataforma centralizada de logs/métricas/trazas correlacionables, no soluciones aisladas por equipo (ver [`observability.md`](./observability.md)).
- Alertas sobre síntomas visibles al usuario, con runbook vinculado, nunca ruido sin acción posible (ver [`alerting.md`](./alerting.md)).

## Configuración y entornos

- Mismo artefacto en todos los entornos, configuración inyectada externamente (ver [`configuration-management.md`](./configuration-management.md)).
- Staging con paridad real respecto a producción, datos de prueba siempre anonimizados (ver [`environments.md`](./environments.md)).

## Releases

- Versionado semántico derivado de Conventional Commits, changelog generado automáticamente (ver [`release-management.md`](./release-management.md)).
- Toda release trazable de extremo a extremo.

Ver [`pitfalls.md`](./pitfalls.md) y [`examples.md`](./examples.md).
