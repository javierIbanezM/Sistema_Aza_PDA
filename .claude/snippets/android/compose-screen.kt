// snippets/android/compose-screen.kt
// Pantalla Compose completa con ViewModel + StateFlow — ver recipes/android-create-compose-screen.md.

sealed interface PedidoUiState {
    object Cargando : PedidoUiState
    data class Exito(val pedido: PedidoUiModel) : PedidoUiState
    data class Error(val mensaje: String) : PedidoUiState
}

@Immutable
data class PedidoUiModel(val id: String, val total: String, val estado: String)

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

@Composable
private fun ContenidoPedido(pedido: PedidoUiModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pedido ${pedido.id}", style = MaterialTheme.typography.headlineSmall)
        Text("Total: ${pedido.total}")
        Text("Estado: ${pedido.estado}")
    }
}
