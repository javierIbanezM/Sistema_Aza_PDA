# Flow

> Ver [`coroutines.md`](./coroutines.md) para la base de coroutines sobre la que se construye `Flow`.

## `Flow` como stream asíncrono de múltiples valores

```kotlin
fun observarPedidos(): Flow<List<Pedido>> = flow {
    while (true) {
        emit(pedidoDao.obtenerTodos()) // emite un nuevo valor cada vez
        delay(5000)
    }
}
```

Mientras una `suspend fun` devuelve **un único** valor de forma asíncrona (equivalente a una Promise), `Flow` emite **cero o más** valores a lo largo del tiempo — el equivalente conceptual a un stream/observable reactivo, pero construido de forma nativa sobre coroutines.

## `StateFlow`: estado observable con valor actual siempre disponible

```kotlin
class PedidoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Cargando)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow() // expuesto como solo-lectura

    fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Cargando
            _uiState.value = try {
                UiState.Exito(repository.obtenerPedido(id))
            } catch (e: Exception) {
                UiState.Error(e.message)
            }
        }
    }
}
```

Patrón estándar en Android moderno: `MutableStateFlow` privado, expuesto como `StateFlow` de solo lectura — el `ViewModel` nunca expone la capacidad de mutar el estado desde fuera, la UI solo observa (ver [`../android/architecture.md`](../android/architecture.md)).

## Recoger un `Flow` de forma segura respecto al ciclo de vida

```kotlin
@Composable
fun PantallaPedido(viewModel: PedidoViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // recolecta solo mientras la UI está activa
    // ...
}
```

`collectAsStateWithLifecycle` (no el más antiguo `collectAsState`) pausa la recolección cuando la UI no está visible (app en segundo plano) — evita procesar/recolectar actualizaciones innecesarias cuando nadie las está viendo, con el consiguiente ahorro de batería/CPU.

## Operadores de transformación

```kotlin
pedidosFlow
    .filter { it.estado == EstadoPedido.CONFIRMADO }
    .map { it.calcularTotal() }
    .distinctUntilChanged() // evita re-emitir si el valor no cambió respecto al anterior
    .debounce(300) // agrupa emisiones rápidas consecutivas, útil para búsquedas con input del usuario
    .collect { total -> actualizar(total) }
```

## `flowOn`: cambiar el dispatcher de la parte upstream

```kotlin
fun observarPedidos(): Flow<List<Pedido>> = flow {
    emit(pedidoDao.obtenerTodos()) // operación de I/O
}.flowOn(Dispatchers.IO) // TODO lo anterior a este punto se ejecuta en Dispatchers.IO
```

`flowOn` afecta a los operadores **anteriores** a él en la cadena, no a los posteriores — un error común es colocarlo pensando que afecta a todo el flow, cuando en realidad solo cambia el contexto de ejecución "hacia arriba" en la cadena de construcción.

## `SharedFlow` para eventos únicos (no estado)

```kotlin
private val _eventos = MutableSharedFlow<Evento>()
val eventos: SharedFlow<Evento> = _eventos.asSharedFlow()

fun onPedidoConfirmado() {
    viewModelScope.launch { _eventos.emit(Evento.MostrarSnackbar("Pedido confirmado")) }
}
```

`StateFlow` siempre tiene un valor actual (apropiado para *estado*: "así está la pantalla ahora"); `SharedFlow` no retiene valor por defecto (apropiado para *eventos* de una vez: "muestra este snackbar", que no debería repetirse si la UI se recompone).

## Checklist

- [ ] ¿Se usa `StateFlow` para estado observable persistente, y `SharedFlow` para eventos de una única vez?
- [ ] ¿La UI recolecta con `collectAsStateWithLifecycle`, respetando el ciclo de vida?
- [ ] ¿`flowOn` está colocado en el punto correcto de la cadena, afectando solo a los operadores anteriores?

Ver también: [`coroutines.md`](./coroutines.md), [`../android/architecture.md`](../android/architecture.md), [`../compose/compose.md`](../compose/compose.md).
