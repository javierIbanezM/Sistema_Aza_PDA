# Recipe: Crear un Endpoint REST (Node/NestJS)

Aplica: [`../knowledge/nestjs/architecture.md`](../knowledge/nestjs/architecture.md), [`../knowledge/nestjs/validation.md`](../knowledge/nestjs/validation.md), [`../standards/api.md`](../standards/api.md), [`../standards/dto.md`](../standards/dto.md).

## Pasos

### 1. Definir el DTO de entrada con validación

```typescript
export class CrearPedidoDto {
  @IsUUID()
  clienteId: string;

  @IsArray()
  @ArrayMinSize(1)
  @ValidateNested({ each: true })
  @Type(() => LineaPedidoDto)
  lineas: LineaPedidoDto[];
}

export class LineaPedidoDto {
  @IsUUID()
  productoId: string;

  @IsInt()
  @Min(1)
  cantidad: number;
}
```

### 2. Definir el DTO de salida (nunca la entidad de dominio directamente)

```typescript
export class PedidoResponseDto {
  id: string;
  estado: string;
  total: number;
  fechaCreacion: string;
}
```

### 3. Definir el puerto y el caso de uso (si no existen ya)

```typescript
export const PEDIDO_REPOSITORY = Symbol('PedidoRepository');

export interface PedidoRepository {
  guardar(pedido: Pedido): Promise<void>;
}

@Injectable()
export class CrearPedidoUseCase {
  constructor(@Inject(PEDIDO_REPOSITORY) private readonly pedidoRepository: PedidoRepository) {}

  async ejecutar(clienteId: string, lineas: LineaPedido[]): Promise<Pedido> {
    const pedido = Pedido.crear(clienteId);
    lineas.forEach(linea => pedido.agregarLinea(linea));
    pedido.confirmar();
    await this.pedidoRepository.guardar(pedido);
    return pedido;
  }
}
```

### 4. Definir el Mapper

```typescript
@Injectable()
export class PedidoMapper {
  aResponse(pedido: Pedido): PedidoResponseDto {
    return {
      id: pedido.id,
      estado: pedido.estado,
      total: pedido.calcularTotal(),
      fechaCreacion: pedido.fechaCreacion.toISOString(),
    };
  }

  aLineaPedido(dto: LineaPedidoDto): LineaPedido {
    return new LineaPedido(dto.productoId, dto.cantidad);
  }
}
```

### 5. Escribir el Controller

```typescript
@Controller('api/v1/pedidos')
export class PedidoController {
  constructor(
    private readonly crearPedidoUseCase: CrearPedidoUseCase,
    private readonly pedidoMapper: PedidoMapper,
  ) {}

  @Post()
  async crear(@Body() dto: CrearPedidoDto): Promise<PedidoResponseDto> {
    const lineas = dto.lineas.map(l => this.pedidoMapper.aLineaPedido(l));
    const pedido = await this.crearPedidoUseCase.ejecutar(dto.clienteId, lineas);
    return this.pedidoMapper.aResponse(pedido);
  }
}
```

### 6. Declarar el módulo

```typescript
@Module({
  controllers: [PedidoController],
  providers: [
    CrearPedidoUseCase,
    PedidoMapper,
    { provide: PEDIDO_REPOSITORY, useClass: PedidoRepositoryTypeOrm },
  ],
})
export class PedidosModule {}
```

### 7. Manejo de errores (si no existe ya un Exception Filter global)

Ver [`../knowledge/nestjs/pipes-guards-interceptors.md`](../knowledge/nestjs/pipes-guards-interceptors.md) — añadir el mapeo correspondiente si el caso de uso lanza una excepción de dominio nueva.

### 8. Tests

```typescript
describe('CrearPedidoUseCase', () => {
  let pedidoRepository: jest.Mocked<PedidoRepository>;
  let useCase: CrearPedidoUseCase;

  beforeEach(() => {
    pedidoRepository = { guardar: jest.fn() };
    useCase = new CrearPedidoUseCase(pedidoRepository);
  });

  it('debe crear y guardar el pedido con las líneas dadas', async () => {
    const pedido = await useCase.ejecutar('c-1', [new LineaPedido('p-1', 2)]);

    expect(pedidoRepository.guardar).toHaveBeenCalledWith(pedido);
  });
});
```

## Checklist final

Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md) completo antes de dar la tarea por terminada.

Ver también: [`node-create-service.md`](./node-create-service.md), [`../knowledge/express/routing.md`](../knowledge/express/routing.md) para el equivalente en Express puro.
