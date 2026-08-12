# Best Practices (Android — plataforma)

> Síntesis de la plataforma Android. Para el lenguaje Kotlin, ver [`../kotlin/best-practices.md`](../kotlin/best-practices.md); para Compose, [`../compose/`](../compose/) — no se duplican aquí.

## Arquitectura

- Clean Architecture / MVVM: `domain/` sin imports de `android.*` (ver [`architecture.md`](./architecture.md)).
- ViewModel expone solo `StateFlow` de solo lectura, nunca `MutableStateFlow` público.
- Repository como única fuente de verdad, decidiendo entre caché local (Room) y remoto (Retrofit).

## Ciclo de vida

- Estado que debe sobrevivir a rotación vive en el ViewModel, no en la Activity/Fragment (ver [`lifecycle.md`](./lifecycle.md)).
- `viewModelScope` vs. `lifecycleScope` elegido según si el trabajo debe sobrevivir a rotación.
- Recolección de `Flow` en la UI con `repeatOnLifecycle`/`collectAsStateWithLifecycle`.

## Permisos

- Solicitar en el momento en que se necesitan, con contexto — nunca todos al arrancar (ver [`permissions.md`](./permissions.md)).
- Verificar el permiso antes de cada uso, nunca asumir que sigue concedido.

## Performance

- Nunca trabajo bloqueante en el hilo principal (ver [`performance.md`](./performance.md)).
- Baseline Profiles + `minifyEnabled`/`shrinkResources` en release.
- Imágenes cargadas con tamaño objetivo explícito, nunca a resolución completa innecesaria.

Ver [`pitfalls.md`](./pitfalls.md) y [`examples.md`](./examples.md).
