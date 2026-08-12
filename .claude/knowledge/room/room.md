# Room

> Ver [`../android/architecture.md`](../android/architecture.md) para cómo Room encaja como fuente de datos local en el Repository, y [`../hibernate/jpa.md`](../hibernate/jpa.md) para el concepto equivalente de ORM en el ecosistema Java de este framework.

## Entity, DAO, Database

```kotlin
@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val id: String,
    val clienteId: String,
    val estado: String,
    val total: Double,
)

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos WHERE id = :id")
    suspend fun obtenerPorId(id: String): PedidoEntity?

    @Query("SELECT * FROM pedidos WHERE estado = :estado")
    fun observarPorEstado(estado: String): Flow<List<PedidoEntity>> // Flow: se actualiza automáticamente ante cambios

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: PedidoEntity)
}

@Database(entities = [PedidoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pedidoDao(): PedidoDao
}
```

Un DAO puede devolver `Flow<T>` en lugar de `T`/`List<T>` directamente — Room emite automáticamente un nuevo valor cada vez que los datos subyacentes cambian, sin necesidad de volver a consultar manualmente (ver [`../kotlin/flow.md`](../kotlin/flow.md)).

## Migraciones: nunca `fallbackToDestructiveMigration` en producción

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pedidos ADD COLUMN notas TEXT")
    }
}

Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
    .addMigrations(MIGRATION_1_2) // explícita, versionada — nunca fallbackToDestructiveMigration() en release
    .build()
```

`fallbackToDestructiveMigration()` borra y recrea la base de datos ante cualquier cambio de esquema no migrado explícitamente — aceptable únicamente en desarrollo temprano sin datos de usuario reales que preservar; en producción equivale a perder todos los datos locales del usuario ante una actualización de la app.

## Relaciones entre entidades

```kotlin
data class PedidoConLineas(
    @Embedded val pedido: PedidoEntity,
    @Relation(parentColumn = "id", entityColumn = "pedidoId")
    val lineas: List<LineaPedidoEntity>,
)

@Dao
interface PedidoDao {
    @Transaction // obligatorio: la consulta + las relaciones deben ejecutarse atómicamente
    @Query("SELECT * FROM pedidos WHERE id = :id")
    suspend fun obtenerConLineas(id: String): PedidoConLineas?
}
```

`@Transaction` es obligatorio en consultas que cargan relaciones — sin ella, Room ejecuta la consulta principal y la de la relación como operaciones separadas, con riesgo de inconsistencia si los datos cambian entre ambas.

## Consultas complejas: verificadas en tiempo de compilación

Room valida el SQL de cada `@Query` contra el esquema definido por las entidades **en tiempo de compilación** — un error de sintaxis SQL o una referencia a una columna inexistente falla el build, no en tiempo de ejecución como ocurriría con SQL construido dinámicamente sin este tipo de verificación.

## Checklist

- [ ] ¿Toda migración de esquema está explícita y versionada, sin `fallbackToDestructiveMigration` en release?
- [ ] ¿Las consultas que cargan relaciones (`@Relation`) están marcadas `@Transaction`?
- [ ] ¿Se usa `Flow<T>` en el DAO cuando la UI necesita observar cambios automáticamente, en lugar de re-consultar manualmente?

Ver también: [`../android/architecture.md`](../android/architecture.md), [`../kotlin/flow.md`](../kotlin/flow.md), [`../../patterns/repository.md`](../../patterns/repository.md).
