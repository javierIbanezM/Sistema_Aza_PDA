# Patrón: Repository

## Descripción

El patrón Repository da la ilusión de una colección en memoria de objetos de dominio, ocultando los detalles concretos de cómo se persisten (SQL, ORM, API externa, archivo). El código cliente (casos de uso de la capa de aplicación) interactúa con una interfaz (`PedidoRepository.guardar()`, `.buscarPorId()`) sin conocer si detrás hay PostgreSQL, MongoDB o un mock en memoria.

En DDD (ver [`../ddd.md`](../knowledge/architecture/ddd.md)), existe un Repository por cada Agregado, nunca por tabla — si un agregado se compone de varias tablas relacionadas, el Repository de ese agregado es responsable de orquestar la persistencia de todas ellas de forma consistente.

## Cuándo usarlo

- Cuando la lógica de negocio necesita persistir/recuperar entidades de dominio y se quiere mantener esa lógica independiente de la tecnología de persistencia concreta (ver [`../clean-architecture.md`](../knowledge/architecture/clean-architecture.md)).
- Cuando se necesita poder testear casos de uso sin una base de datos real, sustituyendo el Repository por una implementación en memoria (Fake, ver [`../testing.md`](../knowledge/architecture/testing.md)).
- Cuando existe la posibilidad real (no especulativa, ver YAGNI en [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)) de cambiar de tecnología de persistencia en el futuro.

## Cuándo NO usarlo

- En scripts o utilidades pequeñas de un único uso, donde acceder directamente al ORM/driver es más simple y no hay lógica de negocio que aislar.
- Como capa "por si acaso" sobre un ORM que ya provee una abstracción suficiente (ej. Spring Data JPA ya abstrae buena parte del acceso a datos) — en ese caso, el Repository de dominio puede ser una envoltura fina sobre el repositorio de Spring Data, no una reimplementación completa.
- Cuando se necesita realizar consultas de solo lectura complejas y muy específicas para un caso de uso puntual (reportes, dashboards) — ahí es preferible un patrón de *Query Object* o *CQRS de lectura* dedicado, en lugar de forzar el modelo de "colección de agregados" del Repository sobre una necesidad de lectura que no encaja con él.

## Ventajas

- Aísla la lógica de negocio de los detalles de infraestructura (cumple DIP, ver [`../solid.md`](../knowledge/architecture/solid.md)).
- Facilita el testing de casos de uso con implementaciones en memoria, sin necesidad de una base de datos real en cada test unitario.
- Centraliza la lógica de acceso a datos de un agregado en un único lugar, evitando queries dispersas por todo el código.

## Desventajas

- Añade una capa de indirección y código de mapeo (entidad de dominio ↔ entidad de persistencia) que tiene coste de mantenimiento.
- Mal aplicado (un Repository por tabla en lugar de por agregado, o exponiendo métodos de consulta genéricos tipo `findAll()`/`findByAnyField()`), puede degenerar en una capa de acceso a datos anémica que no aporta el valor real del patrón.
- Puede ocultar problemas de rendimiento si el código cliente no es consciente de qué operación de persistencia real dispara cada llamada (ej. un `guardar()` que internamente hace N queries).

## Ejemplo

```typescript
// Puerto (interfaz), definido en la capa de Application
export interface PedidoRepository {
  guardar(pedido: Pedido): Promise<void>;
  buscarPorId(id: string): Promise<Pedido | null>;
  buscarPendientesDeEnvio(): Promise<Pedido[]>;
}

// Adaptador concreto, en Infrastructure
export class PedidoRepositoryTypeOrm implements PedidoRepository {
  constructor(private readonly ormRepo: Repository<PedidoEntity>) {}

  async guardar(pedido: Pedido): Promise<void> {
    const entity = PedidoMapper.aEntity(pedido);
    await this.ormRepo.save(entity);
  }

  async buscarPorId(id: string): Promise<Pedido | null> {
    const entity = await this.ormRepo.findOneBy({ id });
    return entity ? PedidoMapper.aDominio(entity) : null;
  }

  async buscarPendientesDeEnvio(): Promise<Pedido[]> {
    const entities = await this.ormRepo.findBy({ estado: 'CONFIRMADO', enviado: false });
    return entities.map(PedidoMapper.aDominio);
  }
}

// Fake para tests, en memoria, sin dependencias externas
export class PedidoRepositoryEnMemoria implements PedidoRepository {
  private pedidos = new Map<string, Pedido>();

  async guardar(pedido: Pedido): Promise<void> {
    this.pedidos.set(pedido.id, pedido);
  }

  async buscarPorId(id: string): Promise<Pedido | null> {
    return this.pedidos.get(id) ?? null;
  }

  async buscarPendientesDeEnvio(): Promise<Pedido[]> {
    return [...this.pedidos.values()].filter(p => p.estaConfirmadoYSinEnviar());
  }
}
```

## Errores comunes

- **Repository genérico (`GenericRepository<T>`) con métodos tipo `findAll()`/`update(Object)`**: reintroduce acoplamiento a la estructura de la tabla y pierde el propósito del patrón (expresar operaciones significativas del dominio, no CRUD genérico).
- **Exponer el `QueryBuilder`/`Specification` del ORM directamente en la interfaz del Repository**: filtra detalles de infraestructura hacia la capa de aplicación, violando la regla de dependencia de Clean Architecture.
- **Un Repository por tabla en un agregado compuesto por varias tablas**: rompe la unidad de consistencia transaccional del agregado; el Repository debe operar al nivel del agregado completo.

Ver también: [`../ddd.md`](../knowledge/architecture/ddd.md), [`../clean-architecture.md`](../knowledge/architecture/clean-architecture.md), [`../anti-patterns/anemic-domain-model.md`](../anti-patterns/anemic-domain-model.md).
