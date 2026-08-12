# API Design (para integración entre sistemas)

> Ver [`../../standards/api.md`](../../standards/api.md) para las convenciones REST del framework y [`../architecture/openapi.md`](../architecture/openapi.md) para documentación de contrato — este documento cubre consideraciones específicas de APIs consumidas por **sistemas externos** (ERPs, WMS, CRMs), no solo por un frontend propio.

## Diseñar para el consumidor menos flexible

Una API consumida por un frontend propio puede evolucionar en conjunto con sus clientes; una API consumida por un ERP/WMS de terceros con su propio ciclo de actualización (a veces años) debe asumir que el consumidor **no podrá adaptarse rápido** a un cambio de contrato — esto empuja a un versionado más conservador y a compatibilidad hacia atrás más estricta de lo habitual (ver [`api-versioning.md`](./api-versioning.md)).

## Contratos explícitos, no implícitos

```json
{
  "pedidoId": "p-123",
  "estado": "CONFIRMADO",
  "lineas": [{ "productoId": "sku-1", "cantidad": 2 }]
}
```

Documentar explícitamente (OpenAPI, ver [`../architecture/openapi.md`](../architecture/openapi.md)): qué campos son obligatorios vs. opcionales, qué formato exacto de fecha/número se usa, qué codificación de caracteres, y **el comportamiento exacto ante campos desconocidos** (¿se ignoran silenciosamente, o la petición se rechaza?) — un sistema externo integrado por otro equipo no puede "adivinar" estas decisiones desde el código fuente como sí puede un equipo interno.

## Paginación y límites explícitos para consumidores batch

```
GET /api/v1/pedidos?cursor=abc123&limit=500
```

Sistemas de integración (ERPs sincronizando catálogos completos, por ejemplo) suelen necesitar recorrer volúmenes grandes de datos de forma completa y fiable — un límite de página demasiado pequeño sin cursor eficiente convierte una sincronización razonable en miles de llamadas; documentar explícitamente el límite máximo permitido y el mecanismo de cursor (ver [`../../standards/api.md`](../../standards/api.md)).

## Bulk operations: cuándo ofrecerlas

```
POST /api/v1/pedidos/bulk
{ "pedidos": [ { ... }, { ... }, ... ] }
```

Si un sistema externo necesita crear/actualizar cientos de registros de una vez (importación masiva, sincronización periódica), una API que solo soporta un registro por llamada fuerza al consumidor a cientos de peticiones HTTP — considerar un endpoint bulk cuando el patrón de uso real de la integración lo justifica (no por defecto, ver [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)), con manejo de éxito/fallo parcial explícito por elemento (`207 Multi-Status` o un array de resultados individuales en el body).

## Idempotencia como parte del contrato, no un detalle interno

```
POST /api/v1/pedidos
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```

Para operaciones de creación con efectos costosos de duplicar (crear un pedido, iniciar un envío en un WMS), exponer explícitamente el soporte de una cabecera de idempotencia en la documentación del contrato — ver [`idempotency.md`](./idempotency.md) para la implementación. Un sistema externo que reintenta ante timeout (comportamiento estándar en integraciones) necesita saber que puede hacerlo sin duplicar el efecto.

## Checklist

- [ ] ¿El contrato documenta explícitamente el comportamiento ante campos desconocidos/opcionales, sin dejarlo implícito?
- [ ] ¿Existe paginación con cursor eficiente para consumidores que sincronizan volúmenes grandes?
- [ ] ¿Se ha evaluado si el patrón de uso real justifica un endpoint bulk?
- [ ] ¿Las operaciones de creación costosas soportan una `Idempotency-Key` documentada?

Ver también: [`api-versioning.md`](./api-versioning.md), [`idempotency.md`](./idempotency.md), [`../../standards/api.md`](../../standards/api.md), [`../architecture/openapi.md`](../architecture/openapi.md).
