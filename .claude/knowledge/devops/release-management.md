# Release Management

> Ver [`../../standards/commits.md`](../../standards/commits.md) para Conventional Commits, base del versionado semántico automático que este documento aplica a nivel de release.

## Versionado semántico como contrato de expectativas

```
MAJOR.MINOR.PATCH — 2.4.1
MAJOR: cambio que rompe compatibilidad
MINOR: funcionalidad nueva, compatible hacia atrás
PATCH: corrección de bug, compatible hacia atrás
```

El número de versión comunica a quien consume el artefacto (otro equipo, un cliente de una librería) qué esperar del cambio sin necesidad de leer el changelog completo — derivado automáticamente de Conventional Commits (`feat:` → MINOR, `fix:` → PATCH, `BREAKING CHANGE:` → MAJOR, ver [`../../standards/commits.md`](../../standards/commits.md)).

## Changelog generado, no escrito a mano

```
## v2.4.1
### Fixed
- Corrección de expiración prematura del refresh token (fix(auth): ...)
### Added
- Endpoint de cancelación de pedidos (feat(pedidos): ...)
```

Generado automáticamente a partir de los mensajes de commit estructurados — un changelog mantenido manualmente diverge del historial real con el tiempo; uno generado es siempre consistente con lo que realmente cambió.

## Release Notes: para humanos, no solo para máquinas

El changelog automático es preciso pero técnico — para releases de cara a usuarios finales o stakeholders de negocio, complementar con una nota de release en lenguaje de negocio ("ahora puedes cancelar un pedido hasta 24h después de confirmarlo"), redactada por quien entiende el impacto real, no generada mecánicamente del historial de commits.

## Cadencia de release: continua vs. programada

| Modelo | Cuándo |
|---|---|
| Continuous Deployment | Cada merge a main que pasa el pipeline se despliega automáticamente — máxima velocidad, requiere alta confianza en el pipeline y feature flags para funcionalidad incompleta |
| Release programada | Ventanas fijas (semanal/quincenal) — más control, apropiado cuando hay dependencias externas (comunicación a clientes, coordinación con otros equipos) que requieren previsibilidad |

Ninguna es universalmente superior — la elección depende de la madurez del pipeline (cobertura de tests, velocidad de rollback) y de restricciones de negocio reales, no de preferencia por "ser ágil".

## Trazabilidad: qué versión, con qué código, desplegada cuándo y por quién

```
Release v2.4.1: commit abc123f, pipeline #4521, desplegado 2026-07-31 10:15 por CI, aprobado por @tech-lead
```

Cada release debe ser rastreable de extremo a extremo — ante un incidente, la primera pregunta ("¿qué cambió recientemente?") debe responderse en segundos, no reconstruyendo manualmente el historial.

## Checklist

- [ ] ¿El versionado semántico se deriva automáticamente de Conventional Commits, no asignado manualmente?
- [ ] ¿El changelog se genera automáticamente del historial de commits estructurado?
- [ ] ¿Cada release es trazable (commit, pipeline, quién aprobó, cuándo se desplegó)?
- [ ] ¿La cadencia de release (continua vs. programada) responde a restricciones reales, no a preferencia sin justificar?

Ver también: [`../../standards/commits.md`](../../standards/commits.md), [`ci-cd.md`](./ci-cd.md), [`artifact-management.md`](./artifact-management.md).
