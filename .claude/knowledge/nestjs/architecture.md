# Architecture (NestJS)

> Ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md) para el razonamiento de capas que NestJS facilita de forma más estructurada que Express gracias a su sistema de módulos nativo.

## Módulos: unidad de organización

```typescript
@Module({
  imports: [TypeOrmModule.forFeature([PedidoEntity])],
  controllers: [PedidoController],
  providers: [
    CrearPedidoUseCase,
    { provide: 'PedidoRepository', useClass: PedidoRepositoryTypeOrm }, // puerto -> adaptador
  ],
  exports: [CrearPedidoUseCase], // visible para otros módulos que importen este
})
export class PedidosModule {}
```

Un módulo por feature/Bounded Context (ver [`../architecture/ddd.md`](../architecture/ddd.md)), no un único `AppModule` monolítico con todo declarado — cada módulo encapsula sus propios controllers/providers y expone explícitamente (`exports`) solo lo que otros módulos necesitan consumir.

## Inyección de dependencias: tokens para interfaces

```typescript
// application/ports/pedido-repository.ts
export interface PedidoRepository {
  guardar(pedido: Pedido): Promise<void>;
  buscarPorId(id: string): Promise<Pedido | null>;
}
export const PEDIDO_REPOSITORY = Symbol('PedidoRepository'); // token de inyección para la interfaz
```

```typescript
@Injectable()
export class CrearPedidoUseCase {
  constructor(
    @Inject(PEDIDO_REPOSITORY) private readonly pedidoRepository: PedidoRepository
  ) {}
}
```

```typescript
@Module({
  providers: [
    { provide: PEDIDO_REPOSITORY, useClass: PedidoRepositoryTypeOrm },
  ],
})
export class PedidosModule {}
```

TypeScript no preserva interfaces en tiempo de ejecución (a diferencia de las clases) — inyectar una interfaz requiere un token explícito (`Symbol` o string), a diferencia de Spring donde la interfaz Java sí existe en runtime y puede inyectarse directamente (ver [`../spring/spring-core.md`](../spring/spring-core.md)).

## Controllers delgados, delegan en casos de uso

```typescript
@Controller('api/v1/pedidos')
export class PedidoController {
  constructor(private readonly crearPedidoUseCase: CrearPedidoUseCase) {}

  @Post()
  @UsePipes(new ValidationPipe({ whitelist: true, forbidNonWhitelisted: true }))
  async crear(@Body() dto: CrearPedidoDto): Promise<PedidoResponseDto> {
    const pedido = await this.crearPedidoUseCase.ejecutar(dto.clienteId, dto.lineas);
    return this.pedidoMapper.aResponse(pedido);
  }
}
```

`whitelist: true` descarta silenciosamente propiedades no declaradas en el DTO; `forbidNonWhitelisted: true` en su lugar **rechaza** la petición si llegan propiedades no esperadas — preferible en APIs que priorizan detectar errores de integración del cliente en lugar de ignorarlos silenciosamente.

## DTOs con `class-validator` + `class-transformer`

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
```

Ver [`validation.md`](./validation.md) para el detalle completo, y [`../../standards/dto.md`](../../standards/dto.md) para la norma general de DTOs.

## Ciclo de vida de providers: scope

```typescript
@Injectable({ scope: Scope.REQUEST }) // nueva instancia por cada petición HTTP
export class ContextoPeticionService {
  usuarioActual?: Usuario;
}

@Injectable() // por defecto: Singleton, una única instancia para toda la aplicación
export class CrearPedidoUseCase { /* ... */ }
```

`Scope.REQUEST` tiene coste de rendimiento (una instancia nueva por petición, en lugar de reutilizar una única instancia Singleton) — usar solo cuando genuinamente se necesita estado por petición, no por defecto.

## Módulos globales: usar con moderación

```typescript
@Global()
@Module({
  providers: [ConfigService],
  exports: [ConfigService],
})
export class ConfigModule {}
```

`@Global()` hace que el módulo esté disponible en toda la aplicación sin necesidad de importarlo explícitamente en cada módulo consumidor — apropiado para configuración/utilidades verdaderamente transversales (config, logging), no como atajo general para evitar declarar imports explícitos entre módulos de negocio.

## Checklist

- [ ] ¿Cada módulo corresponde a un feature/Bounded Context, no todo en un único módulo?
- [ ] ¿Las interfaces inyectadas usan un token (`Symbol`) explícito?
- [ ] ¿Los controllers delegan en casos de uso, sin lógica de negocio propia?
- [ ] ¿`Scope.REQUEST` se usa solo cuando es genuinamente necesario, no por defecto?

Ver también: [`validation.md`](./validation.md), [`pipes-guards-interceptors.md`](./pipes-guards-interceptors.md), [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md).
