# Fastify

> Ver [`../../standards/validation.md`](../../standards/validation.md) y [`../../standards/api.md`](../../standards/api.md) para las normas generales que Fastify implementa mediante JSON Schema nativo.

## JSON Schema como mecanismo central (validación + serialización)

```typescript
const crearPedidoSchema = {
  body: {
    type: 'object',
    required: ['clienteId', 'lineas'],
    properties: {
      clienteId: { type: 'string', format: 'uuid' },
      lineas: {
        type: 'array',
        minItems: 1,
        items: {
          type: 'object',
          required: ['productoId', 'cantidad'],
          properties: {
            productoId: { type: 'string', format: 'uuid' },
            cantidad: { type: 'integer', minimum: 1 },
          },
        },
      },
    },
  },
  response: {
    201: {
      type: 'object',
      properties: {
        id: { type: 'string' },
        estado: { type: 'string' },
        total: { type: 'number' },
      },
    },
  },
};

fastify.post('/api/v1/pedidos', { schema: crearPedidoSchema }, async (request, reply) => {
  const pedido = await crearPedidoUseCase.ejecutar(request.body.clienteId, request.body.lineas);
  reply.status(201).send(pedidoMapper.aResponse(pedido));
});
```

El schema cumple **dos funciones a la vez**: valida el `body` de entrada (rechazando peticiones que no cumplen antes de llegar al handler) y **serializa** la respuesta de salida de forma optimizada (Fastify compila el schema de respuesta a una función de serialización rápida, evitando `JSON.stringify` genérico) — es la razón principal del alto rendimiento de Fastify frente a Express en benchmarks.

## Plugins encapsulados

```typescript
import fp from 'fastify-plugin';

async function pedidosPlugin(fastify: FastifyInstance): Promise<void> {
  fastify.post('/pedidos', { schema: crearPedidoSchema }, crearPedidoHandler);
  fastify.get('/pedidos/:id', obtenerPedidoHandler);
}

export default fp(pedidosPlugin); // fastify-plugin evita el encapsulamiento por defecto, comparte el contexto con el padre
```

```typescript
fastify.register(pedidosPlugin, { prefix: '/api/v1' });
```

Por defecto, cada `register` crea un contexto de encapsulamiento propio (decoradores/hooks declarados dentro no se filtran hacia fuera) — `fastify-plugin` (`fp`) desactiva ese encapsulamiento específicamente cuando el plugin necesita exponer algo al contexto padre (ej. un decorador compartido por toda la aplicación).

## Hooks: lógica transversal por ciclo de vida

```typescript
fastify.addHook('onRequest', async (request, reply) => {
  request.traceId = request.headers['x-correlation-id'] || crypto.randomUUID();
});

fastify.addHook('onError', async (request, reply, error) => {
  logger.error('Error en petición', { traceId: request.traceId, error });
});
```

Equivalente conceptual a los middlewares de Express (ver [`../express/middleware.md`](../express/middleware.md)) o los interceptors de NestJS (ver [`../nestjs/pipes-guards-interceptors.md`](../nestjs/pipes-guards-interceptors.md)), con puntos de enganche más granulares (`onRequest`, `preValidation`, `preHandler`, `onSend`, `onError`...).

## Manejo de errores centralizado

```typescript
fastify.setErrorHandler((error, request, reply) => {
  if (error instanceof PedidoNoEncontradoException) {
    reply.status(404).send({ error: { code: error.code, message: error.message, details: [] } });
    return;
  }

  logger.error('Error no controlado', error);
  reply.status(500).send({ error: { code: 'INTERNAL_ERROR', message: 'Error inesperado', details: [] } });
});
```

Formato consistente con [`../../standards/api.md`](../../standards/api.md), igual que el `errorHandlerMiddleware` de Express o el Exception Filter de NestJS.

## Cuándo elegir Fastify sobre Express/NestJS

- Cargas de alto throughput donde el rendimiento de serialización/validación es un factor medible y relevante — el enfoque schema-first de Fastify supera a Express en benchmarks estándar.
- Equipos que prefieren JSON Schema como fuente de verdad única para validación y documentación (puede generar OpenAPI directamente desde los schemas, ver [`../architecture/openapi.md`](../architecture/openapi.md)).
- Proyectos que no necesitan la estructura opinionada de NestJS (módulos, DI compleja) y prefieren algo más cercano a Express pero con mejor rendimiento por defecto.

## Checklist

- [ ] ¿Cada ruta declara su `schema` de body/response, aprovechando validación + serialización optimizada?
- [ ] ¿Los plugins usan `fastify-plugin` únicamente cuando necesitan compartir contexto con el padre?
- [ ] ¿Existe un `setErrorHandler` global que mapea excepciones de dominio a códigos HTTP consistentes con `standards/api.md`?

Ver también: [`../../standards/validation.md`](../../standards/validation.md), [`../express/middleware.md`](../express/middleware.md), [`../architecture/openapi.md`](../architecture/openapi.md).
