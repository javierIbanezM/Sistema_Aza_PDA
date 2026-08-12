# Middleware (Express)

> Ver [`routing.md`](./routing.md) para la organización de rutas, y [`../node/error-handling.md`](../node/error-handling.md) para la mecánica de errores del lenguaje que este middleware traduce a respuestas HTTP.

## Cadena de middlewares: orden importa

```typescript
app.use(helmet()); // cabeceras de seguridad, primero
app.use(cors(corsOptions));
app.use(express.json({ limit: '1mb' })); // parseo de body con límite explícito
app.use(correlationIdMiddleware); // ver ../../standards/logging-standard.md
app.use('/api/v1/pedidos', pedidosRouter);
app.use(errorHandlerMiddleware); // SIEMPRE al final
```

El middleware de manejo de errores debe ser el **último** registrado — Express lo identifica por tener 4 parámetros (`err, req, res, next`) y solo lo invoca cuando algún middleware/handler anterior llama a `next(error)` o lanza una excepción (en handlers async, ver el wrapper en [`../node/error-handling.md`](../node/error-handling.md)).

## Middleware de manejo global de errores

```typescript
function errorHandlerMiddleware(err: Error, req: Request, res: Response, next: NextFunction): void {
  if (err instanceof PedidoNoEncontradoException) {
    res.status(404).json({ error: { code: err.code, message: err.message, details: [] } });
    return;
  }

  if (err instanceof ValidationException) {
    res.status(400).json({ error: { code: 'VALIDATION_ERROR', message: err.message, details: err.detalles } });
    return;
  }

  logger.error('Error no controlado', err); // se loguea completo, nunca se expone al cliente
  res.status(500).json({ error: { code: 'INTERNAL_ERROR', message: 'Ha ocurrido un error inesperado', details: [] } });
}
```

Formato consistente con [`../../standards/api.md`](../../standards/api.md), mapeando cada tipo de excepción de dominio (ver [`../node/error-handling.md`](../node/error-handling.md)) a su código HTTP correspondiente — equivalente funcional al `@RestControllerAdvice` de Spring (ver [`../spring/spring-web.md`](../spring/spring-web.md)).

## Middleware de correlación de peticiones

```typescript
import { AsyncLocalStorage } from 'async_hooks';

const contextoPeticion = new AsyncLocalStorage<{ traceId: string }>();

function correlationIdMiddleware(req: Request, res: Response, next: NextFunction): void {
  const traceId = (req.headers['x-correlation-id'] as string) || crypto.randomUUID();
  res.setHeader('X-Correlation-Id', traceId);
  contextoPeticion.run({ traceId }, next);
}
```

Ver [`../node/debugging.md`](../node/debugging.md) para el uso completo de `AsyncLocalStorage`, y [`../../standards/logging-standard.md`](../../standards/logging-standard.md) para el razonamiento de correlación de peticiones.

## Middleware de autenticación

```typescript
function autenticarJwt(req: Request, res: Response, next: NextFunction): void {
  const header = req.headers.authorization;
  if (!header?.startsWith('Bearer ')) {
    res.status(401).json({ error: { code: 'UNAUTHORIZED', message: 'Token requerido', details: [] } });
    return;
  }

  try {
    const claims = jwtService.validarYExtraerClaims(header.substring(7));
    (req as AuthenticatedRequest).usuario = claims;
    next();
  } catch {
    res.status(401).json({ error: { code: 'INVALID_TOKEN', message: 'Token inválido o expirado', details: [] } });
  }
}
```

Aplicado selectivamente por ruta (`router.use(autenticarJwt)` dentro del router de un recurso protegido), no globalmente si existen rutas públicas (login, health check).

## Middleware de rate limiting

```typescript
import rateLimit from 'express-rate-limit';

const loginRateLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 5, // 5 intentos por ventana de 15 minutos
  message: { error: { code: 'TOO_MANY_ATTEMPTS', message: 'Demasiados intentos, inténtalo más tarde', details: [] } },
});

router.post('/login', loginRateLimiter, loginHandler);
```

Ver `CLAUDE.md` sección 12.1 (Identification and Authentication Failures) — mitigación de fuerza bruta en endpoints de autenticación.

## Checklist

- [ ] ¿El middleware de manejo de errores es el último registrado en la cadena?
- [ ] ¿Cada excepción de dominio se mapea explícitamente a su código HTTP, formato consistente con `standards/api.md`?
- [ ] ¿Existe propagación de `trace-id` mediante middleware, disponible en logs vía `AsyncLocalStorage`?
- [ ] ¿Los endpoints de autenticación tienen rate limiting?

Ver también: [`routing.md`](./routing.md), [`../node/error-handling.md`](../node/error-handling.md), [`../../standards/api.md`](../../standards/api.md).
