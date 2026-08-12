# Prompt: Revisión Arquitectónica

## Propósito

Guiar una revisión de código o de un Pull Request centrada en aspectos arquitectónicos y de diseño, complementaria (no sustitutiva) a la revisión funcional habitual.

## Instrucciones para Claude Code

Al recibir este prompt junto con un diff, PR o módulo de código, realiza la revisión en este orden:

1. **Lee el contexto primero**: identifica qué capa(s) toca el cambio (Domain, Application, Infrastructure, Interface — ver [`../clean-architecture.md`](../knowledge/architecture/clean-architecture.md)) y qué Bounded Context afecta (ver [`../ddd.md`](../knowledge/architecture/ddd.md)).

2. **Verifica la regla de dependencia**: ¿algún import introducido en `domain/` o `application/` referencia directamente un framework, ORM o librería de infraestructura? Si es así, señálalo como violación de Clean Architecture (ver [`../clean-architecture.md`](../knowledge/architecture/clean-architecture.md)).

3. **Revisa SOLID** (ver [`../solid.md`](../knowledge/architecture/solid.md)):
   - ¿Alguna clase modificada/creada mezcla más de un motivo de cambio (SRP)?
   - ¿Se ha añadido un `if-else`/`switch` que probablemente crecerá con el tiempo, en lugar de un punto de extensión (OCP)?
   - ¿Alguna nueva dependencia se inyecta como interfaz/abstracción, o como implementación concreta (DIP)?

4. **Verifica anti-patrones conocidos** — recorre la lista de [`../anti-patterns/`](../anti-patterns/) y señala explícitamente cualquier coincidencia: [God Object](../anti-patterns/god-object.md), [Spaghetti Code](../anti-patterns/spaghetti.md), [Anemic Domain Model](../anti-patterns/anemic-domain-model.md), [N+1 Query](../anti-patterns/n-plus-one.md), [SQL Injection](../anti-patterns/sql-injection.md), [Memory Leak](../anti-patterns/memory-leak.md), [Callback Hell](../anti-patterns/callback-hell.md).

5. **Verifica cobertura de tests**: ¿el cambio incluye tests para el comportamiento nuevo (camino feliz, validación fallida, caso límite)? Ver [`../testing.md`](../knowledge/architecture/testing.md).

6. **Verifica DRY/KISS/YAGNI** (ver [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)): ¿hay duplicación de conocimiento de negocio? ¿hay abstracción especulativa sin un caso de uso real presente?

7. **Verifica documentación**: si el cambio introduce una decisión arquitectónica relevante, ¿se acompaña de un ADR? (ver [`../documentacion-tecnica.md`](../knowledge/architecture/technical-documentation.md)).

## Formato de salida esperado

```
## Resumen del cambio
[Una frase describiendo qué hace el cambio y qué capas/contextos toca]

## Hallazgos arquitectónicos
- [ ] [Severidad: Alta/Media/Baja] Descripción del hallazgo + enlace al documento/patrón/antipatrón relevante + sugerencia concreta de corrección

## Aspectos positivos
- [Qué se ha hecho bien, para reforzar buenas prácticas ya aplicadas]

## Checklist de arquitectura
[Copiar y marcar el checklist de `.../checklists/architecture.md`]
```

No repitas explicaciones largas de los principios en la revisión — enlaza al documento correspondiente y explica brevemente por qué aplica a este caso concreto.
