# Performance (Compose)

> Ver [`compose.md`](./compose.md) para los fundamentos de recomposición. Ver [`../android/performance.md`](../android/performance.md) para rendimiento a nivel de plataforma.

## `@Stable` y `@Immutable`: ayudar al compilador a evitar recomposiciones

```kotlin
@Immutable // garantiza al compilador de Compose que este tipo nunca cambia tras construirse
data class PedidoUiModel(val id: String, val total: String, val estado: String)
```

Compose solo puede saltar la recomposición de un Composable si puede demostrar que sus parámetros no cambiaron — tipos con propiedades `var` o colecciones mutables (`List` sin garantía de inmutabilidad real, como `ArrayList` expuesto como `List`) impiden esa garantía. `@Immutable`/`@Stable` son promesas explícitas al compilador; usarlas incorrectamente (marcar como inmutable algo que sí muta) produce bugs de UI no actualizada, no solo una pérdida de optimización.

## Lambdas estables: `remember` para callbacks pasados a hijos

```kotlin
// Cada recomposición del padre crea una NUEVA lambda, forzando recomposición del hijo aunque nada relevante cambió
ItemPedido(onClick = { navegarADetalle(pedido.id) })

// Lambda recordada, misma identidad entre recomposiciones si sus dependencias no cambian
val onClickRecordado = remember(pedido.id) { { navegarADetalle(pedido.id) } }
ItemPedido(onClick = onClickRecordado)
```

## `derivedStateOf`: evitar recálculos en cada recomposición

```kotlin
val listaOrdenada by remember(pedidos) {
    derivedStateOf { pedidos.sortedByDescending { it.fecha } } // solo recalcula si "pedidos" cambia realmente
}
```

Sin `derivedStateOf`, un cálculo derivado de un estado que cambia con frecuencia (posición de scroll, por ejemplo) se recalcularía en cada recomposición aunque el resultado real no cambie — `derivedStateOf` memoriza el resultado y solo recalcula cuando sus dependencias cambian de verdad.

## `LazyColumn`/`LazyRow`: siempre con `key`

```kotlin
LazyColumn {
    items(pedidos, key = { it.id }) { pedido -> ItemPedido(pedido) } // ver compose.md
}
```

Sin `key`, insertar/eliminar/reordenar un elemento en medio de la lista puede hacer que Compose recomponga todos los elementos posteriores en lugar de solo el que cambió — regresión de rendimiento fácil de introducir sin darse cuenta en listas grandes.

## Diagnóstico con Layout Inspector: contador de recomposiciones

Android Studio → Layout Inspector muestra, por Composable, cuántas veces se ha recompuesto durante la sesión — un número desproporcionadamente alto en un Composable simple es la señal directa de un problema de estabilidad de parámetros (lambda no recordada, tipo no marcado como estable) que investigar con las técnicas de arriba.

## Evitar trabajo pesado directamente en el cuerpo del Composable

```kotlin
// Antipatrón: se recalcula en CADA recomposición, incluso si nada relevante cambió
@Composable
fun Resumen(pedidos: List<Pedido>) {
    val total = pedidos.sumOf { it.calcularTotal() } // recalculado en cada recomposición
    Text("Total: $total")
}

// Correcto: memorizado con remember/derivedStateOf según si depende de estado mutable
@Composable
fun Resumen(pedidos: List<Pedido>) {
    val total by remember(pedidos) { derivedStateOf { pedidos.sumOf { it.calcularTotal() } } }
    Text("Total: $total")
}
```

## Checklist

- [ ] ¿Los modelos de UI pasados a Composables están marcados `@Immutable`/`@Stable` cuando corresponde, y esa promesa es realmente cierta?
- [ ] ¿Las lambdas pasadas como callbacks a Composables hijos están `remember`-adas cuando el hijo es costoso de recomponer?
- [ ] ¿Los cálculos derivados de estado usan `derivedStateOf` en lugar de recalcularse en cada recomposición?
- [ ] ¿Se ha usado el contador de recomposiciones del Layout Inspector antes de optimizar por intuición?

Ver también: [`compose.md`](./compose.md), [`../android/performance.md`](../android/performance.md).
