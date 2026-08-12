// snippets/android/retrofit-service.kt
// Servicio Retrofit con interceptores y manejo de errores — ver knowledge/retrofit/retrofit.md.

interface PedidoApi {
    @GET("api/v1/pedidos/{id}")
    suspend fun obtenerPedido(@Path("id") id: String): PedidoDto

    @POST("api/v1/pedidos")
    suspend fun crearPedido(@Body request: CrearPedidoRequest): PedidoDto
}

@Serializable
data class PedidoDto(val id: String, val estado: String, val total: Double)

fun PedidoDto.toDominio(): Pedido = Pedido(id, EstadoPedido.valueOf(estado), mutableListOf())

class PedidoRepositoryImpl(
    private val dao: PedidoDao,
    private val api: PedidoApi,
) : PedidoRepository {

    override suspend fun obtenerPedido(id: String): Resultado<Pedido> {
        return try {
            val local = dao.obtenerPorId(id)
            if (local != null) return Resultado.Exito(local.toDominio())

            val remoto = api.obtenerPedido(id)
            dao.insertar(remoto.toDominio().toEntity())
            Resultado.Exito(remoto.toDominio())
        } catch (e: HttpException) {
            if (e.code() == 404) Resultado.Error(PedidoNoEncontradoException(id))
            else Resultado.Error(e)
        } catch (e: IOException) {
            Resultado.Error(ErrorDeRed(e))
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(authInterceptor).build()

    @Provides
    @Singleton
    fun providePedidoApi(client: OkHttpClient): PedidoApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PedidoApi::class.java)
}
