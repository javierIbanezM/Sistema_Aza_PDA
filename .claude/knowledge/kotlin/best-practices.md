# Best Practices (Kotlin — lenguaje)

> Síntesis del lenguaje Kotlin puro. Para Android como plataforma, ver [`../android/best-practices.md`](../android/best-practices.md) — no se duplica aquí.

## Null-safety

- Evitar `!!` salvo certeza absoluta documentada — preferir safe call (`?.`) + elvis (`?:`) (ver [`language.md`](./language.md)).
- Modelar la ausencia de valor con el sistema de tipos (`String?`), no con valores centinela (`""`, `-1`).

## Modelado de datos

- `data class` para modelos inmutables, `sealed class`/`sealed interface` para conjuntos cerrados de variantes con `when` exhaustivo.
- `val` por defecto, `var` solo cuando la reasignación es genuinamente necesaria.

## Coroutines y Flow

- Dispatcher correcto según el tipo de trabajo: `IO` para I/O, `Default` para CPU, `Main` solo para UI (ver [`coroutines.md`](./coroutines.md)).
- Coroutines atadas a un scope con ciclo de vida (`viewModelScope`/`lifecycleScope`), nunca `GlobalScope`.
- `StateFlow` para estado, `SharedFlow` para eventos de una vez (ver [`flow.md`](./flow.md)).
- `CancellationException` siempre relanzada si se captura accidentalmente.

## Legibilidad

- Extension functions para enriquecer APIs sin herencia; scope functions (`let`/`apply`/`also`) con criterio, no anidadas indiscriminadamente.

Ver [`pitfalls.md`](./pitfalls.md) y [`examples.md`](./examples.md).
