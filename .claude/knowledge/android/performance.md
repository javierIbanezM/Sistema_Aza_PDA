# Performance (Android)

> Ver [`../compose/performance.md`](../compose/performance.md) para optimización específica de recomposición en Compose. Este documento cubre rendimiento a nivel de plataforma/aplicación.

## Hilo principal: nunca trabajo bloqueante

```kotlin
// Antipatrón: I/O síncrono en el hilo principal, congela la UI (ANR — Application Not Responding)
fun cargarConfiguracion(): Config {
    return File("config.json").readText().let { Json.decodeFromString(it) } // bloqueante en el hilo de UI
}

// Correcto: mover a un dispatcher de I/O
suspend fun cargarConfiguracion(): Config = withContext(Dispatchers.IO) {
    File("config.json").readText().let { Json.decodeFromString(it) }
}
```

Ver [`../kotlin/coroutines.md`](../kotlin/coroutines.md) — más de ~5 segundos de bloqueo del hilo principal dispara un diálogo de ANR visible para el usuario; incluso bloqueos menores producen jank (caídas de frames) perceptible.

## Baseline Profiles: reducir el tiempo de arranque

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
}
```

Un Baseline Profile precompila las rutas de código más usadas al arrancar la app, evitando la interpretación/compilación JIT en el primer uso — reduce significativamente el tiempo hasta que la app es interactiva, especialmente en dispositivos de gama baja.

## Tamaño de APK/AAB: R8 y `shrinkResources`

```kotlin
// build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = true // R8: elimina código no usado, ofusca
        isShrinkResources = true // elimina recursos (drawables, strings) no referenciados
    }
}
```

Un APK/AAB más pequeño reduce tanto el tiempo de descarga/instalación como el consumo de almacenamiento — verificar con el Android Studio APK Analyzer qué ocupa más espacio antes de optimizar a ciegas.

## `LayoutInspector`: detectar recomposiciones/layouts excesivos

Herramienta integrada en Android Studio que muestra la jerarquía de vistas en tiempo real y, en Compose, cuenta recomposiciones por Composable — el primer paso de diagnóstico ante lentitud de UI percibida, antes de optimizar por intuición (ver [`../compose/performance.md`](../compose/performance.md)).

## Imágenes: la causa más común de consumo de memoria excesivo

```kotlin
// Cargar con una librería que gestiona downsampling/caché automáticamente (Coil, Glide)
AsyncImage(
    model = ImageRequest.Builder(context).data(url).size(Size(200, 200)).build(), // tamaño explícito, no la imagen completa
    contentDescription = null,
)
```

Cargar una imagen a su resolución completa cuando se muestra en un espacio pequeño desperdicia memoria proporcionalmente al tamaño real de la imagen — especificar el tamaño objetivo permite a la librería de carga hacer downsampling antes de decodificar completamente en memoria.

## Checklist

- [ ] ¿Existe algún trabajo bloqueante (I/O, cálculo pesado) ejecutándose en el hilo principal?
- [ ] ¿Se usa un Baseline Profile en builds de producción?
- [ ] ¿`minifyEnabled`/`shrinkResources` están activos en el build de release?
- [ ] ¿Las imágenes se cargan con tamaño objetivo explícito, evitando decodificar a resolución completa innecesariamente?

Ver también: [`../compose/performance.md`](../compose/performance.md), [`../kotlin/coroutines.md`](../kotlin/coroutines.md).
