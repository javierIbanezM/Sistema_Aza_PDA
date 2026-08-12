# Best Practices (Java — lenguaje)

> Síntesis de las reglas más importantes del **lenguaje Java puro**. Para Spring, Hibernate, Lombok, MapStruct, Maven/Gradle, JUnit/Mockito, ver el checklist específico de cada documento en su propia carpeta (`../spring/`, `../hibernate/`, `../lombok/`, etc.) — no se duplican aquí.

## Lenguaje

- Java 17+: usar `record` para DTOs/Value Objects inmutables (ver [`records.md`](./records.md)), `sealed`/pattern matching en `switch` cuando aporte claridad (ver [`sealed-classes.md`](./sealed-classes.md)).
- `Optional<T>` solo en retornos que pueden estar ausentes; nunca como parámetro de método ni como campo de una entidad (ver [`language.md`](./language.md)).
- Inmutabilidad por defecto: campos `final`, sin setters públicos indiscriminados.

## Colecciones y streams

- Elegir la estructura de datos según el patrón de acceso real, no por costumbre (ver [`collections.md`](./collections.md)).
- Streams para transformaciones declarativas simples; bucle explícito cuando la lógica tiene múltiples efectos secundarios entrelazados (ver [`streams.md`](./streams.md)).
- `parallelStream()` solo tras medir beneficio real, nunca por defecto.

## Concurrencia

- `java.util.concurrent` sobre `synchronized` manual salvo necesidad muy específica (ver [`concurrency.md`](./concurrency.md)).
- Virtual threads (Java 21+) para cargas I/O-bound de alta concurrencia — ver [`virtual-threads.md`](./virtual-threads.md), con atención al riesgo de *pinning* en bloques `synchronized`.

## Excepciones

- Excepciones de dominio unchecked, extendiendo una base propia del proyecto (ver [`exceptions.md`](../../standards/exceptions.md) y [`../../standards/exceptions.md`](../../standards/exceptions.md)).
- `try-with-resources` para todo recurso `AutoCloseable`.

## Genéricos

- PECS (`? extends` para lectura, `? super` para escritura) en parámetros de métodos genéricos (ver [`generics.md`](./generics.md)).

## Testing

- Pirámide de testing (ver [`../architecture/testing.md`](../architecture/testing.md)): mayoría de tests unitarios con JUnit 5 + Mockito (ver [`../junit/junit.md`](../junit/junit.md), [`../mockito/mockito.md`](../mockito/mockito.md)).

## Performance

- Medir con JFR/JMC antes de optimizar (ver [`performance.md`](./performance.md)).
- Descartar fugas de memoria (crecimiento sostenido sin caída tras GC) antes de atribuir latencia a otra causa.

Ver [`pitfalls.md`](./pitfalls.md) para los errores más frecuentes del lenguaje, y [`examples.md`](./examples.md) para casos de uso completos.
