# Examples (Kotlin)

## Ejemplo — ViewModel con StateFlow y manejo de errores estructurado

```kotlin
sealed interface PedidoUiState {
    object Cargando : PedidoUiState
    data class Exito(val pedido: Pedido) : PedidoUiState
    data class Error(val mensaje: String) : PedidoUiState
}

class PedidoViewModel(private val repository: PedidoRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<PedidoUiState>(PedidoUiState.Cargando)
    val uiState: StateFlow<PedidoUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.value = PedidoUiState.Cargando
            _uiState.value = try {
                PedidoUiState.Exito(repository.obtenerPedido(id))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                PedidoUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
```

Combina [`language.md`](./language.md) (sealed interface, data class), [`coroutines.md`](./coroutines.md) (viewModelScope, manejo de cancelación) y [`flow.md`](./flow.md) (StateFlow) — ver [`../android/architecture.md`](../android/architecture.md) para cómo este ViewModel se integra en la arquitectura completa de una pantalla Android.

Ver también: [`../android/examples.md`](../android/examples.md) para el ejemplo completo incluyendo la capa de UI con Compose.
