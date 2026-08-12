# Validation (NestJS)

> Ver [`../../standards/validation.md`](../../standards/validation.md) para la norma general en dos capas. Ver [`../spring/spring-validation.md`](../spring/spring-validation.md) para el equivalente conceptual con Bean Validation en Spring.

## `class-validator` sobre DTOs

```typescript
export class CrearPedidoDto {
  @IsUUID('4', { message: 'clienteId debe ser un UUID válido' })
  clienteId: string;

  @IsArray()
  @ArrayMinSize(1, { message: 'El pedido debe tener al menos una línea' })
  @ValidateNested({ each: true })
  @Type(() => LineaPedidoDto)
  lineas: LineaPedidoDto[];

  @IsOptional()
  @IsString()
  @MaxLength(500)
  notas?: string;
}

export class LineaPedidoDto {
  @IsUUID()
  productoId: string;

  @IsInt()
  @Min(1)
  cantidad: number;
}
```

`@ValidateNested({ each: true })` + `@Type(() => LineaPedidoDto)` son ambos necesarios para validar en cascada un array de objetos anidados — `@Type` le indica a `class-transformer` a qué clase transformar cada elemento antes de que `class-validator` pueda aplicar las validaciones de esa clase.

## `ValidationPipe` global

```typescript
app.useGlobalPipes(new ValidationPipe({
  whitelist: true,           // descarta propiedades no declaradas en el DTO
  forbidNonWhitelisted: true, // rechaza la petición si llegan propiedades no esperadas
  transform: true,            // convierte automáticamente tipos primitivos (string de query -> number)
  transformOptions: { enableImplicitConversion: true },
}));
```

Ver [`../../standards/api.md`](../../standards/api.md) para el formato de error estándar — NestJS lanza `BadRequestException` automáticamente ante fallo de validación; el filtro global (ver [`pipes-guards-interceptors.md`](./pipes-guards-interceptors.md)) debe mapear su estructura de errores al formato consistente del framework.

## Grupos de validación

```typescript
export class PedidoDto {
  @IsEmpty({ groups: ['crear'] })
  @IsUUID(undefined, { groups: ['actualizar'] })
  id?: string;
}
```

```typescript
@Post()
crear(@Body(new ValidationPipe({ groups: ['crear'] })) dto: PedidoDto) { }
```

Ver [`../spring/spring-validation.md`](../spring/spring-validation.md) — el mismo criterio aplica: preferir DTOs separados por operación salvo que ambos compartan la inmensa mayoría de campos y reglas.

## Validadores personalizados

```typescript
@ValidatorConstraint({ name: 'codigoPostalValido', async: false })
export class CodigoPostalValidoConstraint implements ValidatorConstraintInterface {
  validate(valor: string): boolean {
    return /^\d{5}$/.test(valor);
  }
  defaultMessage(): string {
    return 'Código postal no válido';
  }
}

export function CodigoPostalValido(options?: ValidationOptions) {
  return function (object: object, propertyName: string) {
    registerDecorator({
      target: object.constructor,
      propertyName,
      options,
      constraints: [],
      validator: CodigoPostalValidoConstraint,
    });
  };
}
```

## Validación asíncrona (verificación contra base de datos)

```typescript
@ValidatorConstraint({ name: 'clienteExiste', async: true })
export class ClienteExisteConstraint implements ValidatorConstraintInterface {
  constructor(private readonly clienteRepository: ClienteRepository) {}

  async validate(clienteId: string): Promise<boolean> {
    return (await this.clienteRepository.buscarPorId(clienteId)) !== null;
  }
}
```

Usar con moderación — validar existencia contra base de datos en el DTO acopla la validación de forma a una consulta de infraestructura; considerar si esta verificación pertenece mejor al caso de uso (que ya va a buscar el cliente de todas formas) en lugar de duplicarla en el pipe de validación.

## Checklist

- [ ] ¿Los DTOs anidados usan `@ValidateNested` + `@Type` juntos?
- [ ] ¿El `ValidationPipe` global usa `whitelist`/`forbidNonWhitelisted`?
- [ ] ¿Se ha evitado duplicar una verificación (ej. existencia de un recurso) tanto en un validador async como en el caso de uso?

Ver también: [`../../standards/validation.md`](../../standards/validation.md), [`architecture.md`](./architecture.md), [`pipes-guards-interceptors.md`](./pipes-guards-interceptors.md).
