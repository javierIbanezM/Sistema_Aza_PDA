# Retries and Backoff

> Ver [`idempotency.md`](./idempotency.md) — reintentar solo es seguro si la operación es idempotente. Ver [`../../standards/exceptions.md`](../../standards/exceptions.md) para la distinción entre errores transitorios y permanentes que determina qué se reintenta.

## Qué reintentar (y qué nunca reintentar)

| Tipo de error | ¿Reintentar? |
|---|---|
| Timeout de red | Sí — puede ser transitorio |
| `503 Service Unavailable` / `502 Bad Gateway` | Sí — el servidor señala sobrecarga temporal |
| `429 Too Many Requests` | Sí, respetando `Retry-After` si el servidor lo indica |
| `500 Internal Server Error` | Depende — si es determinista (mismo input, mismo fallo), no reintentar sin cambios |
| `400 Bad Request` / `404 Not Found` / `422 Unprocessable Entity` | **Nunca** — el mismo request producirá el mismo error |
| `401 Unauthorized` | No reintentar el mismo request; renovar credenciales primero, luego un único reintento |

Reintentar un error 4xx (excepto 429) es la causa más común de retries desperdiciados en integraciones mal diseñadas — un `404` no va a convertirse en `200` por reintentar la petición idéntica.

## Backoff exponencial con jitter

```java
int intento = 0;
long delayBase = 100; // ms
while (intento < maxIntentos) {
    try {
        return llamarServicioExterno();
    } catch (ErrorTransitorioException e) {
        intento++;
        if (intento == maxIntentos) throw e;
        long delay = (long) (delayBase * Math.pow(2, intento)) + ThreadLocalRandom.current().nextLong(0, 100);
        Thread.sleep(delay); // 100ms, 200ms, 400ms... + jitter aleatorio
    }
}
```

El **backoff exponencial** evita bombardear un sistema ya sobrecargado con reintentos inmediatos repetidos (que empeorarían la sobrecarga); el **jitter** (aleatoriedad añadida) evita que múltiples clientes reintentando simultáneamente ante el mismo fallo (ej. tras una caída breve del servicio) converjan todos en el mismo instante de reintento, produciendo un pico sincronizado de carga — un antipatrón conocido como *thundering herd*.

## Límite máximo de reintentos y timeout total

```java
RetryConfig config = RetryConfig.custom()
    .maxAttempts(3)
    .waitDuration(Duration.ofMillis(200))
    .retryExceptions(ErrorTransitorioException.class)
    .build();
```

Un número de reintentos sin límite (o un límite demasiado alto) puede hacer que una petición del usuario final espere minutos antes de fallar definitivamente — establecer tanto un máximo de intentos como un timeout total razonable para la operación completa, no solo por intento individual.

## Respetar `Retry-After`

```
HTTP/1.1 429 Too Many Requests
Retry-After: 30
```

Si el servidor indica explícitamente cuánto esperar, respetarlo en lugar de aplicar el propio backoff calculado — el servidor tiene información directa de su capacidad de recuperación que el cliente no tiene.

## Reintentos y Circuit Breaker: mecanismos complementarios

Los reintentos gestionan fallos **puntuales** de una llamada individual; el Circuit Breaker (ver [`circuit-breaker.md`](./circuit-breaker.md)) gestiona fallos **sostenidos** de un servicio completo — combinarlos correctamente: reintentar dentro del límite de intentos configurado, y solo si el circuito no está ya abierto (evitar reintentar contra un servicio que el propio sistema ya ha determinado que está caído).

## Checklist

- [ ] ¿Se reintentan solo errores transitorios, nunca errores 4xx deterministas (excepto 429)?
- [ ] ¿El backoff es exponencial con jitter, no un delay fijo o inmediato?
- [ ] ¿Existe un límite máximo de intentos y un timeout total razonable?
- [ ] ¿Se respeta la cabecera `Retry-After` cuando el servidor la proporciona?
- [ ] ¿La operación reintentada es idempotente (ver [`idempotency.md`](./idempotency.md))?

Ver también: [`idempotency.md`](./idempotency.md), [`circuit-breaker.md`](./circuit-breaker.md), [`../../standards/exceptions.md`](../../standards/exceptions.md).
