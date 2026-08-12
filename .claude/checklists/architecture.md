# Checklist — Arquitectura de Software

> Verificar antes de dar por terminada cualquier tarea que implique una decisión de diseño o arquitectura. Cada ítem enlaza al documento donde se explica el criterio en profundidad.

## Diseño de código (SOLID / DRY / KISS / YAGNI)

- [ ] Cada clase/módulo nuevo tiene un único motivo de cambio identificable — [`solid.md`](../knowledge/architecture/solid.md)
- [ ] No se ha modificado código estable y probado para añadir un caso nuevo cuando existía un punto de extensión disponible — [`solid.md`](../knowledge/architecture/solid.md)
- [ ] Cualquier subtipo introducido puede sustituir a su tipo base sin sorpresas — [`solid.md`](../knowledge/architecture/solid.md)
- [ ] Las interfaces expuestas son del tamaño mínimo que cada cliente necesita — [`solid.md`](../knowledge/architecture/solid.md)
- [ ] La lógica de negocio depende de abstracciones, no de frameworks/librerías concretas — [`solid.md`](../knowledge/architecture/solid.md)
- [ ] No hay duplicación de conocimiento de negocio (reglas, validaciones, configuración) — [`dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)
- [ ] No se ha introducido complejidad accidental por encima de la complejidad inherente del problema — [`dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)
- [ ] No se ha añadido abstracción, configuración o punto de extensión para un requisito hipotético sin confirmar — [`dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)

## Modelado de dominio (DDD)

- [ ] Los Bounded Contexts y sus fronteras están identificados si el cambio afecta al modelo de dominio — [`ddd.md`](../knowledge/architecture/ddd.md)
- [ ] El lenguaje del código coincide con el lenguaje del negocio en ese contexto — [`ddd.md`](../knowledge/architecture/ddd.md)
- [ ] Los "primitivos con significado" (dinero, email, rangos de fechas) están modelados como Value Objects — [`ddd.md`](../knowledge/architecture/ddd.md)
- [ ] Cada Agregado tiene una única Raíz que controla todas sus invariantes — [`ddd.md`](../knowledge/architecture/ddd.md)
- [ ] Las entidades no son anémicas: contienen comportamiento, no solo getters/setters — [`anti-patterns/AnemicDomainModel.md`](../anti-patterns/anemic-domain-model.md)

## Arquitectura en capas

- [ ] El código de `domain/` no importa nada de frameworks ni de infraestructura — [`clean-architecture.md`](../knowledge/architecture/clean-architecture.md)
- [ ] Los casos de uso dependen únicamente de interfaces (puertos) — [`clean-architecture.md`](../knowledge/architecture/clean-architecture.md)
- [ ] Existe mapeo explícito entre DTOs, entidades de dominio y entidades de persistencia — [`clean-architecture.md`](../knowledge/architecture/clean-architecture.md)

## Anti-patrones

- [ ] No hay indicios de [God Object](../anti-patterns/god-object.md)
- [ ] No hay indicios de [Spaghetti Code](../anti-patterns/spaghetti.md)
- [ ] No hay indicios de [N+1 Query](../anti-patterns/n-plus-one.md) en código que accede a colecciones con relaciones
- [ ] No hay concatenación de SQL con input de usuario ([SQL Injection](../anti-patterns/sql-injection.md))
- [ ] No hay listeners/recursos sin liberación simétrica ([Memory Leak](../anti-patterns/memory-leak.md))
- [ ] No hay callbacks anidados más de 2-3 niveles ([Callback Hell](../anti-patterns/callback-hell.md))

## Testing

- [ ] Todo caso de uso nuevo tiene test de camino feliz, validación fallida y caso límite — [`testing.md`](../knowledge/architecture/testing.md)
- [ ] Los tests unitarios no dependen de I/O real — [`testing.md`](../knowledge/architecture/testing.md)
- [ ] Los tests son deterministas (sin `sleep` arbitrarios, con reloj fijado si aplica) — [`testing.md`](../knowledge/architecture/testing.md)

## Refactoring (si aplica)

- [ ] Existe red de seguridad de tests antes de refactorizar — [`refactoring.md`](../knowledge/architecture/refactoring.md)
- [ ] El refactor está separado de cualquier cambio funcional en el mismo PR — [`refactoring.md`](../knowledge/architecture/refactoring.md)

## Logging y Observabilidad

- [ ] Los logs nuevos son estructurados y no exponen datos sensibles — [`logging.md`](../knowledge/architecture/logging.md)
- [ ] Se usa el logger configurado del framework, no `console.log`/`System.out.println` — [`logging.md`](../knowledge/architecture/logging.md)

## Documentación

- [ ] Existe un ADR si el cambio implica una decisión arquitectónica relevante — [`documentacion-tecnica.md`](../knowledge/architecture/technical-documentation.md)
- [ ] La documentación de API (OpenAPI/Swagger) está actualizada si el contrato cambió — [`openapi.md`](../knowledge/architecture/openapi.md)
- [ ] El README sigue permitiendo levantar el proyecto sin conocimiento tribal adicional — [`documentacion-tecnica.md`](../knowledge/architecture/technical-documentation.md)
