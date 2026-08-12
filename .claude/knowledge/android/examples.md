# Examples (Android)

## Ejemplo — Pantalla completa: ViewModel + Compose + Room + Retrofit

**Contexto**: pantalla de detalle de pedido, con caché local y actualización remota.

**Flujo aplicado**:

1. Dominio (`domain/`): `Pedido` con comportamiento propio — ver [`architecture.md`](./architecture.md) y [`../architecture/ddd.md`](../architecture/ddd.md).
2. Repository con estrategia cache-aside: Room como caché local, Retrofit como fuente remota — ver [`../room/room.md`](../room/room.md), [`../retrofit/retrofit.md`](../retrofit/retrofit.md), [`../../patterns/repository.md`](../../patterns/repository.md).
3. Use Case (`ObtenerPedidoUseCase`) inyectado vía Hilt — ver [`../hilt/hilt.md`](../hilt/hilt.md).
4. ViewModel con `StateFlow<PedidoUiState>` — ver [`../kotlin/flow.md`](../kotlin/flow.md), [`../kotlin/examples.md`](../kotlin/examples.md) para el ViewModel completo.
5. Composable que recolecta el estado con `collectAsStateWithLifecycle` y renderiza según el estado (Cargando/Exito/Error) — ver [`../compose/compose.md`](../compose/compose.md), [`../material3/material3.md`](../material3/material3.md) para los componentes visuales.
6. Tests: ViewModel testeado con Fake del Repository, sin Android SDK ni Compose (test unitario puro de JVM); UI testeada con Compose Testing.

**Recipe asociada**: [`../../recipes/android-create-compose-screen.md`](../../recipes/android-create-compose-screen.md).

---

## Ejemplo — Sincronización periódica en segundo plano

**Contexto**: sincronizar pedidos pendientes con el servidor cada cierto tiempo, incluso con la app cerrada.

**Flujo aplicado**:

1. `CoroutineWorker` que ejecuta la sincronización — ver [`../workmanager/workmanager.md`](../workmanager/workmanager.md).
2. Constraints de red (`NetworkType.CONNECTED`) para no ejecutar sin conectividad.
3. El Worker usa el mismo `PedidoRepository` que la UI, reutilizando toda la lógica de sincronización ya testeada — no duplica la lógica de acceso a datos.
4. Notificación local (ver [`../firebase/firebase.md`](../firebase/firebase.md) si se usa FCM para push remoto, o `NotificationManager` nativo si es puramente local) al completar la sincronización con cambios relevantes.
