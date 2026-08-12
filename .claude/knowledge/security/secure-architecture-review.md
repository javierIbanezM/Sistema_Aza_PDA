# Secure Architecture Review

> Ver [`../../agents/software-architect.md`](../../agents/software-architect.md) para el proceso general de diseño de arquitectura — este documento cubre el checklist específico de seguridad que complementa esa revisión, aplicado antes de aprobar un diseño para implementación.

## Cuándo realizar una revisión de arquitectura segura

Ante cualquier cambio arquitectónico significativo (nueva integración externa, nuevo flujo de datos sensibles, nuevo componente expuesto públicamente) — mismo criterio de proporcionalidad que el resto del framework aplica a decisiones de alto impacto (ver [`../../agents/software-architect.md`](../../agents/software-architect.md), clasificación de decisiones por reversibilidad).

## Checklist de revisión: en orden, no aleatorio

```
1. ¿Existe un threat model para este diseño? (ver threat-modeling.md) — si no, detener la revisión aquí
2. ¿Los límites de confianza están claramente identificados en el diseño?
3. ¿Cada dato sensible tiene su clasificación explícita (público/interno/sensible/regulado)?
4. ¿La autenticación y autorización están diseñadas en cada punto de acceso, no solo en el perímetro?
5. ¿El diseño sigue defense in depth, o depende de una única capa de protección?
6. ¿Los secretos/credenciales del diseño siguen el proceso de gestión de secretos ya establecido?
7. ¿El diseño introduce nueva superficie de ataque (nuevo endpoint, nueva dependencia, nuevo servicio externo)?
8. ¿Existe un plan de observabilidad/detección específico para este componente?
```

El orden importa: sin threat model (paso 1), el resto de la revisión carece de contexto sobre qué amenazas son relevantes para este diseño concreto — nunca saltar directamente a "revisar controles" sin haber establecido primero qué se está protegiendo de qué.

## Nunca aprobar un diseño solo porque "cumple OWASP"

Cumplir una checklist genérica (OWASP Top 10, CIS Benchmarks) es un punto de partida, no un objetivo suficiente — un diseño puede cumplir cada ítem de una checklist genérica y aun así ser vulnerable a una amenaza específica de este sistema concreto que la checklist genérica no contempla porque es propia del dominio de negocio (ver [`threat-modeling.md`](./threat-modeling.md)). La revisión de arquitectura segura evalúa el diseño **contra su propio threat model**, no contra una lista universal.

## Documentar la revisión como el resto de decisiones arquitectónicas

```
Formato: mismo que templates/ADR.md, con sección adicional "Amenazas consideradas y mitigaciones"
```

Ver [`../../templates/ADR.md`](../../templates/ADR.md) — una revisión de seguridad sin documentar es una revisión que no puede auditarse después, ni sirve de referencia si el mismo patrón de diseño se repite en otro contexto.

## Salida de la revisión: aprobado, aprobado con condiciones, o rechazado

Una revisión de arquitectura segura no es binaria simple — puede aprobar el diseño con condiciones específicas (mitigaciones concretas que deben implementarse antes de producción, no "recomendaciones" vagas sin seguimiento) — ver [`../../playbooks/new-feature.md`](../../playbooks/new-feature.md) para cómo esto se integra en el flujo de trabajo del equipo.

## Checklist

- [ ] ¿La revisión partió de un threat model, no de una checklist genérica aislada?
- [ ] ¿Se evaluaron los ocho puntos en orden, sin saltar directamente a controles sin contexto de amenazas?
- [ ] ¿La revisión está documentada de forma auditable, con las condiciones de aprobación explícitas si las hay?

Ver también: [`threat-modeling.md`](./threat-modeling.md), [`../../agents/software-architect.md`](../../agents/software-architect.md), [`../../templates/ADR.md`](../../templates/ADR.md).
