# Lifecycle (Android)

> Ver [`../kotlin/coroutines.md`](../kotlin/coroutines.md) para `viewModelScope`/`lifecycleScope`, directamente ligados al ciclo de vida descrito aquí.

## Ciclo de vida de una Activity/Fragment

```
onCreate → onStart → onResume → [en primer plano] → onPause → onStop → onDestroy
                                        ↕ (rotación, minimizar/restaurar)
```

`onCreate` se ejecuta solo una vez por instancia — pero un cambio de configuración (rotación de pantalla) **destruye y recrea** la Activity por defecto, ejecutando `onCreate` de nuevo. Cualquier estado que deba sobrevivir a esto debe manejarse explícitamente (`ViewModel`, que sobrevive a cambios de configuración, o `rememberSaveable` en Compose).

## `ViewModel`: sobrevive a cambios de configuración, no al cierre real

```kotlin
class PedidoViewModel : ViewModel() {
    override fun onCleared() {
        // se llama SOLO cuando la Activity/Fragment se destruye definitivamente,
        // no en cada rotación — punto correcto para liberar recursos
    }
}
```

Un `ViewModel` obtenido vía `viewModel()` (Compose) o `ViewModelProvider` sobrevive a la recreación de la Activity por rotación — el estado que contiene no se pierde ni se recalcula innecesariamente en cada giro de pantalla.

## `lifecycleScope` vs. `viewModelScope`

```kotlin
class PedidoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            // se cancela automáticamente en onDestroy de la Activity — NO sobrevive a rotación
        }
    }
}
```

`lifecycleScope` (atado a la Activity/Fragment) se cancela en cada destrucción, incluida la rotación; `viewModelScope` (atado al ViewModel) sobrevive a la rotación y solo se cancela cuando el ViewModel se destruye definitivamente — elegir el scope incorrecto es una causa común de trabajo cancelado prematuramente (una petición de red interrumpida por rotar la pantalla) o de fugas (trabajo que no se cancela cuando debería).

## `repeatOnLifecycle`: recolectar Flows de forma segura

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { estado -> actualizar(estado) }
        // se detiene automáticamente cuando la Activity pasa a background,
        // se reanuda automáticamente al volver a STARTED
    }
}
```

Evita procesar actualizaciones de un `Flow` mientras la UI no es visible — ver [`../kotlin/flow.md`](../kotlin/flow.md), mismo principio que `collectAsStateWithLifecycle` en Compose.

## `savedInstanceState`: sobrevivir incluso a la muerte del proceso

```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    outState.putString("pedido_id", pedidoIdActual)
}
```

A diferencia del `ViewModel` (que no sobrevive si el sistema operativo mata el proceso completo por presión de memoria mientras la app está en background), `savedInstanceState` sí persiste ese caso — usar para el mínimo de estado necesario para reconstruir la pantalla, no como almacenamiento general.

## Checklist

- [ ] ¿El estado que debe sobrevivir a rotación vive en el `ViewModel`, no en la Activity/Fragment/Composable directamente?
- [ ] ¿Se ha elegido el scope correcto (`viewModelScope` vs. `lifecycleScope`) según si el trabajo debe sobrevivir a rotación?
- [ ] ¿La recolección de `Flow` en la UI usa `repeatOnLifecycle`/`collectAsStateWithLifecycle`, no una recolección sin control de ciclo de vida?

Ver también: [`../kotlin/coroutines.md`](../kotlin/coroutines.md), [`../compose/compose.md`](../compose/compose.md).
