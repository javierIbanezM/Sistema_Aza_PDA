# Recipe: Crear una Base de Datos Room (Android)

Aplica: [`../knowledge/room/room.md`](../knowledge/room/room.md), [`../knowledge/hilt/hilt.md`](../knowledge/hilt/hilt.md), [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md).

## Pasos

### 1. Diseñar primero el dominio (sin Room)

```kotlin
class Pedido(val id: String, val estado: EstadoPedido, private val lineas: MutableList<LineaPedido>) {
    fun confirmar() { /* invariantes de negocio */ }
}
```

### 2. Definir la entidad de persistencia

```kotlin
@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val id: String,
    val estado: String,
    val fechaCreacion: Long,
)

fun PedidoEntity.toDominio(): Pedido = Pedido(id, EstadoPedido.valueOf(estado), mutableListOf())
fun Pedido.toEntity(): PedidoEntity = PedidoEntity(id, estado.name, System.currentTimeMillis())
```

### 3. Definir el DAO

```kotlin
@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos WHERE id = :id")
    suspend fun obtenerPorId(id: String): PedidoEntity?

    @Query("SELECT * FROM pedidos ORDER BY fechaCreacion DESC")
    fun observarTodos(): Flow<List<PedidoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: PedidoEntity)

    @Delete
    suspend fun eliminar(pedido: PedidoEntity)
}
```

### 4. Registrar en la Database

```kotlin
@Database(entities = [PedidoEntity::class], version = 1, exportSchema = true) // exportSchema para versionar migraciones
abstract class AppDatabase : RoomDatabase() {
    abstract fun pedidoDao(): PedidoDao
}
```

### 5. Módulo Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
            .addMigrations(MIGRATION_1_2) // añadir a medida que el esquema evoluciona, nunca fallbackToDestructiveMigration
            .build()

    @Provides
    fun providePedidoDao(db: AppDatabase): PedidoDao = db.pedidoDao()
}
```

### 6. Implementar el Repository que usa el DAO

Ver [`java-create-repository.md`](./java-create-repository.md) para el equivalente Java del mismo patrón — este framework aplica el mismo Repository pattern (ver [`../patterns/repository.md`](../patterns/repository.md)) en cualquier dominio.

### 7. Test instrumentado con base de datos en memoria

```kotlin
@Test
fun deberiaGuardarYRecuperarUnPedido() = runTest {
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val dao = db.pedidoDao()

    dao.insertar(PedidoEntity("p-1", "BORRADOR", System.currentTimeMillis()))
    val recuperado = dao.obtenerPorId("p-1")

    assertThat(recuperado).isNotNull()
    db.close()
}
```

## Checklist final

- [ ] ¿Se diseñó primero el dominio, y la entidad Room se derivó de él?
- [ ] ¿Existe migración versionada para cualquier cambio de esquema futuro, con `exportSchema = true`?
- [ ] ¿El DAO devuelve `Flow<T>` cuando la UI necesita observar cambios automáticamente?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/android/room-dao.kt`](../snippets/android/room-dao.kt).
