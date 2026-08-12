# Pitfalls (Kotlin) — Errores frecuentes

> Ver [`../android/pitfalls.md`](../android/pitfalls.md) para trampas específicas de la plataforma Android.

## `!!` como solución rápida a un error de compilación

```kotlin
val nombre = usuario!!.nombre // NPE en runtime si usuario es null — reintroduce el problema que Kotlin evita
```

Ver [`language.md`](./language.md) — usar `?.`/`?:` o restructurar para que la nulidad se maneje explícitamente.

## Capturar `CancellationException` sin relanzarla

```kotlin
try {
    trabajoLargo()
} catch (e: Exception) { // captura también CancellationException, rompiendo la cancelación estructurada
    logger.error(e)
}
```

Ver [`coroutines.md`](./coroutines.md) — siempre `if (e is CancellationException) throw e` o capturar tipos más específicos.

## `GlobalScope` para lanzar coroutines

```kotlin
GlobalScope.launch { sincronizar() } // sobrevive a la destrucción de la pantalla que lo originó, fuga de recursos
```

Ver [`coroutines.md`](./coroutines.md) — usar `viewModelScope`/`lifecycleScope` según el ciclo de vida correcto.

## `flowOn` colocado en el punto incorrecto de la cadena

```kotlin
flow { emit(dato()) }
    .map { transformar(it) }
    .flowOn(Dispatchers.IO) // solo afecta a lo ANTERIOR — "transformar" también corre en IO, quizás no se pretendía
```

Ver [`flow.md`](./flow.md).

## Checklist de revisión rápida

- [ ] ¿Hay algún `!!` sin justificación explícita?
- [ ] ¿Se captura `CancellationException` sin relanzarla en algún `catch` genérico?
- [ ] ¿Se usa `GlobalScope` en lugar de un scope con ciclo de vida?

Ver también: [`best-practices.md`](./best-practices.md).
