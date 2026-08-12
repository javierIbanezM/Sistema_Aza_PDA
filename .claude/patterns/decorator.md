# Pattern: Decorator

## Descripción

Decorator añade comportamiento adicional a un objeto de forma dinámica, envolviéndolo en una o más clases "decoradoras" que implementan la misma interfaz que el objeto original y delegan en él, añadiendo lógica antes/después de la delegación. Permite combinar responsabilidades de forma flexible sin recurrir a una explosión de subclases para cada combinación posible.

## Cuándo usarlo

- Cuando se necesita añadir responsabilidades transversales (logging, caché, medición de tiempo, reintentos, control de acceso) a una operación existente, sin modificar su implementación original ni crear una subclase por cada combinación.
- Cuando las responsabilidades a añadir son opcionales y combinables (ej. un repositorio con caché, con caché + logging, o solo logging), y crear una subclase por cada combinación sería inviable (herencia múltiple no soportada, o explosión combinatoria de clases).

## Cuándo NO usarlo

- Cuando el comportamiento adicional es fijo y único (no hay combinaciones) — añadirlo directamente en la implementación es más simple (ver KISS en [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).
- Cuando el framework ya provee un mecanismo declarativo equivalente y más idiomático (interceptores/AOP en Spring, middlewares en Express/NestJS) — preferir el mecanismo nativo del framework sobre implementar Decorator manualmente, salvo que el framework no cubra el caso.

## Ventajas

- Añade responsabilidades de forma dinámica y combinable, sin explosión de subclases.
- Respeta el principio Open/Closed (ver [`../knowledge/architecture/solid.md`](../knowledge/architecture/solid.md)): se añade comportamiento envolviendo, sin modificar la clase original.
- Cada decorador tiene una única responsabilidad clara (logging, caché, retry...), facilitando testear cada una de forma aislada.

## Desventajas

- Múltiples capas de decoradores anidados pueden dificultar seguir el flujo real de ejecución al depurar.
- Si se usa en exceso para responsabilidades que en realidad son fijas y no combinables, añade indirección innecesaria.

## Ejemplo

```typescript
interface PedidoRepository {
  buscarPorId(id: string): Promise<Pedido | null>;
}

// Implementación base
class PedidoRepositoryTypeOrm implements PedidoRepository {
  async buscarPorId(id: string): Promise<Pedido | null> {
    // consulta real a base de datos
    return this.orm.findOneBy({ id });
  }
}

// Decorador: añade caché sin modificar la implementación base
class CacheadoPedidoRepository implements PedidoRepository {
  constructor(private readonly interno: PedidoRepository, private readonly cache: Cache) {}

  async buscarPorId(id: string): Promise<Pedido | null> {
    const enCache = await this.cache.get(`pedido:${id}`);
    if (enCache) return enCache;

    const pedido = await this.interno.buscarPorId(id);
    if (pedido) await this.cache.set(`pedido:${id}`, pedido, { ttl: 60 });
    return pedido;
  }
}

// Decorador: añade logging, componible con el anterior
class LoggeadoPedidoRepository implements PedidoRepository {
  constructor(private readonly interno: PedidoRepository) {}

  async buscarPorId(id: string): Promise<Pedido | null> {
    logger.info(`Buscando pedido ${id}`);
    const resultado = await this.interno.buscarPorId(id);
    logger.info(`Pedido ${id} ${resultado ? 'encontrado' : 'no encontrado'}`);
    return resultado;
  }
}

// Composición: caché + logging, sin tocar la implementación base ni crear una subclase por combinación
const repository: PedidoRepository = new LoggeadoPedidoRepository(
  new CacheadoPedidoRepository(
    new PedidoRepositoryTypeOrm(orm),
    cache
  )
);
```

### Equivalente declarativo con AOP (Spring)

```java
@Cacheable("pedidos")
@Loggable
public Pedido buscarPorId(String id) {
    return repository.findById(id);
}
```

En Spring, el mismo resultado se logra habitualmente con anotaciones + AOP (`@Cacheable`, interceptores propios) en lugar de escribir manualmente las clases decoradoras — el patrón subyacente es el mismo, pero el framework provee el mecanismo de forma declarativa.

## Errores comunes

- Anidar decoradores en un orden que produce un comportamiento incorrecto (ej. cachear antes de aplicar control de acceso, exponiendo datos que deberían haberse filtrado por permisos) — el orden de composición importa y debe documentarse.
- Reimplementar manualmente lo que el framework ya ofrece de forma declarativa (interceptores, AOP, middlewares), añadiendo complejidad innecesaria.

Ver también: [`../knowledge/architecture/solid.md`](../knowledge/architecture/solid.md) (OCP), [`facade.md`](./facade.md), [`../patterns/strategy.md`](./strategy.md).
