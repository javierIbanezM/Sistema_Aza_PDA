# TypeScript

> Ver [`modules.md`](./modules.md) para la configuración de módulos asociada. Este framework asume TypeScript por defecto en proyectos Node nuevos (ver `CLAUDE.md` sección 5.1).

## `strict: true`, sin excepciones

```json
{
  "compilerOptions": {
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "exactOptionalPropertyTypes": true
  }
}
```

`strict` habilita, entre otras, `strictNullChecks` (obliga a manejar explícitamente `null`/`undefined`) y `noImplicitAny` (prohíbe el tipo `any` implícito). `noUncheckedIndexedAccess` añade `undefined` al tipo de un acceso por índice (`array[i]`), reflejando la realidad de que el índice puede estar fuera de rango — sin esto, TypeScript asume incorrectamente que `array[i]` siempre existe.

## `any` prohibido salvo justificación explícita

```typescript
// Evitar
function procesar(datos: any) { /* ... */ }

// Preferir: tipo específico, o unknown + type guard si el tipo es genuinamente desconocido
function procesar(datos: unknown) {
  if (esRespuestaValida(datos)) {
    // datos ahora está tipado según el type guard
  }
}

function esRespuestaValida(datos: unknown): datos is RespuestaApi {
  return typeof datos === 'object' && datos !== null && 'id' in datos;
}
```

`unknown` obliga a verificar el tipo antes de operar sobre el valor (a diferencia de `any`, que desactiva la verificación de tipos por completo) — es el reemplazo type-safe de `any` cuando el tipo real no se conoce de antemano (respuesta de una API externa, `JSON.parse` sin schema).

## Tipos de utilidad esenciales

```typescript
interface Pedido {
  id: string;
  clienteId: string;
  estado: EstadoPedido;
  total: number;
}

type CrearPedidoInput = Omit<Pedido, 'id' | 'estado'>; // sin id ni estado, se asignan al crear
type ActualizarPedidoInput = Partial<Pick<Pedido, 'estado'>>; // solo estado, opcional
type PedidoReadonly = Readonly<Pedido>; // inmutable a nivel de tipo
```

`Omit`/`Pick`/`Partial`/`Readonly` derivan tipos nuevos a partir de uno existente sin duplicar la definición — cambios en `Pedido` se propagan automáticamente a los tipos derivados, evitando desincronización manual (aplicación de DRY, ver [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md), a las definiciones de tipo).

## Discriminated Unions (equivalente TypeScript a sealed classes de Java)

```typescript
type ResultadoPago =
  | { tipo: 'exitoso'; transaccionId: string; monto: number }
  | { tipo: 'rechazado'; motivo: string }
  | { tipo: 'pendiente'; referencia: string };

function manejarResultado(resultado: ResultadoPago): string {
  switch (resultado.tipo) {
    case 'exitoso': return `Pago ${resultado.transaccionId} exitoso`;
    case 'rechazado': return `Rechazado: ${resultado.motivo}`;
    case 'pendiente': return `Pendiente: ${resultado.referencia}`;
    // TypeScript verifica exhaustividad si se usa un "never" de control (ver abajo)
  }
}

function assertNunca(valor: never): never {
  throw new Error(`Caso no manejado: ${JSON.stringify(valor)}`);
}
```

Ver [`../java/sealed-classes.md`](../java/sealed-classes.md) para el equivalente conceptual en Java — el mismo principio de "conjunto cerrado de variantes verificado en compilación" aplicado con las herramientas de TypeScript.

## Genéricos con restricciones (equivalente a bounded types de Java)

```typescript
interface Repository<T extends { id: string }> {
  guardar(entidad: T): Promise<void>;
  buscarPorId(id: string): Promise<T | null>;
}
```

Ver [`../java/generics.md`](../java/generics.md) para el razonamiento equivalente sobre genéricos acotados.

## Branded types: primitivos con significado (Value Objects ligeros)

```typescript
type ClienteId = string & { readonly __brand: 'ClienteId' };

function crearClienteId(valor: string): ClienteId {
  if (!valor || valor.trim().length === 0) throw new Error('ClienteId no puede estar vacío');
  return valor as ClienteId;
}

function buscarCliente(id: ClienteId) { /* ... */ }

buscarCliente('cualquier-string' as any); // requiere cast explícito, TypeScript no confunde string suelto con ClienteId
buscarCliente(crearClienteId('c-123')); // correcto
```

Aproximación ligera al concepto de Value Object de DDD (ver [`../architecture/ddd.md`](../architecture/ddd.md)) cuando no se justifica una clase completa — el "branding" evita mezclar accidentalmente un `string` cualquiera con uno que representa específicamente un `ClienteId`, detectado en tiempo de compilación.

## Checklist

- [ ] ¿`strict: true` está habilitado, sin `any` implícito ni explícito sin justificar?
- [ ] ¿Se usan tipos de utilidad (`Omit`/`Pick`/`Partial`) en lugar de duplicar definiciones de tipo?
- [ ] ¿Los conjuntos cerrados de variantes usan discriminated unions con verificación de exhaustividad?
- [ ] ¿`unknown` se usa en lugar de `any` para datos externos de tipo no garantizado, con type guards explícitos?

Ver también: [`modules.md`](./modules.md), [`../java/sealed-classes.md`](../java/sealed-classes.md), [`../java/generics.md`](../java/generics.md), [`../architecture/ddd.md`](../architecture/ddd.md).
