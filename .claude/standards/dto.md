# Standard: DTO (Data Transfer Objects)

> Norma prescriptiva de diseño de DTOs. Ver [`../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md) para el razonamiento completo sobre por qué se mapea entre capas.

## Regla fundamental

**Nunca exponer una entidad de dominio ni una entidad de persistencia (JPA/TypeORM `@Entity`) directamente como request o response de la API.** Cada frontera de capa tiene su propio objeto de transferencia, con mapeo explícito (ver [`../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md)).

## Tipos de DTO

| DTO | Propósito |
|---|---|
| Request DTO | Datos de entrada de una petición (`CrearPedidoRequest`) — solo los campos que el cliente puede/debe enviar |
| Response DTO | Datos de salida de una respuesta (`PedidoResponse`) — solo los campos que el cliente debe ver, nunca campos internos (hash de contraseña, flags internos de auditoría) |
| Query DTO / Filter DTO | Parámetros de filtrado/paginación de una consulta (`BuscarPedidosQuery`) |
| Internal DTO | Transferencia entre capas internas (Application → Infrastructure) cuando no coincide 1:1 con el modelo de dominio ni con el de persistencia |

## Reglas

1. **Inmutabilidad por defecto**: DTOs como `record` (Java), `readonly` interfaces o clases con propiedades `readonly` (TypeScript), `data class` (Kotlin).
2. **Validación en el Request DTO** (ver [`validation.md`](./validation.md)), nunca en el Response DTO (no tiene sentido validar lo que el propio sistema genera).
3. **Un DTO por operación, no un DTO compartido "genérico"** entre `crear` y `actualizar` si los campos requeridos difieren — un campo opcional en `actualizar` pero requerido en `crear` señala que deberían ser DTOs distintos, no el mismo con validación condicional.
4. **Nombrar el DTO por su propósito, no por la entidad que envuelve**: `CrearPedidoRequest`, no `PedidoDTO` genérico que se reutiliza para todo.
5. **Mapeo explícito y automatizado**: MapStruct (Java), mappers manuales o `class-transformer` (Node) — nunca serialización directa de la entidad de dominio/persistencia con anotaciones de exclusión de campos como único mecanismo de control (`@JsonIgnore` disperso es frágil: un campo nuevo en la entidad se expone por defecto salvo que alguien recuerde excluirlo explícitamente).

## Ejemplo

```typescript
// Request DTO: solo lo que el cliente puede enviar, con validación
class CrearPedidoRequest {
  @IsUUID()
  clienteId: string;

  @IsArray()
  @ArrayMinSize(1)
  @ValidateNested({ each: true })
  @Type(() => LineaPedidoRequest)
  lineas: LineaPedidoRequest[];
}

// Response DTO: solo lo que el cliente debe ver
class PedidoResponse {
  id: string;
  estado: string;
  total: number;
  fechaCreacion: string;
  // nunca: clienteEmailInterno, flagsAuditoria, hashInterno, etc.
}

// Mapper explícito, único punto de traducción
class PedidoMapper {
  static aResponse(pedido: Pedido): PedidoResponse {
    return {
      id: pedido.id,
      estado: pedido.estado,
      total: pedido.calcularTotal(),
      fechaCreacion: pedido.fechaCreacion.toISOString(),
    };
  }
}
```

## Checklist

- [ ] ¿Ningún endpoint expone directamente una entidad de dominio o de persistencia?
- [ ] ¿El Request DTO valida solo los campos que el cliente controla?
- [ ] ¿El Response DTO excluye explícitamente cualquier campo interno/sensible?
- [ ] ¿Existe un mapper explícito y centralizado, no serialización automática sin control?
- [ ] ¿Se ha evitado un DTO "genérico" compartido entre operaciones con requisitos de campos distintos?

Ver también: [`validation.md`](./validation.md), [`api.md`](./api.md), [`../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md).
