# Compose (Jetpack Compose)

> Ver [`../android/architecture.md`](../android/architecture.md) para cómo un Composable encaja en la arquitectura general de una pantalla Android.

## State hoisting: Composables stateless por defecto

```kotlin
@Composable
fun CampoBusqueda(valor: String, onValorCambiado: (String) -> Unit) { // sin estado propio
    TextField(value = valor, onValueChange = onValorCambiado)
}

@Composable
fun PantallaBusqueda() {
    var texto by remember { mutableStateOf("") } // el estado "sube" al padre que lo necesita
    CampoBusqueda(valor = texto, onValorCambiado = { texto = it })
}
```

*State hoisting*: el estado se declara en el nivel más alto donde es necesario, y se pasa hacia abajo como parámetros + callbacks — el Composable hijo no tiene estado propio, es una función pura de sus parámetros de entrada. Facilita testear, reutilizar y razonar sobre cada Composable de forma aislada.

## `remember` vs. `rememberSaveable`

```kotlin
var contador by remember { mutableStateOf(0) }         // se pierde en rotación de pantalla
var texto by rememberSaveable { mutableStateOf("") }    // sobrevive a rotación (vía Bundle)
```

Ver [`../android/lifecycle.md`](../android/lifecycle.md) — `rememberSaveable` para estado de UI que el usuario esperaría que sobreviva a un giro de pantalla (texto escrito en un formulario); `remember` para estado derivado/efímero que es aceptable recalcular.

## Recomposición: qué la dispara y cómo evitarla innecesariamente

```kotlin
@Composable
fun ListaPedidos(pedidos: List<Pedido>) {
    LazyColumn {
        items(pedidos, key = { it.id }) { pedido -> // "key" evita recomposiciones innecesarias al reordenar/filtrar
            ItemPedido(pedido)
        }
    }
}
```

Compose recompone (vuelve a ejecutar) un Composable cuando alguno de sus parámetros de entrada cambia — proveer una `key` estable en listas evita que Compose trate elementos reordenados como "nuevos" y los recomponga innecesariamente. Ver [`performance.md`](./performance.md) para el diagnóstico y optimización completos de recomposición excesiva.

## Efectos secundarios: `LaunchedEffect`, `DisposableEffect`

```kotlin
@Composable
fun PantallaPedido(id: String, viewModel: PedidoViewModel) {
    LaunchedEffect(id) { // se relanza si "id" cambia; se cancela si el Composable sale de composición
        viewModel.cargar(id)
    }
}

@Composable
fun MonitorDeConexion() {
    DisposableEffect(Unit) {
        val listener = registrarListenerConexion()
        onDispose { desregistrarListener(listener) } // limpieza simétrica al salir de composición
    }
}
```

`LaunchedEffect` para lanzar coroutines vinculadas al ciclo de vida del Composable; `DisposableEffect` cuando se necesita limpieza explícita (des-registrar un listener, cerrar un recurso) — mismo principio de limpieza simétrica que en [`../javascript/dom.md`](../javascript/dom.md), expresado con la API de efectos de Compose.

## Modificadores (`Modifier`): orden importa

```kotlin
Box(
    modifier = Modifier
        .padding(16.dp)      // primero el padding externo
        .background(Color.Gray) // luego el fondo, que NO incluye el padding anterior
        .padding(8.dp)        // padding interno adicional, dentro del fondo
)
```

Los modificadores se aplican en el orden declarado — `padding` antes de `background` deja el padding fuera del área coloreada; invertir el orden lo incluiría dentro. Un error común es no considerar este orden y obtener un resultado visual inesperado.

## Checklist

- [ ] ¿Los Composables son stateless por defecto, con el estado elevado al nivel apropiado (state hoisting)?
- [ ] ¿Las listas usan `key` estable para evitar recomposiciones innecesarias al reordenar?
- [ ] ¿`rememberSaveable` se usa para estado que debe sobrevivir a rotación, `remember` para el resto?
- [ ] ¿Todo `DisposableEffect` tiene su `onDispose` con limpieza simétrica?

Ver también: [`performance.md`](./performance.md), [`../android/architecture.md`](../android/architecture.md), [`../material3/material3.md`](../material3/material3.md).
