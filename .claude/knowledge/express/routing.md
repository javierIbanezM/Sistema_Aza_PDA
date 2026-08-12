# Routing (Express)

> Ver [`../../standards/api.md`](../../standards/api.md) para las convenciones REST que este documento aplica en Express. Ver [`middleware.md`](./middleware.md) para el manejo de errores y lógica transversal.

## Organización de rutas por recurso, con Router

```typescript
// pedidos.routes.ts
import { Router } from 'express';

const router = Router();

router.post('/', asyncHandler(crearPedidoHandler));
router.get('/:id', asyncHandler(obtenerPedidoHandler));
router.get('/', asyncHandler(listarPedidosHandler));
router.patch('/:id/confirmar', asyncHandler(confirmarPedidoHandler));

export { router as pedidosRouter };
```

```typescript
// app.ts
app.use('/api/v1/pedidos', pedidosRouter);
```

Un `Router` por recurso (siguiendo la organización de capas de [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md), este archivo vive en `interface/http/`), montado con su prefijo de versión — nunca todas las rutas declaradas directamente sobre `app` en un único archivo creciente.

## Handlers delgados: delegan en casos de uso

```typescript
async function crearPedidoHandler(req: Request, res: Response): Promise<void> {
  const request = CrearPedidoRequestSchema.parse(req.body); // validación, ver ../../standards/validation.md
  const pedido = await crearPedidoUseCase.ejecutar(request.clienteId, request.lineas);
  res.status(201).json(pedidoMapper.aResponse(pedido));
}
```

El handler traduce HTTP ↔ caso de uso — nunca contiene lógica de negocio (ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md)), igual que un `@RestController` de Spring (ver [`../spring/spring-web.md`](../spring/spring-web.md)).

## Parámetros de ruta, query, body

```typescript
router.get('/:id', (req: Request<{ id: string }>, res) => {
  const { id } = req.params; // tipado si se especifica el genérico de Request
});

router.get('/', (req: Request<{}, {}, {}, { estado?: string; page?: string }>, res) => {
  const { estado, page } = req.query;
});
```

Tipar explícitamente `Request<Params, ResBody, ReqBody, ReqQuery>` en TypeScript en lugar de acceder a `req.params`/`req.query` sin tipo — evita errores de acceso a propiedades inexistentes detectables en compilación.

## Versionado en la URL

```typescript
app.use('/api/v1/pedidos', pedidosRouterV1);
app.use('/api/v2/pedidos', pedidosRouterV2); // coexisten durante la transición
```

Ver [`../../standards/api.md`](../../standards/api.md) — versión explícita en la URL para cambios que rompen compatibilidad.

## Paginación en endpoints de colección

```typescript
router.get('/', asyncHandler(async (req, res) => {
  const cursor = req.query.cursor as string | undefined;
  const limite = Math.min(parseInt(req.query.limit as string) || 20, 100); // límite máximo, nunca sin tope

  const { items, nextCursor } = await listarPedidosUseCase.ejecutar({ cursor, limite });
  res.json({ items: items.map(pedidoMapper.aResponse), nextCursor });
}));
```

Ver [`../../standards/api.md`](../../standards/api.md) — paginación obligatoria en colecciones, con límite máximo para evitar que un cliente solicite un `limit` desproporcionado.

## Checklist

- [ ] ¿Las rutas están organizadas por recurso con `Router`, no todas en un único archivo?
- [ ] ¿Los handlers delegan en casos de uso, sin lógica de negocio propia?
- [ ] ¿Los parámetros de ruta/query están tipados explícitamente?
- [ ] ¿Los endpoints de colección tienen paginación con límite máximo?

Ver también: [`middleware.md`](./middleware.md), [`../../standards/api.md`](../../standards/api.md).
