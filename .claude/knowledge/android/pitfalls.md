# Pitfalls (Android) — Errores frecuentes

> Para trampas del lenguaje Kotlin puro, ver [`../kotlin/pitfalls.md`](../kotlin/pitfalls.md).

## I/O síncrono en el hilo principal

```kotlin
fun cargar() { File("datos.json").readText() } // bloquea el hilo de UI, riesgo de ANR
```

Ver [`performance.md`](./performance.md) — mover a `Dispatchers.IO`.

## Estado guardado en la Activity en lugar del ViewModel

```kotlin
class PedidoActivity : ComponentActivity() {
    private var pedidoActual: Pedido? = null // se pierde en cada rotación de pantalla
}
```

Ver [`lifecycle.md`](./lifecycle.md) — el estado que debe sobrevivir a rotación vive en el ViewModel.

## Asumir que un permiso concedido sigue vigente

```kotlin
if (permisoYaConcedidoAntes) { usarCamara() } // el usuario pudo revocarlo desde Ajustes desde entonces
```

Ver [`permissions.md`](./permissions.md) — verificar antes de cada uso.

## Cargar imágenes a resolución completa en miniaturas

```kotlin
AsyncImage(model = urlImagenOriginal4K, /* sin especificar size */) // desperdicia memoria proporcionalmente
```

Ver [`performance.md`](./performance.md).

## Checklist de revisión rápida

- [ ] ¿Hay I/O síncrono en el hilo principal?
- [ ] ¿Hay estado crítico guardado en la Activity/Fragment en lugar del ViewModel?
- [ ] ¿Se verifica el permiso antes de cada uso de la funcionalidad protegida?

Ver también: [`best-practices.md`](./best-practices.md).
