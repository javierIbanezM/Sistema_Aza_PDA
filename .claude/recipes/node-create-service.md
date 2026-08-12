# Recipe: Crear un Service / Caso de Uso (Node/TypeScript)

Aplica: [`../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md), [`../knowledge/node/async-patterns.md`](../knowledge/node/async-patterns.md), [`../standards/exceptions.md`](../standards/exceptions.md).

## Pasos

### 1. Definir el puerto que el caso de uso necesita

```typescript
export interface PedidoRepository {
  guardar(pedido: Pedido): Promise<void>;
  buscarPorId(id: string): Promise<Pedido | null>;
}
```

### 2. Definir excepciones de dominio específicas

```typescript
export abstract class DomainException extends Error {
  constructor(message: string, public readonly code: string) {
    super(message);
    this.name = this.constructor.name;
  }
}

export class PedidoNoEncontradoException extends DomainException {
  constructor(id: string) {
    super(`Pedido no encontrado: ${id}`, 'PEDIDO_NOT_FOUND');
  }
}
```

### 3. Escribir el caso de uso: una única responsabilidad, un único método público

```typescript
@Injectable()
export class ConfirmarPedidoUseCase {
  constructor(
    @Inject(PEDIDO_REPOSITORY) private readonly pedidoRepository: PedidoRepository,
    private readonly eventBus: EventBus, // ver patterns/observer.md
  ) {}

  async ejecutar(id: string): Promise<void> {
    const pedido = await this.pedidoRepository.buscarPorId(id);
    if (!pedido) throw new PedidoNoEncontradoException(id);

    pedido.confirmar(); // la invariante vive en la entidad, no aquí

    await this.pedidoRepository.guardar(pedido);
    this.eventBus.publish(new PedidoConfirmadoEvent(pedido.id));
  }
}
```

### 4. Reaccionar al evento en otros casos de uso, en lugar de llamarlos directamente

```typescript
@Injectable()
export class NotificarClienteAlConfirmarHandler {
  constructor(private readonly notificadorEmail: NotificadorEmail) {}

  async handle(evento: PedidoConfirmadoEvent): Promise<void> {
    await this.notificadorEmail.notificarConfirmacion(evento.pedidoId);
  }
}
```

Este desacoplamiento evita que `ConfirmarPedidoUseCase` se convierta en un [God Object](../anti-patterns/god-object.md) a medida que se añaden más efectos secundarios (ver [`../patterns/observer.md`](../patterns/observer.md)).

### 5. Tests unitarios (sin framework, sin base de datos)

```typescript
describe('ConfirmarPedidoUseCase', () => {
  it('debe lanzar excepción si el pedido no existe', async () => {
    const pedidoRepository: jest.Mocked<PedidoRepository> = {
      guardar: jest.fn(),
      buscarPorId: jest.fn().mockResolvedValue(null),
    };
    const eventBus = { publish: jest.fn() };
    const useCase = new ConfirmarPedidoUseCase(pedidoRepository, eventBus as any);

    await expect(useCase.ejecutar('p-999')).rejects.toThrow(PedidoNoEncontradoException);
  });

  it('debe publicar el evento tras confirmar', async () => {
    const pedido = new PedidoTestBuilder().conLineas([unaLinea()]).build();
    const pedidoRepository: jest.Mocked<PedidoRepository> = {
      guardar: jest.fn(),
      buscarPorId: jest.fn().mockResolvedValue(pedido),
    };
    const eventBus = { publish: jest.fn() };
    const useCase = new ConfirmarPedidoUseCase(pedidoRepository, eventBus as any);

    await useCase.ejecutar(pedido.id);

    expect(eventBus.publish).toHaveBeenCalledWith(expect.any(PedidoConfirmadoEvent));
  });
});
```

## Checklist final

- [ ] ¿El caso de uso tiene un único método público con un único propósito?
- [ ] ¿Las invariantes de negocio viven en el dominio, no en el caso de uso?
- [ ] ¿Los efectos secundarios están desacoplados vía eventos, no encadenados directamente?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`node-create-rest-endpoint.md`](./node-create-rest-endpoint.md), [`../knowledge/node/async-patterns.md`](../knowledge/node/async-patterns.md).
