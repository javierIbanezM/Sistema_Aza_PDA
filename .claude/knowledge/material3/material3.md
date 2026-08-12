# Material Design 3

> Ver [`../compose/compose.md`](../compose/compose.md) para los fundamentos de Compose sobre los que se construyen estos componentes visuales.

## Theming dinámico (Android 12+)

```kotlin
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // paleta derivada del wallpaper del usuario — Material You
    } else {
        if (isSystemInDarkTheme()) paletaOscuraEstatica else paletaClaraEstatica // fallback para versiones anteriores
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

El theming dinámico deriva la paleta de colores del wallpaper del usuario en Android 12+ — siempre con una paleta estática de fallback definida por la marca para versiones anteriores, ya que la API no existe en ellas.

## Componentes estructurales: `Scaffold`

```kotlin
@Composable
fun PantallaPedidos() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Pedidos") }) },
        floatingActionButton = { FloatingActionButton(onClick = { }) { Icon(Icons.Default.Add, null) } },
        bottomBar = { NavigationBar { /* items */ } },
    ) { paddingValues ->
        ListaPedidos(modifier = Modifier.padding(paddingValues)) // respeta el espacio de topBar/bottomBar
    }
}
```

`Scaffold` gestiona automáticamente el layout estructural estándar (barra superior, contenido, barra inferior, FAB) y expone el `paddingValues` correcto para que el contenido no quede oculto tras esos elementos — reimplementar este layout manualmente es una fuente común de bugs de superposición visual.

## Accesibilidad: no opcional

```kotlin
Icon(
    imageVector = Icons.Default.Delete,
    contentDescription = "Eliminar pedido", // OBLIGATORIO si el icono es interactivo por sí solo
)

IconButton(
    onClick = { eliminar() },
    modifier = Modifier.size(48.dp), // tamaño táctil mínimo recomendado, incluso si el icono visual es más pequeño
) { /* ... */ }
```

`contentDescription = null` es válido **solo** cuando el elemento es puramente decorativo y ya existe texto adyacente que describe la acción — para cualquier elemento interactivo sin texto visible, un `contentDescription` es obligatorio para que lectores de pantalla (TalkBack) puedan describirlo al usuario.

## Componentes de formulario con validación visual

```kotlin
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    isError = email.isNotEmpty() && !email.esEmailValido(), // ver ../kotlin/language.md para la extension function
    supportingText = {
        if (email.isNotEmpty() && !email.esEmailValido()) Text("Email no válido")
    },
)
```

## Checklist

- [ ] ¿Existe una paleta estática de fallback para versiones anteriores a Android 12, no solo theming dinámico?
- [ ] ¿Todo elemento interactivo sin texto visible tiene `contentDescription`?
- [ ] ¿Los elementos táctiles tienen un tamaño mínimo de 48dp, incluso si el icono visual es más pequeño?

Ver también: [`../compose/compose.md`](../compose/compose.md), [`../android/architecture.md`](../android/architecture.md).
