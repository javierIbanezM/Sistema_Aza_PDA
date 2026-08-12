// snippets/android/room-dao.kt
// Entity + DAO + Database Room — ver recipes/android-create-room-database.md.

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val id: String,
    val estado: String,
    val fechaCreacion: Long,
)

fun PedidoEntity.toDominio(): Pedido = Pedido(id, EstadoPedido.valueOf(estado), mutableListOf())
fun Pedido.toEntity(): PedidoEntity = PedidoEntity(id, estado.name, System.currentTimeMillis())

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

@Database(entities = [PedidoEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pedidoDao(): PedidoDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()

    @Provides
    fun providePedidoDao(db: AppDatabase): PedidoDao = db.pedidoDao()
}
