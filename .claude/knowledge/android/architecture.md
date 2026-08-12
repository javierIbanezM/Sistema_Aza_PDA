# Architecture (Android)

> Ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md) para el razonamiento general de capas que este documento aplica concretamente a Android con MVVM.

## Capas: UI (Compose) → ViewModel → UseCase → Repository → DataSource

```kotlin
// domain/ — sin ninguna dependencia de Android SDK
class Pedido(val id: String, val estado: EstadoPedido, private val lineas: MutableList<LineaPedido>) {
    fun confirmar() {
        check(lineas.isNotEmpty()) { "No se puede confirmar un pedido sin líneas" }
        // ...
    }
}

interface PedidoRepository { // puerto, en domain/ o data/
    suspend fun obtenerPedido(id: String): Pedido
}

class ConfirmarPedidoUseCase(private val repository: PedidoRepository) { // application/
    suspend operator fun invoke(id: String) {
        val pedido = repository.obtenerPedido(id)
        pedido.confirmar()
        repository.guardar(pedido)
    }
}

class PedidoRepositoryImpl( // data/ — implementación concreta con Room + Retrofit
    private val dao: PedidoDao,
    private val api: PedidoApi,
) : PedidoRepository {
    override suspend fun obtenerPedido(id: String): Pedido =
        dao.obtenerPorId(id)?.toDominio() ?: api.obtenerPedido(id).toDominio()
}
```

`domain/` no importa nada de `android.*` — igual principio que en cualquier otro dominio del framework (ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md)), verificable incluso en un módulo Gradle separado que no dependa del SDK de Android.

## ViewModel: expone estado inmutable, nunca mutable

```kotlin
class PedidoViewModel(private val confirmarPedido: ConfirmarPedidoUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<PedidoUiState>(PedidoUiState.Cargando)
    val uiState: StateFlow<PedidoUiState> = _uiState.asStateFlow() // solo lectura hacia fuera
}
```

Ver [`../kotlin/flow.md`](../kotlin/flow.md) — el `MutableStateFlow` nunca se expone directamente, solo su vista de solo lectura.

## Use Case: una única función pública

```kotlin
class ConfirmarPedidoUseCase(private val repository: PedidoRepository) {
    suspend operator fun invoke(id: String) { /* ... */ } // operator fun invoke: se llama como confirmarPedido(id)
}
```

Un Use Case por operación de negocio, con `operator fun invoke` como único punto de entrada — mismo principio que los casos de uso de Java/Node en este framework (ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md)), expresado de forma idiomática en Kotlin.

## Repository: única fuente de verdad

```kotlin
class PedidoRepositoryImpl(
    private val dao: PedidoDao,        // Room, ver ../room/room.md
    private val api: PedidoApi,        // Retrofit, ver ../retrofit/retrofit.md
) : PedidoRepository {
    override suspend fun obtenerPedido(id: String): Pedido {
        return dao.obtenerPorId(id)?.toDominio()
            ?: api.obtenerPedido(id).toDominio().also { dao.insertar(it.toEntity()) } // cache-aside
    }
}
```

Ver [`../../patterns/repository.md`](../../patterns/repository.md) — decide entre caché local (Room) y remoto (Retrofit), aplicando el patrón *single source of truth*.

## Estructura de carpetas recomendada

```
app/
├── domain/       (Pedido, PedidoRepository interfaz, casos de uso)
├── data/         (PedidoRepositoryImpl, PedidoDao, PedidoApi, mappers)
├── ui/           (Composables, ViewModels)
└── di/           (módulos Hilt, ver ../hilt/hilt.md)
```

## Checklist

- [ ] ¿`domain/` está libre de imports de `android.*`?
- [ ] ¿El `ViewModel` expone únicamente `StateFlow` de solo lectura, nunca `MutableStateFlow` público?
- [ ] ¿Cada Use Case tiene una única función pública (`operator fun invoke`)?
- [ ] ¿El Repository decide entre caché local y remoto, siendo la única fuente de verdad para la UI?

Ver también: [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md), [`../kotlin/`](../kotlin/), [`../compose/compose.md`](../compose/compose.md).
