# Hilt

> Ver [`../architecture/solid.md`](../architecture/solid.md) (DIP) para el principio que Hilt implementa, y [`../spring/spring-core.md`](../spring/spring-core.md)/[`../nestjs/architecture.md`](../nestjs/architecture.md) para el mismo concepto en otros dominios del framework.

## Módulos organizados por capa

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()

    @Provides
    fun providePedidoDao(database: AppDatabase): PedidoDao = database.pedidoDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds // para interfaces con una única implementación — más eficiente que @Provides
    abstract fun bindPedidoRepository(impl: PedidoRepositoryImpl): PedidoRepository
}
```

`@Binds` (en una clase `abstract`) para vincular una interfaz a su implementación concreta es preferible a `@Provides` cuando no hay lógica de construcción adicional — genera menos código y es más eficiente en tiempo de compilación.

## Inyección por constructor en ViewModels

```kotlin
@HiltViewModel
class PedidoViewModel @Inject constructor(
    private val obtenerPedido: ObtenerPedidoUseCase,
) : ViewModel()
```

```kotlin
@Composable
fun PantallaPedido(viewModel: PedidoViewModel = hiltViewModel()) { /* ... */ }
```

Mismo principio de inyección por constructor que en cualquier otro dominio del framework (ver [`../spring/spring-core.md`](../spring/spring-core.md)) — `@HiltViewModel` + `hiltViewModel()` conectan ese ViewModel con el grafo de dependencias de Hilt sin configuración manual adicional.

## Scopes: `@Singleton`, `@ViewModelScoped`, `@ActivityScoped`

```kotlin
@Module
@InstallIn(ViewModelComponent::class) // vive tanto como el ViewModel que lo usa, no toda la app
object UseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideObtenerPedidoUseCase(repository: PedidoRepository): ObtenerPedidoUseCase =
        ObtenerPedidoUseCase(repository)
}
```

Elegir el scope según el ciclo de vida real del objeto — `@Singleton` para lo que debe vivir toda la aplicación (base de datos, cliente HTTP), `@ViewModelScoped` para lo que debe vivir tanto como una pantalla concreta; un scope más amplio de lo necesario retiene el objeto en memoria más tiempo del necesario.

## Testing con módulos reemplazados

```kotlin
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
abstract class FakeRepositoryModule {
    @Binds
    abstract fun bindPedidoRepository(fake: PedidoRepositoryFake): PedidoRepository
}
```

`@TestInstallIn` reemplaza un módulo de producción por uno de test en instrumented tests — el mismo principio de Fake en memoria usado en el resto del framework para tests (ver [`../architecture/testing.md`](../architecture/testing.md)), aplicado a través del grafo de Hilt.

## Checklist

- [ ] ¿Los módulos están organizados por capa (`DatabaseModule`, `RepositoryModule`, `UseCaseModule`), no todo en un único módulo?
- [ ] ¿Se usa `@Binds` en lugar de `@Provides` cuando no hay lógica de construcción adicional?
- [ ] ¿El scope de cada binding corresponde al ciclo de vida real del objeto, no `@Singleton` por defecto sin pensar?

Ver también: [`../architecture/solid.md`](../architecture/solid.md), [`../android/architecture.md`](../android/architecture.md), [`../architecture/testing.md`](../architecture/testing.md).
