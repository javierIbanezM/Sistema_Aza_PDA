# Standard: API

> Norma prescriptiva de diseño de APIs REST. Ver `CLAUDE.md` sección 15 y [`../knowledge/architecture/openapi.md`](../knowledge/architecture/openapi.md) para el razonamiento y la documentación de contrato completos.

## Convenciones de URL

- Recursos en plural, sustantivos, `kebab-case`: `/pedidos`, `/lineas-pedido`.
- Sin verbos en la URL: `POST /pedidos` (no `/pedidos/crear`), `PATCH /pedidos/{id}/confirmar` es la excepción aceptable para transiciones de estado que no encajan bien en un `PUT`/`PATCH` de recurso genérico (acción de negocio explícita, no CRUD).
- Anidación de recursos limitada a una relación de pertenencia real: `/pedidos/{pedidoId}/lineas`, no anidar más de 2 niveles.
- Versionado explícito en la URL: `/api/v1/...`.

## Verbos HTTP

| Verbo | Uso | Idempotente |
|---|---|---|
| `GET` | Leer un recurso o colección | Sí |
| `POST` | Crear un recurso, o ejecutar una acción de negocio que no encaja en CRUD | No |
| `PUT` | Reemplazar un recurso completo | Sí |
| `PATCH` | Actualizar parcialmente un recurso | No (salvo diseño explícito idempotente) |
| `DELETE` | Eliminar un recurso | Sí |

## Códigos de respuesta

| Código | Uso |
|---|---|
| `200 OK` | Éxito con cuerpo de respuesta (`GET`, `PUT`, `PATCH`) |
| `201 Created` | Recurso creado (`POST`), incluir header `Location` con la URL del nuevo recurso |
| `204 No Content` | Éxito sin cuerpo de respuesta (`DELETE`, o `POST` de una acción sin resultado que devolver) |
| `400 Bad Request` | Validación de forma fallida (ver [`validation.md`](./validation.md)) |
| `401 Unauthorized` | No autenticado |
| `403 Forbidden` | Autenticado pero sin permiso para la acción |
| `404 Not Found` | Recurso no encontrado |
| `409 Conflict` | Conflicto de estado (recurso ya existe, transición de estado inválida) |
| `422 Unprocessable Entity` | Sintaxis correcta pero viola una regla de negocio |
| `429 Too Many Requests` | Rate limit excedido |
| `500 Internal Server Error` | Error no controlado — debe ser excepcional, nunca la respuesta esperada de un flujo conocido |

## Formato de error estándar (obligatorio en todo el framework)

```json
{
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "No se encontró el pedido solicitado",
    "details": []
  }
}
```

`code` es un identificador estable y legible por máquina (para que el cliente pueda tomar decisiones programáticas), `message` es legible por humanos, `details` contiene errores de validación específicos por campo cuando aplica.

## Paginación

- Obligatoria en todo endpoint que devuelve una colección potencialmente grande.
- Cursor-based por defecto en datasets grandes o con escrituras concurrentes; offset-based aceptable en datasets pequeños/estables.
- Metadatos de paginación en la respuesta (`nextCursor`/`totalCount`/`hasMore`), nunca solo el array desnudo.

## Idempotencia

- Operaciones `PUT`/`DELETE` deben ser idempotentes por diseño (ejecutar la misma petición N veces produce el mismo resultado que ejecutarla una vez).
- Para `POST` que crea recursos con efectos secundarios costosos (pagos, envíos), soportar una cabecera `Idempotency-Key` para evitar duplicados ante reintentos del cliente.

## Checklist

- [ ] ¿La URL usa sustantivos en plural sin verbos, con la excepción justificada de acciones de negocio explícitas?
- [ ] ¿Los códigos de respuesta usados son semánticamente correctos, no solo `200`/`500` para todo?
- [ ] ¿Los errores siguen el formato estándar `{ error: { code, message, details } }`?
- [ ] ¿Los endpoints de colección tienen paginación?
- [ ] ¿Las operaciones `PUT`/`DELETE` son idempotentes?

Ver también: `CLAUDE.md` sección 15, [`../knowledge/architecture/openapi.md`](../knowledge/architecture/openapi.md), [`dto.md`](./dto.md), [`validation.md`](./validation.md).
