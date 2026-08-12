# Patrón: Circuit Breaker

## Descripción

Circuit Breaker protege un sistema de fallos en cascada cuando depende de un servicio externo inestable, "abriendo el circuito" (dejando de intentar la llamada real y devolviendo un fallo rápido o una respuesta alternativa) tras detectar una tasa de fallos por encima de un umbral, en lugar de seguir intentando llamadas que probablemente van a fallar y consumir recursos (hilos, conexiones, tiempo de espera) mientras lo hacen.

## Estados del circuito

- **Closed (cerrado)**: estado normal. Las llamadas pasan directamente al servicio real. Se contabilizan fallos y éxitos.
- **Open (abierto)**: tras superar el umbral de fallos configurado, el circuito se abre. Las llamadas fallan inmediatamente (o se resuelven con un *fallback*) sin siquiera intentar contactar al servicio real, durante un periodo de espera configurado.
- **Half-Open (semi-abierto)**: pasado el periodo de espera, se permite un número limitado de llamadas de prueba al servicio real. Si tienen éxito, el circuito vuelve a `Closed`; si fallan, vuelve a `Open`.

## Cuándo usarlo

- En cualquier llamada saliente a un servicio externo (otro microservicio, una API de terceros, una base de datos remota) que pueda fallar o degradarse, especialmente si esa llamada ocurre con alta frecuencia y en el camino crítico de una petición de usuario.
- Cuando un fallo prolongado del servicio externo podría, sin protección, agotar recursos propios (pool de conexiones, hilos bloqueados esperando timeout) y provocar que el propio servicio deje de responder también — el llamado "fallo en cascada".

## Cuándo NO usarlo

- En llamadas internas dentro del mismo proceso, donde no hay latencia de red ni posibilidad real de fallo parcial de infraestructura.
- Para operaciones donde un fallo simplemente debe propagarse y no existe ningún *fallback* razonable ni beneficio en "fallar rápido" frente a esperar el timeout normal — evaluar caso por caso, no aplicar por defecto a toda llamada externa sin criterio (ver YAGNI en [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md) si el servicio en cuestión tiene un SLA de fiabilidad muy alto y ya existe protección a otro nivel).

## Ventajas

- Evita que un servicio externo caído o lento arrastre a los servicios que dependen de él a un fallo en cascada.
- Falla rápido (*fail fast*) en lugar de acumular peticiones bloqueadas esperando timeouts largos, liberando recursos para atender otras peticiones.
- Da tiempo al servicio externo para recuperarse, al reducir la carga que recibe mientras el circuito está abierto.

## Desventajas

- Añade complejidad de configuración (umbrales de fallo, tiempos de espera) que requiere ajuste basado en el comportamiento real observado, no en valores arbitrarios.
- Mal configurado (umbral demasiado sensible), puede abrir el circuito ante fallos transitorios normales, degradando el servicio innecesariamente.
- Requiere definir explícitamente un comportamiento de *fallback* razonable — si no existe una alternativa sensata, el Circuit Breaker solo cambia el tipo de fallo, no lo elimina.

## Ejemplo (Java con Resilience4j)

```java
CircuitBreakerConfig config = CircuitBreakerConfig.custom()
    .failureRateThreshold(50) // se abre si el 50% de las últimas llamadas fallan
    .waitDurationInOpenState(Duration.ofSeconds(30)) // tiempo abierto antes de pasar a half-open
    .slidingWindowSize(20) // se evalúa sobre las últimas 20 llamadas
    .permittedNumberOfCallsInHalfOpenState(3)
    .build();

CircuitBreaker circuitBreaker = CircuitBreaker.of("servicioPagos", config);

Supplier<ResultadoPago> llamadaProtegida = CircuitBreaker.decorateSupplier(
    circuitBreaker,
    () -> clienteServicioPagos.procesar(pago)
);

try {
    ResultadoPago resultado = llamadaProtegida.get();
} catch (CallNotPermittedException e) {
    // El circuito está abierto: fallback sin intentar la llamada real
    resultado = ResultadoPago.pendienteDeReintento();
}
```

## Ejemplo (Node.js con `opossum`)

```javascript
const CircuitBreaker = require('opossum');

const opciones = {
  timeout: 3000,             // si la llamada tarda más de 3s, se considera fallo
  errorThresholdPercentage: 50,
  resetTimeout: 30000,       // tiempo en estado Open antes de pasar a Half-Open
};

const breaker = new CircuitBreaker(llamarServicioPagos, opciones);

breaker.fallback(() => ({ estado: 'PENDIENTE_DE_REINTENTO' }));

breaker.on('open', () => logger.warn('Circuit breaker de servicio de pagos: ABIERTO'));
breaker.on('halfOpen', () => logger.info('Circuit breaker de servicio de pagos: HALF-OPEN'));
breaker.on('close', () => logger.info('Circuit breaker de servicio de pagos: CERRADO'));

const resultado = await breaker.fire(pago);
```

## Errores comunes

- No definir un *fallback* explícito y significativo, dejando que el circuito abierto simplemente propague un error genérico sin valor añadido para el consumidor.
- Compartir un único Circuit Breaker para llamadas a servicios distintos con perfiles de fiabilidad muy diferentes, mezclando sus tasas de fallo y produciendo aperturas de circuito que no reflejan el estado real de cada dependencia individual.
- No monitorizar los cambios de estado del circuito (ver [`../logging.md`](../knowledge/architecture/logging.md)) — el equipo debe saber cuándo un circuito se abre en producción, ya que es una señal directa de degradación de una dependencia externa.

Ver también: [`../logging.md`](../knowledge/architecture/logging.md), sección 17 de `CLAUDE.md` (Microservicios).
