# Recipe: Crear una Pantalla Compose (Android)

Aplica: [`../knowledge/android/architecture.md`](../knowledge/android/architecture.md), [`../knowledge/compose/compose.md`](../knowledge/compose/compose.md), [`../knowledge/kotlin/flow.md`](../knowledge/kotlin/flow.md).

## Pasos

### 1. Definir el estado de UI como sealed interface

```kotlin
sealed interface PedidoUiState {
    object Cargando : PedidoUiState
    data class Exito(val pedido: PedidoUiModel) : PedidoUiState
    data class Error(val mensaje: String) : PedidoUiState
}

data class PedidoUiModel(val id: String, val total: String, val estado: String) // ver compose/performance.md, @Immutable
```

### 2. ViewModel con StateFlow

```kotlin
@HiltViewModel
class PedidoViewModel @Inject constructor(
    private val obtenerPedido: ObtenerPedidoUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PedidoUiState>(PedidoUiState.Cargando)
    val uiState: StateFlow<PedidoUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.value = PedidoUiState.Cargando
            _uiState.value = try {
                PedidoUiState.Exito(obtenerPedido(id).toUiModel())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                PedidoUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
```

### 3. Composable de pantalla, recolectando el estado con ciclo de vida

```kotlin
@Composable
fun PantallaPedido(id: String, viewModel: PedidoViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(id) { viewModel.cargar(id) }

    when (val estado = uiState) {
        PedidoUiState.Cargando -> CircularProgressIndicator()
        is PedidoUiState.Exito -> ContenidoPedido(estado.pedido)
        is PedidoUiState.Error -> MensajeError(estado.mensaje, onReintentar = { viewModel.cargar(id) })
    }
}
```

### 4. Composable de contenido, stateless

```kotlin
@Composable
private fun ContenidoPedido(pedido: PedidoUiModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pedido ${pedido.id}", style = MaterialTheme.typography.headlineSmall)
        Text("Total: ${pedido.total}")
        Text("Estado: ${pedido.estado}")
    }
}
```

### 5. Tests del ViewModel (sin Compose, sin Android SDK)

```kotlin
@Test
fun `debe mostrar estado de exito tras cargar correctamente`() = runTest {
    val useCaseFake = ObtenerPedidoUseCaseFake(pedido = pedidoDePrueba())
    val viewModel = PedidoViewModel(useCaseFake)

    viewModel.cargar("p-1")

    assertThat(viewModel.uiState.value).isInstanceOf(PedidoUiState.Exito::class.java)
}
```

### 6. Test de UI con Compose Testing (si aplica)

```kotlin
@Test
fun muestraElTotalDelPedido() {
    composeTestRule.setContent { ContenidoPedido(PedidoUiModel("p-1", "€50.00", "CONFIRMADO")) }
    composeTestRule.onNodeWithText("Total: €50.00").assertIsDisplayed()
}
```

## Checklist final

- [ ] ¿El estado de UI es un `sealed interface` exhaustivo?
- [ ] ¿El ViewModel expone solo `StateFlow` de solo lectura?
- [ ] ¿La UI recolecta con `collectAsStateWithLifecycle`?
- [ ] ¿Los Composables de contenido son stateless (state hoisting)?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/android/compose-screen.kt`](../snippets/android/compose-screen.kt).
