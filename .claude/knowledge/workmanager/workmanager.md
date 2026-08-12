# WorkManager

> Ver [`../kotlin/coroutines.md`](../kotlin/coroutines.md) para las coroutines que `CoroutineWorker` usa internamente, y [`../spring/spring-core.md`](../spring/spring-core.md) / [`../../recipes/java-create-scheduler.md`](../../recipes/java-create-scheduler.md) para el concepto equivalente en el backend de este framework.

## `CoroutineWorker`: trabajo diferible garantizado

```kotlin
class SincronizarPedidosWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: PedidoRepository, // inyectado vía HiltWorkerFactory
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.sincronizarPendientes()
            Result.success()
        } catch (e: IOException) {
            Result.retry() // error transitorio (red) — WorkManager reintentará según la política de backoff
        } catch (e: Exception) {
            Result.failure() // error no recuperable — no tiene sentido reintentar
        }
    }
}
```

Distinguir `Result.retry()` (errores transitorios, ver [`../../standards/exceptions.md`](../../standards/exceptions.md)) de `Result.failure()` (errores permanentes) es la misma disciplina que en cualquier consumidor de cola del framework (ver [`../../recipes/java-create-worker.md`](../../recipes/java-create-worker.md)) — reintentar un error que no es transitorio solo desperdicia batería/recursos sin cambiar el resultado.

## Constraints: no ejecutar sin las condiciones adecuadas

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

val trabajoSincronizacion = PeriodicWorkRequestBuilder<SincronizarPedidosWorker>(15, TimeUnit.MINUTES)
    .setConstraints(constraints)
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sincronizar_pedidos",
    ExistingPeriodicWorkPolicy.KEEP, // si ya existe un trabajo con ese nombre, no duplicar
    trabajoSincronizacion,
)
```

`enqueueUniquePeriodicWork` con `ExistingPeriodicWorkPolicy.KEEP` evita duplicar el mismo trabajo periódico si la app se reinicia o el código que lo programa se ejecuta más de una vez — sin esto, cada reinicio de la app podría añadir un trabajo periódico adicional acumulativo.

## Cadenas de trabajo (`WorkContinuation`)

```kotlin
WorkManager.getInstance(context)
    .beginWith(descargarDatosWork)
    .then(procesarDatosWork) // solo se ejecuta si descargarDatosWork tiene éxito
    .then(notificarUsuarioWork)
    .enqueue()
```

## Observar el estado de un trabajo desde la UI

```kotlin
WorkManager.getInstance(context)
    .getWorkInfoByIdLiveData(workRequest.id)
    .observe(lifecycleOwner) { workInfo ->
        when (workInfo?.state) {
            WorkInfo.State.SUCCEEDED -> mostrarExito()
            WorkInfo.State.FAILED -> mostrarError()
            else -> {}
        }
    }
```

## Checklist

- [ ] ¿Se distingue `Result.retry()` (transitorio) de `Result.failure()` (permanente)?
- [ ] ¿Los trabajos periódicos usan `enqueueUniquePeriodicWork` para evitar duplicados?
- [ ] ¿Las constraints (red, batería) reflejan los requisitos reales del trabajo, no ejecutan sin condiciones cuando deberían?

Ver también: [`../kotlin/coroutines.md`](../kotlin/coroutines.md), [`../../standards/exceptions.md`](../../standards/exceptions.md), [`../../recipes/java-create-worker.md`](../../recipes/java-create-worker.md).
