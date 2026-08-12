# Clean Architecture / Arquitectura Hexagonal

> Documento único de referencia. Los detalles de implementación específicos de cada framework (Spring, NestJS, Android) enlazan aquí en lugar de reexplicar las capas.

## Introducción

Clean Architecture (Robert C. Martin) y Arquitectura Hexagonal / Ports & Adapters (Alistair Cockburn) son formulaciones distintas de la misma idea central: **el dominio y la lógica de negocio no deben depender de frameworks, bases de datos ni interfaces de usuario**. En este framework se usan como sinónimos prácticos, con la siguiente organización en capas.

## Las capas

```
┌─────────────────────────────────────────────┐
│  Interface / Presentation                    │  Controladores REST, GraphQL resolvers,
│  (depende de Application)                    │  CLI, UI de Android
├─────────────────────────────────────────────┤
│  Infrastructure                              │  Repositorios JPA/TypeORM, clientes HTTP,
│  (implementa los puertos de Application)     │  colas de mensajería, adaptadores externos
├─────────────────────────────────────────────┤
│  Application                                 │  Casos de uso, orquestación,
│  (depende solo de Domain)                    │  define los puertos (interfaces)
├─────────────────────────────────────────────┤
│  Domain                                      │  Entidades, Value Objects, Agregados,
│  (no depende de nada externo)                │  reglas de negocio puras
└─────────────────────────────────────────────┘
```

### Regla de dependencia

> Las dependencias del código fuente solo pueden apuntar hacia adentro.

Nada en un círculo interior puede saber nada de un círculo exterior. En particular, el nombre de algo declarado en un círculo exterior no debe mencionarse en el código de un círculo interior — eso incluye funciones, clases, variables o cualquier otro elemento de software con nombre.

Esto significa, en la práctica:

- `Domain` no importa nada de Spring, Hibernate, Express, TypeORM, Android SDK, etc. Es Java/Kotlin/TypeScript puro.
- `Application` define **interfaces** (puertos) que necesita para funcionar (ej. `PedidoRepository`, `NotificadorEmail`), pero no las implementa.
- `Infrastructure` implementa esas interfaces usando la tecnología concreta (JPA, un cliente SMTP, un cliente HTTP a un servicio externo).
- `Interface/Presentation` traduce peticiones externas (HTTP, GraphQL, eventos) en llamadas a los casos de uso de `Application`.

### Puertos y Adaptadores

- **Puerto**: interfaz definida en `Application` (o en `Domain` si el concepto es puramente de dominio) que declara una capacidad que el sistema necesita, sin especificar cómo se implementa. Ejemplo: `interface PedidoRepository { guardar(pedido: Pedido): void }`.
- **Adaptador**: implementación concreta de un puerto, vive en `Infrastructure`. Ejemplo: `class PedidoRepositoryJpa implements PedidoRepository`.
- **Puerto de entrada (driving port)**: lo que expone el sistema hacia el exterior (un caso de uso invocado por un controlador).
- **Puerto de salida (driven port)**: lo que el sistema necesita del exterior (un repositorio, un cliente de email).

## Ejemplo completo — Caso de uso "Crear Pedido"

### Domain

```typescript
// domain/pedido.ts
export class Pedido {
  private constructor(
    public readonly id: string,
    private lineas: LineaPedido[],
    private estado: EstadoPedido
  ) {}

  static crear(id: string): Pedido {
    return new Pedido(id, [], EstadoPedido.BORRADOR);
  }

  agregarLinea(linea: LineaPedido): void {
    if (this.estado !== EstadoPedido.BORRADOR) {
      throw new Error('No se pueden agregar líneas a un pedido no editable');
    }
    this.lineas.push(linea);
  }

  confirmar(): void {
    if (this.lineas.length === 0) {
      throw new Error('Un pedido no puede confirmarse sin líneas');
    }
    this.estado = EstadoPedido.CONFIRMADO;
  }
}

export enum EstadoPedido { BORRADOR, CONFIRMADO }
export interface LineaPedido { productoId: string; cantidad: number; }
```

Sin ninguna dependencia externa. Se puede testear con `node domain.test.ts` sin levantar ningún framework.

### Application (define el puerto de salida y el caso de uso)

```typescript
// application/ports/pedido-repository.ts
import { Pedido } from '.../domain/pedido';

export interface PedidoRepository {
  guardar(pedido: Pedido): Promise<void>;
  buscarPorId(id: string): Promise<Pedido | null>;
}
```

```typescript
// application/use-cases/crear-pedido.ts
import { Pedido, LineaPedido } from '.../domain/pedido';
import { PedidoRepository } from '../ports/pedido-repository';

export class CrearPedidoUseCase {
  constructor(private readonly pedidoRepository: PedidoRepository) {} // DIP: depende de la abstracción

  async ejecutar(id: string, lineas: LineaPedido[]): Promise<void> {
    const pedido = Pedido.crear(id);
    lineas.forEach(linea => pedido.agregarLinea(linea));
    pedido.confirmar();
    await this.pedidoRepository.guardar(pedido);
  }
}
```

`CrearPedidoUseCase` no sabe si el pedido se guarda en PostgreSQL, MongoDB o en memoria. Solo conoce el contrato `PedidoRepository`.

### Infrastructure (adaptador concreto)

```typescript
// infrastructure/persistence/pedido-repository-typeorm.ts
import { PedidoRepository } from '.../application/ports/pedido-repository';
import { Pedido } from '.../domain/pedido';
import { PedidoEntity } from './pedido.entity';

export class PedidoRepositoryTypeOrm implements PedidoRepository {
  constructor(private readonly repo: Repository<PedidoEntity>) {}

  async guardar(pedido: Pedido): Promise<void> {
    const entity = PedidoMapper.aEntity(pedido); // mapeo explícito Dominio -> Entidad de persistencia
    await this.repo.save(entity);
  }

  async buscarPorId(id: string): Promise<Pedido | null> {
    const entity = await this.repo.findOneBy({ id });
    return entity ? PedidoMapper.aDominio(entity) : null;
  }
}
```

### Interface/Presentation (controlador)

```typescript
// interface/http/pedido.controller.ts
@Controller('pedidos')
export class PedidoController {
  constructor(private readonly crearPedidoUseCase: CrearPedidoUseCase) {}

  @Post()
  async crear(@Body() dto: CrearPedidoDto): Promise<void> {
    await this.crearPedidoUseCase.ejecutar(dto.id, dto.lineas); // DTO -> parámetros del caso de uso
  }
}
```

### Composición (Dependency Injection en el borde de la aplicación)

```typescript
// main.ts / módulo de configuración
const pedidoRepository: PedidoRepository = new PedidoRepositoryTypeOrm(typeormRepo);
const crearPedidoUseCase = new CrearPedidoUseCase(pedidoRepository);
```

La instanciación concreta ("¿qué implementación de `PedidoRepository` uso?") ocurre en un único lugar: el punto de composición de la aplicación (en NestJS, esto lo gestiona el contenedor de DI vía `@Module`; en Spring, `@Configuration`/component scanning).

## Mapeo entre capas

Nunca se debe exponer directamente:

- Una entidad de persistencia (JPA `@Entity`, TypeORM `@Entity`) como objeto de dominio.
- Un objeto de dominio directamente como respuesta HTTP.

Cada frontera de capa tiene su propio mapeo explícito:

```
DTO de entrada (HTTP) → parámetros del caso de uso
Dominio → Entidad de persistencia (al guardar)
Entidad de persistencia → Dominio (al leer)
Dominio → DTO de salida (HTTP)
```

Esto tiene un coste de código repetitivo (mappers), pero evita que un cambio de esquema de base de datos rompa el contrato de la API, o que un cambio de la API fuerce un cambio de esquema de base de datos. Herramientas como MapStruct (Java) automatizan este mapeo en tiempo de compilación sin eliminar la separación conceptual.

## Organización de carpetas recomendada

```
src/
├── domain/
│   ├── pedido/
│   │   ├── pedido.ts
│   │   ├── linea-pedido.ts
│   │   └── pedido.events.ts
│
├── application/
│   ├── ports/
│   │   ├── pedido-repository.ts
│   │   └── notificador-email.ts
│   └── use-cases/
│       ├── crear-pedido.ts
│       └── confirmar-pedido.ts
│
├── infrastructure/
│   ├── persistence/
│   │   ├── pedido-repository-typeorm.ts
│   │   └── pedido.entity.ts
│   └── email/
│       └── notificador-email-smtp.ts
│
└── interface/
    └── http/
        ├── pedido.controller.ts
        └── dto/
            └── crear-pedido.dto.ts
```

Organizar por **capa primero, feature después** (como arriba) funciona bien en servicios pequeños/medianos. En servicios grandes con muchos módulos de negocio, es habitual invertir el criterio (**feature primero, capa después**: `pedidos/domain`, `pedidos/application`...) para que cada módulo de negocio sea autocontenible. Ambas organizaciones respetan la misma regla de dependencia; la elección es de navegabilidad, no de arquitectura.

## Cuándo NO aplicar Clean Architecture completa

En scripts pequeños, prototipos, funciones serverless de un único propósito o CRUDs administrativos internos sin lógica de negocio relevante, imponer las cuatro capas completas es sobre-ingeniería (ver [`dry-kiss-yagni.md`](./dry-kiss-yagni.md)). La señal para introducir Clean Architecture es la presencia de **reglas de negocio no triviales** que se beneficiarán de aislarse de la infraestructura, no el tamaño del proyecto por sí solo.

## Checklist

- [ ] ¿El código de `domain/` compila y se puede testear sin ninguna dependencia de framework?
- [ ] ¿Los casos de uso dependen únicamente de interfaces (puertos), nunca de implementaciones concretas de infraestructura?
- [ ] ¿Existe mapeo explícito entre DTOs, entidades de dominio y entidades de persistencia, sin reutilizar la misma clase para las tres cosas?
- [ ] ¿La instanciación de adaptadores concretos ocurre en un único punto de composición?

Ver también: [`ddd.md`](./ddd.md), [`patterns/Repository.md`](../../patterns/repository.md), [`solid.md`](./solid.md) (principio DIP).
