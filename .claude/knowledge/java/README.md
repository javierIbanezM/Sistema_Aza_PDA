# Knowledge — Java

Índice del **lenguaje Java en sí**, independiente de cualquier framework. Spring vive en [`../spring/`](../spring/), Hibernate/JPA en [`../hibernate/`](../hibernate/), Lombok/MapStruct/Maven/Gradle/JUnit/Mockito tienen cada uno su propia carpeta — no se duplican aquí. Los patrones de diseño y anti-patrones son globales, en [`../../patterns/`](../../patterns/) y [`../../anti-patterns/`](../../anti-patterns/).

| Documento | Descripción |
|---|---|
| [`language.md`](./language.md) | Fundamentos: null-safety, inmutabilidad, interfaces con métodos por defecto, `var`, text blocks |
| [`collections.md`](./collections.md) | Elección de estructura de datos, colecciones inmutables, comparadores |
| [`streams.md`](./streams.md) | Operaciones funcionales, streams paralelos, errores comunes |
| [`concurrency.md`](./concurrency.md) | `ExecutorService`, `CompletableFuture`, estructuras concurrentes, deadlocks |
| [`virtual-threads.md`](./virtual-threads.md) | Project Loom (Java 21+): cuándo aportan valor, integración con Spring Boot, pinning |
| [`records.md`](./records.md) | Value Objects idiomáticos, constructor compacto, cuándo NO usar un record |
| [`sealed-classes.md`](./sealed-classes.md) | Jerarquías cerradas, pattern matching exhaustivo, comparación con enum |
| [`exceptions.md`](../../standards/exceptions.md) | Checked vs. unchecked, try-with-resources, multi-catch (mecánica del lenguaje) |
| [`generics.md`](./generics.md) | PECS, métodos genéricos, type erasure |
| [`performance.md`](./performance.md) | Profiling JFR/JMC, GC, pool de conexiones (nivel JVM) |
| [`debugging.md`](./debugging.md) | Stack traces, `jstack`, remote debugging, `git bisect` (nivel lenguaje/JVM) |
| [`best-practices.md`](./best-practices.md) | Síntesis de las reglas más importantes de Java puro |
| [`pitfalls.md`](./pitfalls.md) | Errores frecuentes específicos del lenguaje |
| [`examples.md`](./examples.md) | Casos de uso completos (referencian Spring/Hibernate donde aplica, sin duplicarlos) |

## Dominios relacionados (no duplicados aquí)

| Tecnología | Carpeta |
|---|---|
| Spring / Spring Boot | [`../spring/`](../spring/) |
| Hibernate / JPA | [`../hibernate/`](../hibernate/) |
| Lombok | [`../lombok/`](../lombok/) |
| MapStruct | [`../mapstruct/`](../mapstruct/) |
| Maven | [`../maven/`](../maven/) |
| Gradle | [`../gradle/`](../gradle/) |
| JUnit | [`../junit/`](../junit/) |
| Mockito | [`../mockito/`](../mockito/) |

## Cómo usar este índice

- Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí (regla de oro nº4).
- Si el concepto es transversal a cualquier lenguaje (SOLID, DDD, Clean Architecture, testing en general), no lo dupliques aquí — está en [`../architecture/`](../architecture/).
- Si es un patrón o anti-patrón de diseño (Repository, God Object...), va en [`../../patterns/`](../../patterns/) / [`../../anti-patterns/`](../../anti-patterns/), global.
