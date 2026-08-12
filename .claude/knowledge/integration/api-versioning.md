# API Versioning

> Ver [`api-design.md`](./api-design.md) para el contexto de por qué el versionado es más crítico en APIs consumidas por sistemas externos. Ver [`../../standards/api.md`](../../standards/api.md) para la convención base (versión explícita en la URL) que este documento profundiza.

## Compatibilidad hacia atrás como valor por defecto

Un cambio **aditivo** (nuevo campo opcional en la respuesta, nuevo endpoint) no requiere nueva versión — un consumidor que ignora campos desconocidos sigue funcionando. Un cambio que **rompe** (eliminar un campo, cambiar su tipo, cambiar el significado de un valor existente, hacer obligatorio un campo antes opcional) sí la requiere. La disciplina de distinguir ambos casos correctamente evita versionar con más frecuencia de la necesaria (fricción para todos los consumidores) o con menos de la necesaria (romper integraciones sin aviso).

## Estrategias de versionado

| Estrategia | Ejemplo | Trade-off |
|---|---|---|
| En la URL | `/api/v1/pedidos`, `/api/v2/pedidos` | Más explícito y cacheable; requiere mantener rutas paralelas |
| En un header | `Accept-Version: 2` | URL estable; menos visible/descubrible para integradores externos |
| En el content type | `Accept: application/vnd.empresa.v2+json` | Preciso por recurso; poco habitual, mayor curva de aprendizaje |

Este framework usa versión en la URL por defecto (ver [`../../standards/api.md`](../../standards/api.md)) — más simple de entender y depurar para equipos externos que integran el sistema sin contexto profundo del framework.

## Periodo de convivencia entre versiones

```
/api/v1/pedidos  (deprecated, soporte hasta 2026-12-31)
/api/v2/pedidos  (actual)
```

Anunciar la deprecación de una versión con antelación explícita (fecha de fin de soporte publicada, no solo "eventualmente la quitaremos"), y mantener ambas versiones funcionando en paralelo durante ese periodo — especialmente crítico con consumidores externos (ERPs, WMS) cuyo ciclo de actualización puede ser mucho más lento que el del propio equipo.

## Cabecera de deprecación explícita

```
HTTP/1.1 200 OK
Deprecation: true
Sunset: Sat, 31 Dec 2026 23:59:59 GMT
Link: <https://docs.empresa.com/migracion-v2>; rel="deprecation"
```

Las cabeceras `Deprecation`/`Sunset` (estándares emergentes) permiten que un consumidor programático detecte automáticamente que está usando una versión en proceso de retirada, sin depender de que un humano lea la documentación — relevante para integraciones de sistemas que rara vez son revisadas activamente tras su puesta en marcha inicial.

## Versionado de eventos (mensajería asíncrona)

```json
{ "tipo": "pedido.confirmado", "version": 2, "datos": { ... } }
```

Ver `CLAUDE.md` sección 16.2 — los eventos publicados en colas/topics necesitan su propio versionado independiente del de la API REST, ya que los consumidores de eventos pueden desplegarse y actualizarse de forma completamente desacoplada del productor. Ver [`event-driven.md`](./event-driven.md) y [`message-queues.md`](./message-queues.md).

## Checklist

- [ ] ¿Se distingue correctamente un cambio aditivo (sin nueva versión) de uno que rompe compatibilidad (con nueva versión)?
- [ ] ¿Existe una fecha de fin de soporte publicada para versiones deprecadas, no solo un aviso vago?
- [ ] ¿Los eventos asíncronos tienen su propio esquema de versionado, independiente de la API REST?

Ver también: [`api-design.md`](./api-design.md), [`../../standards/api.md`](../../standards/api.md), `CLAUDE.md` sección 16.
