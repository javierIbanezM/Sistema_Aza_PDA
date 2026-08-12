# Retrofit

> Ver [`../android/architecture.md`](../android/architecture.md) para cómo Retrofit encaja como fuente de datos remota en el Repository, y [`../../standards/api.md`](../../standards/api.md) para las convenciones del contrato que este cliente consume.

## Definición del servicio

```kotlin
interface PedidoApi {
    @GET("api/v1/pedidos/{id}")
    suspend fun obtenerPedido(@Path("id") id: String): PedidoDto // suspend fun: integración nativa con coroutines

    @POST("api/v1/pedidos")
    suspend fun crearPedido(@Body request: CrearPedidoRequest): PedidoDto

    @GET("api/v1/pedidos")
    suspend fun listarPedidos(@Query("cursor") cursor: String?): PaginaPedidosDto
}
```

`suspend fun` en la interfaz del servicio integra Retrofit directamente con coroutines sin callbacks ni `Call<T>` manual — ver [`../kotlin/coroutines.md`](../kotlin/coroutines.md).

## Modelos de red separados del dominio

```kotlin
@Serializable
data class PedidoDto(val id: String, val estado: String, val total: Double)

fun PedidoDto.toDominio(): Pedido = Pedido(id = id, estado = EstadoPedido.valueOf(estado), total = total.toBigDecimal())
```

Nunca usar el DTO de red directamente como modelo de dominio — mapeo explícito, mismo principio que [`../../standards/dto.md`](../../standards/dto.md) aplica en el resto del framework.

## Interceptores: autenticación, logging, reintentos

```kotlin
val authInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer ${tokenProvider.obtenerToken()}")
        .build()
    chain.proceed(request)
}

val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    // NUNCA loggear el body completo en builds de release — puede contener datos sensibles, ver standards/logging-standard.md
}

val client = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(loggingInterceptor)
    .build()
```

## Manejo de errores HTTP

```kotlin
suspend fun obtenerPedido(id: String): Pedido {
    val response = pedidoApi.obtenerPedido(id) // suspend fun lanza HttpException ante 4xx/5xx
    return response.toDominio()
}

// Manejo centralizado en el Repository o un wrapper común
suspend fun <T> ejecutarLlamada(llamada: suspend () -> T): Resultado<T> {
    return try {
        Resultado.Exito(llamada())
    } catch (e: HttpException) {
        when (e.code()) {
            404 -> Resultado.Error(PedidoNoEncontradoException())
            else -> Resultado.Error(e)
        }
    } catch (e: IOException) {
        Resultado.Error(ErrorDeRed(e)) // fallo de red, distinto de un error HTTP válido
    }
}
```

A diferencia de `fetch` en JavaScript (ver [`../javascript/fetch.md`](../javascript/fetch.md)), una función `suspend` de Retrofit **sí lanza excepción** ante un código HTTP de error (`HttpException`) cuando se usa el tipo de retorno directo (no `Response<T>`) — comportamiento distinto entre ambos clientes HTTP del framework, importante no confundir al migrar conocimiento de uno a otro.

## Checklist

- [ ] ¿Los DTOs de red están separados del modelo de dominio, con mapeo explícito?
- [ ] ¿El logging de peticiones/respuestas está desactivado o restringido en builds de release?
- [ ] ¿El manejo de errores distingue `HttpException` (respuesta de error válida) de `IOException` (fallo de red)?

Ver también: [`../android/architecture.md`](../android/architecture.md), [`../../standards/dto.md`](../../standards/dto.md), [`../../standards/api.md`](../../standards/api.md).
