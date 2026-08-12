# Debugging (Android)

> Ver [`../architecture/debugging.md`](../architecture/debugging.md) para la metodología sistemática completa.

## Logcat con filtrado efectivo

```kotlin
Timber.tag("PedidoViewModel").d("Cargando pedido %s", id) // Timber sobre Log.d nativo: tags automáticos, desactivable en release
```

Filtrar Logcat por tag y nivel (`adb logcat PedidoViewModel:D *:S` — silencia todo lo demás) en lugar de leer el stream completo sin filtrar, especialmente en apps con librerías de terceros que generan mucho ruido de log propio.

## Depurar ANRs (Application Not Responding)

Un ANR indica que el hilo principal estuvo bloqueado más de ~5 segundos — Android Studio → Profiler → captura de traza en el momento del ANR muestra exactamente qué método bloqueaba el hilo principal. Ver [`performance.md`](./performance.md) para las causas más comunes (I/O síncrono, cálculo pesado) y su corrección.

## Layout Inspector para jerarquías de vista inesperadas

Muestra la jerarquía de vistas/Composables en tiempo real sobre un dispositivo/emulador conectado — útil cuando un elemento no aparece donde se espera, o cuando hay overdraw (elementos dibujados y luego ocultos por otros encima) no evidente solo leyendo el código.

## Depurar coroutines con nombres explícitos

```kotlin
viewModelScope.launch(CoroutineName("CargarPedido")) {
    // el nombre aparece en las trazas de depuración/crashes, facilitando identificar
    // qué coroutine específica falló cuando hay muchas ejecutándose concurrentemente
}
```

## `StrictMode`: detectar violaciones de buenas prácticas automáticamente

```kotlin
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectDiskReads().detectDiskWrites().detectNetwork() // detecta I/O accidental en el hilo principal
            .penaltyLog()
            .build()
    )
}
```

Habilitar solo en builds de debug — genera logs (o incluso crashes controlados, con `penaltyDeath()`) ante operaciones bloqueantes en el hilo principal que de otro modo podrían pasar desapercibidas hasta manifestarse como un ANR intermitente en producción.

## `git bisect` combinado con tests instrumentados

```bash
git bisect start
git bisect bad
git bisect good v2.3.0
git bisect run ./gradlew connectedAndroidTest --tests "*PedidoFlowTest*"
```

Ver [`../architecture/debugging.md`](../architecture/debugging.md) para el razonamiento completo de bisección.

## Checklist

- [ ] ¿Se filtra Logcat por tag/nivel relevante, no se lee el stream completo sin filtrar?
- [ ] ¿Se ha capturado una traza del Profiler en el momento exacto de un ANR antes de asumir la causa?
- [ ] ¿`StrictMode` está habilitado en builds de debug para detectar I/O accidental en el hilo principal?

Ver también: [`../architecture/debugging.md`](../architecture/debugging.md), [`performance.md`](./performance.md).
