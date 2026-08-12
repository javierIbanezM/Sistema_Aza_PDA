# Recipe: Crear un Middleware (Node/Express)

Aplica: [`../knowledge/express/middleware.md`](../knowledge/express/middleware.md), [`../standards/logging-standard.md`](../standards/logging-standard.md).

## Pasos

### 1. Definir el propósito exacto (lógica genuinamente transversal)

Antes de escribir un middleware, confirmar que la lógica es transversal a múltiples rutas — si solo aplica a un único endpoint, probablemente pertenece dentro del handler, no como middleware separado (ver KISS en [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).

### 2. Escribir el middleware, tipado explícitamente

```typescript
import { Request, Response, NextFunction } from 'express';

export function correlationIdMiddleware(req: Request, res: Response, next: NextFunction): void {
  const traceId = (req.headers['x-correlation-id'] as string) || crypto.randomUUID();
  (req as any).traceId = traceId;
  res.setHeader('X-Correlation-Id', traceId);
  next(); // OBLIGATORIO: sin next(), la cadena se detiene silenciosamente
}
```

**Nunca olvidar `next()`** (o `res.send()`/`res.json()` si el middleware finaliza la respuesta) — un middleware que no llama a ninguno de los dos deja la petición colgada indefinidamente sin ningún error visible.

### 3. Middleware asíncrono: manejo de errores explícito

```typescript
export function autenticarJwt(req: Request, res: Response, next: NextFunction): void {
  const header = req.headers.authorization;
  if (!header?.startsWith('Bearer ')) {
    res.status(401).json({ error: { code: 'UNAUTHORIZED', message: 'Token requerido', details: [] } });
    return; // finaliza aquí, nunca llama a next() con la petición ya respondida
  }

  jwtService.validarYExtraerClaims(header.substring(7))
    .then(claims => {
      (req as AuthenticatedRequest).usuario = claims;
      next();
    })
    .catch(() => {
      res.status(401).json({ error: { code: 'INVALID_TOKEN', message: 'Token inválido', details: [] } });
    });
}
```

Si el middleware realiza trabajo asíncrono, capturar explícitamente cualquier rechazo — un middleware async sin `.catch()` (o sin `try/catch` si se usa `async function`) puede producir un unhandled rejection en lugar de una respuesta de error controlada (ver [`../knowledge/node/error-handling.md`](../knowledge/node/error-handling.md)).

### 4. Aplicar el middleware al alcance correcto

```typescript
// Global: aplica a TODAS las rutas
app.use(correlationIdMiddleware);

// Solo a un router/recurso concreto
router.use(autenticarJwt);

// Solo a un endpoint específico
router.post('/pedidos', autenticarJwt, validarBody(crearPedidoSchema), crearPedidoHandler);
```

No aplicar globalmente un middleware que solo debería afectar a un subconjunto de rutas (ej. autenticación en un endpoint público de health check).

### 5. Test del middleware

```typescript
describe('correlationIdMiddleware', () => {
  it('debe generar un traceId si no viene en la petición', () => {
    const req = { headers: {} } as Request;
    const res = { setHeader: jest.fn() } as unknown as Response;
    const next = jest.fn();

    correlationIdMiddleware(req, res, next);

    expect(res.setHeader).toHaveBeenCalledWith('X-Correlation-Id', expect.any(String));
    expect(next).toHaveBeenCalled();
  });
});
```

## Checklist final

- [ ] ¿El middleware llama siempre a `next()` o finaliza la respuesta, en todos los caminos posibles?
- [ ] ¿El trabajo asíncrono dentro del middleware tiene manejo de errores explícito?
- [ ] ¿El middleware se aplica al alcance correcto (global/router/endpoint), no más amplio de lo necesario?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../knowledge/express/middleware.md`](../knowledge/express/middleware.md), [`../knowledge/node/error-handling.md`](../knowledge/node/error-handling.md).
