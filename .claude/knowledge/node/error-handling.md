# Error Handling (Node.js)

> Ver [`../../standards/exceptions.md`](../../standards/exceptions.md) para la norma prescriptiva transversal. Este documento cubre la mecánica específica de JavaScript/TypeScript en Node.js.

## Jerarquía de errores de dominio

```typescript
export abstract class DomainException extends Error {
  constructor(message: string, public readonly code: string) {
    super(message);
    this.name = this.constructor.name;
    Error.captureStackTrace(this, this.constructor); // excluye el propio constructor del stack trace
  }
}

export class PedidoNoEncontradoException extends DomainException {
  constructor(pedidoId: string) {
    super(`Pedido no encontrado: ${pedidoId}`, 'PEDIDO_NOT_FOUND');
  }
}
```

`Error.captureStackTrace` es específico de V8 (Node.js) — mejora la legibilidad del stack trace excluyendo los frames internos del propio constructor de la excepción.

## Preservar la causa raíz (`Error.cause`, Node.js 16.9+)

```typescript
try {
  await procesarPago(pedido);
} catch (error) {
  throw new PagoFallidoException('No se pudo procesar el pago', { cause: error });
}
```

```typescript
// Al loguear, incluir la cadena completa de causas
function logearErrorCompleto(error: Error): void {
  logger.error(error.message, { stack: error.stack, cause: (error as any).cause });
}
```

Ver [`../../standards/exceptions.md`](../../standards/exceptions.md) — nunca perder la excepción original al envolverla en una de dominio.

## Manejo centralizado de errores no capturados

```typescript
process.on('uncaughtException', (error) => {
  logger.error('Excepción no capturada, terminando proceso', error);
  process.exit(1); // terminar el proceso: el estado tras una excepción no capturada es indeterminado
});

process.on('unhandledRejection', (reason) => {
  logger.error('Promise rechazada sin manejar', reason);
  process.exit(1);
});
```

**No usar `uncaughtException` para "recuperarse" y continuar** — tras una excepción verdaderamente no capturada, el estado interno de la aplicación puede ser inconsistente (recursos parcialmente liberados, estado a medio actualizar). La práctica correcta es loguear con el máximo contexto posible y terminar el proceso de forma controlada, dejando que el orquestador (Kubernetes, PM2) reinicie una instancia limpia.

## Middleware de manejo de errores (Express) / Exception Filter (NestJS)

Ver [`../express/middleware.md`](../express/middleware.md) y [`../nestjs/architecture.md`](../nestjs/architecture.md) para el manejo centralizado específico de cada framework — este documento cubre solo la mecánica de errores del lenguaje/runtime, no la integración HTTP.

## Errores async vs. errores síncronos en Express (antes de Express 5)

```typescript
// En Express 4, un error lanzado DENTRO de un handler async NO se propaga automáticamente
// al middleware de errores sin este wrapper (corregido de forma nativa en Express 5)
function asyncHandler(fn: RequestHandler): RequestHandler {
  return (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
}

app.get('/pedidos/:id', asyncHandler(async (req, res) => {
  const pedido = await buscarPedidoUseCase.ejecutar(req.params.id); // si lanza, el wrapper lo captura y llama a next(error)
  res.json(pedido);
}));
```

Verificar la versión de Express del proyecto — Express 5+ maneja esto de forma nativa sin necesidad del wrapper.

## Validación de tipo de error antes de actuar

```typescript
function esErrorDeRed(error: unknown): error is NodeJS.ErrnoException {
  return error instanceof Error && 'code' in error;
}

try {
  await llamarServicioExterno();
} catch (error) {
  if (esErrorDeRed(error) && error.code === 'ECONNREFUSED') {
    // manejo específico de conexión rechazada
  }
  throw error;
}
```

`unknown` en el `catch` (comportamiento por defecto en TypeScript moderno con `useUnknownInCatchVariables`) obliga a verificar el tipo antes de acceder a propiedades específicas del error — más seguro que asumir `any` implícitamente.

## Checklist

- [ ] ¿Las excepciones de dominio extienden una base común con `code` explícito?
- [ ] ¿Se preserva la causa raíz con `{ cause: error }` al envolver excepciones?
- [ ] ¿Existe manejo de `uncaughtException`/`unhandledRejection` que termina el proceso de forma controlada, no que intenta "continuar"?
- [ ] ¿Los handlers async de Express están envueltos correctamente (si la versión lo requiere) para propagar errores al middleware de errores?

Ver también: [`../../standards/exceptions.md`](../../standards/exceptions.md), [`../express/middleware.md`](../express/middleware.md).
