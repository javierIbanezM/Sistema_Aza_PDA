# Error Handling (en integración de sistemas)

> Ver [`../../standards/exceptions.md`](../../standards/exceptions.md) para la norma general de manejo de excepciones — este documento cubre la clasificación y tratamiento de errores **específicos de comunicación entre sistemas**.

## Clasificación de errores de integración

| Categoría | Ejemplo | Tratamiento |
|---|---|---|
| Error de red | Timeout, conexión rechazada, DNS no resuelto | Reintentar (ver [`retries-and-backoff.md`](./retries-and-backoff.md)) |
| Error de disponibilidad del sistema externo | 503, circuito abierto | Reintentar tras backoff, o fallback (ver [`circuit-breaker.md`](./circuit-breaker.md)) |
| Error de negocio del sistema externo | "Stock insuficiente", "Cliente bloqueado" | **Nunca reintentar** — propagar como error de negocio propio |
| Error de contrato/formato | Campo inesperado, tipo incorrecto | **Nunca reintentar** — señala un desajuste de integración que requiere corrección de código |
| Error de autenticación | Token expirado | Renovar credenciales, un único reintento — nunca reintentar indefinidamente con las mismas credenciales inválidas |

Distinguir estas categorías es la base de toda la estrategia de reintentos (ver [`retries-and-backoff.md`](./retries-and-backoff.md)) — tratar un error de negocio como si fuera transitorio (reintentándolo) no solo desperdicia recursos, sino que puede ocultar un problema real de datos que necesita corrección, no reintento.

## Traducir errores del sistema externo al lenguaje propio

```java
public class ErpErrorTranslator {
    public DomainException traducir(ErpErrorResponse errorErp) {
        return switch (errorErp.getCodigo()) {
            case "STOCK_001" -> new StockInsuficienteException(errorErp.getDetalle());
            case "CLI_003" -> new ClienteBloqueadoException(errorErp.getDetalle());
            default -> new ErrorIntegracionErpException(errorErp.getCodigo(), errorErp.getMensaje());
        };
    }
}
```

Nunca propagar el código/formato de error crudo del sistema externo hacia el resto de la aplicación propia — un `ErpErrorTranslator` explícito (mismo principio que el Anti-Corruption Layer de [`schema-mapping.md`](./schema-mapping.md) aplicado a errores) traduce a excepciones del dominio propio, evitando que el resto del código dependa de los códigos de error específicos y poco documentados de cada sistema externo.

## Errores parciales en operaciones bulk

```json
{
  "resultados": [
    { "id": "p-1", "exito": true },
    { "id": "p-2", "exito": false, "error": { "code": "STOCK_INSUFICIENTE" } },
    { "id": "p-3", "exito": true }
  ]
}
```

Ver [`api-design.md`](./api-design.md) — una operación bulk que falla parcialmente debe reportar el resultado individual de cada elemento, no un único éxito/fallo global que oculta cuáles de los N elementos realmente se procesaron — el consumidor necesita saber exactamente qué reintentar (solo los fallidos) y qué no.

## Timeouts diferenciados por tipo de operación

```java
RequestConfig configLectura = RequestConfig.custom().setResponseTimeout(Timeout.ofSeconds(3)).build();
RequestConfig configEscrituraCritica = RequestConfig.custom().setResponseTimeout(Timeout.ofSeconds(15)).build();
```

Una consulta de solo lectura contra un sistema externo puede tener un timeout corto (el usuario espera una respuesta rápida, y si no llega, un fallback de caché es aceptable); una operación de escritura crítica (confirmar un pago, crear un pedido en el ERP) puede justificar un timeout más largo, ya que abortar prematuramente una operación que en realidad iba a completarse exitosamente en el sistema externo puede requerir una compensación costosa después (ver [`saga.md`](./saga.md)).

## Checklist

- [ ] ¿Se distingue explícitamente entre error transitorio, de negocio, de contrato y de autenticación, con tratamiento distinto para cada uno?
- [ ] ¿Los errores del sistema externo se traducen a excepciones de dominio propio, sin propagar códigos/formatos crudos?
- [ ] ¿Las operaciones bulk reportan el resultado individual de cada elemento, no solo un éxito/fallo global?
- [ ] ¿Los timeouts están calibrados según el tipo de operación (lectura vs. escritura crítica)?

Ver también: [`../../standards/exceptions.md`](../../standards/exceptions.md), [`retries-and-backoff.md`](./retries-and-backoff.md), [`schema-mapping.md`](./schema-mapping.md).
