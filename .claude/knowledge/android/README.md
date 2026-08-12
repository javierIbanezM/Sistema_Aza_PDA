# Knowledge — Android

Índice de la **plataforma Android**. El lenguaje Kotlin vive en [`../kotlin/`](../kotlin/), Jetpack Compose en [`../compose/`](../compose/), y cada librería del ecosistema (Room, Retrofit, Firebase, Hilt, WorkManager, Material 3) tiene su propia carpeta — no se duplican aquí.

| Documento | Descripción |
|---|---|
| [`architecture.md`](./architecture.md) | MVVM + Clean Architecture aplicado a Android: capas, ViewModel, Use Case, Repository |
| [`lifecycle.md`](./lifecycle.md) | Ciclo de vida de Activity/Fragment, ViewModel, scopes, `repeatOnLifecycle` |
| [`permissions.md`](./permissions.md) | Solicitud en tiempo de ejecución, menor privilegio, verificación continua |
| [`performance.md`](./performance.md) | Hilo principal, Baseline Profiles, R8, carga de imágenes |
| [`debugging.md`](./debugging.md) | Logcat, ANRs, Layout Inspector, `StrictMode` |
| [`best-practices.md`](./best-practices.md) | Síntesis de las reglas más importantes de la plataforma |
| [`pitfalls.md`](./pitfalls.md) | Errores frecuentes específicos de Android |
| [`examples.md`](./examples.md) | Casos de uso completos |

## Dominios relacionados (no duplicados aquí)

| Tecnología | Carpeta |
|---|---|
| Kotlin (lenguaje) | [`../kotlin/`](../kotlin/) |
| Jetpack Compose | [`../compose/`](../compose/) |
| Room | [`../room/`](../room/) |
| Retrofit | [`../retrofit/`](../retrofit/) |
| Firebase | [`../firebase/`](../firebase/) |
| Hilt | [`../hilt/`](../hilt/) |
| WorkManager | [`../workmanager/`](../workmanager/) |
| Material Design 3 | [`../material3/`](../material3/) |

Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí ni en el dominio específico correspondiente (regla de oro nº2).
