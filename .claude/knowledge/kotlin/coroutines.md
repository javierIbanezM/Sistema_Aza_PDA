# Coroutines

> Ver [`flow.md`](./flow.md) para flujos de datos asíncronos construidos sobre coroutines. Ver [`../node/async-patterns.md`](../node/async-patterns.md) para el concepto equivalente (`async/await`) en otro lenguaje del framework.

## `suspend fun`: el equivalente Kotlin de `async`

```kotlin
suspend fun obtenerPedido(id: String): Pedido {
    return pedidoApi.buscarPorId(id) // suspende sin bloquear el hilo mientras espera la respuesta
}
```

Una función `suspend` solo puede llamarse desde otra función `suspend` o desde un `CoroutineScope` — el compilador lo verifica, a diferencia de `async/await` en JavaScript donde no hay esa restricción de contexto.

## `CoroutineScope` y estructuración de vida

```kotlin
class PedidoViewModel : ViewModel() {
    fun cargarPedido(id: String) {
        viewModelScope.launch { // atado al ciclo de vida del ViewModel — se cancela automáticamente
            val pedido = repository.obtenerPedido(id)
            _uiState.value = UiState.Exito(pedido)
        }
    }
}
```

*Structured concurrency*: toda coroutine lanzada dentro de un scope se cancela automáticamente cuando ese scope termina (`viewModelScope` al destruirse el ViewModel, `lifecycleScope` al destruirse la Activity/Fragment) — evita el equivalente Android de una Promise "huérfana" que sigue ejecutándose tras destruir la pantalla que la originó.

## `Dispatchers`: dónde se ejecuta cada coroutine

```kotlin
suspend fun procesarPedido(pedido: Pedido) = withContext(Dispatchers.IO) {
    // trabajo de I/O (red, disco, base de datos) — nunca en el hilo principal
    guardarEnBaseDeDatos(pedido)
}

suspend fun calcularResumen(datos: List<Pedido>) = withContext(Dispatchers.Default) {
    // trabajo de CPU intensivo — pool de hilos dedicado, distinto del de I/O
    datos.map { it.calcularTotal() }.sum()
}
```

`Dispatchers.Main` (hilo de UI, para actualizar vistas), `Dispatchers.IO` (pool optimizado para operaciones bloqueantes de I/O), `Dispatchers.Default` (pool optimizado para CPU) — elegir el dispatcher incorrecto es la causa más común de bloqueo de UI o de desperdicio de hilos en Android.

## Manejo de errores y cancelación

```kotlin
viewModelScope.launch {
    try {
        val pedido = repository.obtenerPedido(id)
        _uiState.value = UiState.Exito(pedido)
    } catch (e: CancellationException) {
        throw e // NUNCA capturar CancellationException sin relanzarla — rompe la cancelación estructurada
    } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message)
    }
}
```

`CancellationException` es un mecanismo interno del framework de coroutines para propagar la cancelación — capturarla sin relanzarla (con un `catch (e: Exception)` genérico que la intercepta) rompe la cancelación estructurada, dejando trabajo "zombie" ejecutándose tras cancelar el scope padre.

## `async`/`await` para paralelismo estructurado

```kotlin
suspend fun cargarPantallaPedido(id: String): PantallaPedidoData = coroutineScope {
    val pedido = async { repository.obtenerPedido(id) }       // ambas inician en paralelo
    val historial = async { repository.obtenerHistorial(id) }  // sin esperar la una a la otra
    PantallaPedidoData(pedido.await(), historial.await())       // se espera a ambas aquí
}
```

Equivalente a `Promise.all` (ver [`../node/async-patterns.md`](../node/async-patterns.md)) — `async` inicia la coroutine inmediatamente y `await()` espera su resultado; si no se necesita paralelismo, `suspend fun` secuenciales con `await` implícito son más simples.

## Checklist

- [ ] ¿El dispatcher usado corresponde al tipo de trabajo (IO para I/O, Default para CPU, Main solo para UI)?
- [ ] ¿Las coroutines de larga duración están atadas a un scope con ciclo de vida (`viewModelScope`/`lifecycleScope`), no a `GlobalScope`?
- [ ] ¿`CancellationException` siempre se relanza si se captura accidentalmente en un `catch` genérico?

Ver también: [`flow.md`](./flow.md), [`../android/lifecycle.md`](../android/lifecycle.md).
