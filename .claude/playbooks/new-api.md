# Playbook: New API

## Cuándo usar este playbook

Diseñar y exponer un contrato de API nuevo (o una extensión significativa de uno existente) consumido por clientes externos al servicio — un caso especializado de [`new-feature.md`](./new-feature.md) con énfasis adicional en el diseño del contrato como producto en sí mismo.

## Fases

### 1. Senior Business Analyst

- **Hace**: identifica quién consume la API (¿un único cliente interno, múltiples equipos, terceros externos?) y con qué patrón de uso — esta respuesta condiciona directamente la fase siguiente.
- **Produce**: consumidores identificados, patrón de uso esperado (volumen, frecuencia, necesidad de composición flexible de datos).
- **Entrega a**: Software Architect.

### 2. Software Architect

- **Hace**: decide REST vs. GraphQL (ver [`../decisions/rest-vs-graphql.md`](../decisions/rest-vs-graphql.md) y [`ADR-0003`](../knowledge/architecture/adr/ADR-0003-rest-over-graphql.md) — REST por defecto salvo justificación explícita), diseña el contrato (recursos, operaciones, versionado) siguiendo [`../standards/api.md`](../standards/api.md).
- **Produce**: diseño del contrato (endpoints, DTOs de entrada/salida según [`../standards/dto.md`](../standards/dto.md), códigos de error).
- **Entrega a**: Tech Lead.

### 3. Tech Lead

- **Hace**: decide si el contrato se documenta spec-first (OpenAPI escrito antes de implementar, ver [`../knowledge/architecture/openapi.md`](../knowledge/architecture/openapi.md)) — recomendado cuando hay múltiples consumidores o equipos que necesitan trabajar en paralelo contra el contrato — o code-first (generado desde las anotaciones del código) para APIs internas de un único consumidor.
- **Produce**: plan de implementación + decisión spec-first/code-first.
- **Entrega a**: el agente técnico correspondiente a la tecnología detectada (ver `docs/orchestrator.md` Paso 2b).

### 4. Agente técnico (según tecnología detectada)

- **Hace**: implementa siguiendo la recipe de creación de endpoint correspondiente al stack ([`../agents/senior-java-engineer.md`](../agents/senior-java-engineer.md) usa `java-create-rest-controller.md`; [`../agents/senior-nodejs-engineer.md`](../agents/senior-nodejs-engineer.md) usa `node-create-rest-endpoint.md`), aplicando `standards/validation.md` y `standards/exceptions.md` para el manejo de errores.
- **Produce**: endpoint implementado + documentación OpenAPI generada o verificada contra el código + tests.
- **Entrega a**: Senior Code Reviewer.

### 5. Senior Code Reviewer

- **Hace**: revisa específicamente que el contrato cumple [`../standards/api.md`](../standards/api.md) (verbos correctos, códigos de estado, paginación, formato de error consistente) además de la revisión estándar en capas.
- **Produce**: aprobación.

### 6. Senior Business Analyst (validación de cierre)

- **Hace**: confirma con los consumidores identificados en la fase 1 que el contrato final satisface el patrón de uso esperado — antes de considerar la API estable/versión 1.0 pública.
- **Produce**: confirmación de que el contrato es adecuado, o feedback que reabre la fase 2 si no lo es.

## Diagrama de flujo

```
Senior Business Analyst (consumidores + patrón de uso)
        ↓
Software Architect (REST/GraphQL + diseño del contrato)
        ↓
Tech Lead (spec-first/code-first + plan)
        ↓
Agente técnico (implementación + OpenAPI + tests)
        ↓
Senior Code Reviewer (cumplimiento de standards/api.md)
        ↓
Senior Business Analyst (validación con consumidores reales)
        ↓
   [API publicada]
```

## Regla de oro de este playbook

El contrato de una API con múltiples consumidores es un compromiso difícil de revertir — se trata como una decisión de alto impacto (ver [`../agents/software-architect.md`](../agents/software-architect.md), clasificación de decisiones), nunca como un detalle de implementación menor.
