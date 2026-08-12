# Spring Cache

> Ver [`../../patterns/decorator.md`](../../patterns/decorator.md) para el patrón subyacente (Spring implementa caching mediante proxies/AOP, ver [`spring-aop.md`](./spring-aop.md)). Ver `CLAUDE.md` sección 6.5 para el patrón cache-aside general con Redis.

## Habilitar caching declarativo

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("productos", "categorias"); // en memoria, una única instancia
    }
}
```

Para despliegues con múltiples réplicas, usar un `CacheManager` respaldado por Redis (`RedisCacheManager`) en lugar de uno en memoria — de lo contrario cada réplica mantiene su propia caché desincronizada entre sí, sirviendo potencialmente datos distintos según a qué instancia responda cada petición.

## `@Cacheable`, `@CachePut`, `@CacheEvict`

```java
@Service
public class CatalogoService {

    @Cacheable(value = "productos", key = "#id")
    public Producto buscarProducto(String id) {
        return productoRepository.buscarPorId(id).orElseThrow(); // solo se ejecuta si no está en caché
    }

    @CachePut(value = "productos", key = "#producto.id")
    public Producto actualizarProducto(Producto producto) {
        return productoRepository.guardar(producto); // siempre se ejecuta, y actualiza la caché con el resultado
    }

    @CacheEvict(value = "productos", key = "#id")
    public void eliminarProducto(String id) {
        productoRepository.eliminar(id); // invalida explícitamente la entrada de caché
    }
}
```

## Cuándo aplicar caché (y cuándo no)

- **Aplicar** a datos de lectura frecuente y escritura poco frecuente (catálogos, configuración, datos de referencia) — ver [`../hibernate/hibernate.md`](../hibernate/hibernate.md) para el criterio equivalente en el segundo nivel de caché de Hibernate.
- **No aplicar por defecto** a entidades transaccionales con escritura constante (`Pedido`, `Carrito`) — la invalidación se vuelve más costosa y propensa a bugs de datos obsoletos que el beneficio que aporta.
- Nunca cachear datos sensibles a nivel de usuario individual sin incluir el identificador de usuario en la clave (`key = "#usuarioId + '-' + #id"`) — de lo contrario, un usuario podría recibir en caché una respuesta calculada para otro usuario.

## TTL explícito (no confiar en el tamaño de caché como único límite)

Spring Cache no gestiona TTL de forma nativa con `ConcurrentMapCacheManager` — para expiración por tiempo, usar Caffeine como backend:

```java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager("productos");
    manager.setCaffeine(Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES) // TTL explícito, ver CLAUDE.md sección 6.5
        .maximumSize(10_000));
    return manager;
}
```

Ver `CLAUDE.md` sección 6.5 — toda clave de caché debe tener TTL explícito, nunca una entrada "eterna" no gestionada.

## Errores comunes

- Usar `@Cacheable` sobre un método que tiene efectos secundarios además de calcular el valor a cachear — en la segunda llamada (servida desde caché), el efecto secundario no se ejecuta, produciendo comportamiento inconsistente entre la primera y las siguientes invocaciones.
- Cachear objetos mutables sin copia defensiva — si el llamador muta el objeto devuelto desde caché, corrompe silenciosamente la entrada cacheada para futuras lecturas.
- Olvidar `@CacheEvict` al eliminar/modificar la entidad por una vía distinta a los métodos anotados (ej. un `UPDATE` SQL directo) — la caché queda con datos obsoletos sin que nada la invalide.

## Checklist

- [ ] ¿El `CacheManager` es coherente entre réplicas si el despliegue tiene múltiples instancias (Redis, no en memoria local)?
- [ ] ¿Toda entrada de caché tiene TTL explícito?
- [ ] ¿Se cachean solo datos de lectura frecuente y escritura poco frecuente?
- [ ] ¿Las claves de caché de datos por usuario incluyen el identificador del usuario?
- [ ] ¿Existe `@CacheEvict` en toda operación que modifica el dato cacheado?

Ver también: [`spring-aop.md`](./spring-aop.md), [`../../patterns/decorator.md`](../../patterns/decorator.md), `CLAUDE.md` sección 6.5.
