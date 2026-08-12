# Concurrency

> Ver [`virtual-threads.md`](./virtual-threads.md) para el modelo de concurrencia moderno (Java 21+) que complementa/reemplaza buena parte de lo aquí descrito en cargas I/O-bound.

## `synchronized` vs. `java.util.concurrent`

Evitar `synchronized` manual salvo casos muy simples — preferir las utilidades de `java.util.concurrent`, diseñadas y probadas para los problemas comunes de concurrencia:

```java
// Evitar en código nuevo salvo necesidad muy específica
private final Object lock = new Object();
public void incrementar() {
    synchronized (lock) { contador++; }
}

// Preferir
private final AtomicInteger contador = new AtomicInteger();
public void incrementar() { contador.incrementAndGet(); }
```

## `ExecutorService` para gestionar pools de hilos

```java
ExecutorService executor = Executors.newFixedThreadPool(10);

try {
    List<Future<ResultadoPago>> futuros = pedidos.stream()
        .map(pedido -> executor.submit(() -> procesarPago(pedido)))
        .toList();

    for (Future<ResultadoPago> futuro : futuros) {
        ResultadoPago resultado = futuro.get(5, TimeUnit.SECONDS); // timeout explícito, nunca esperar indefinidamente
    }
} finally {
    executor.shutdown();
}
```

Nunca crear hilos manualmente con `new Thread(...)` en código de aplicación — un `ExecutorService` gestiona el ciclo de vida, límites de concurrencia y reutilización de hilos de forma controlada.

## `CompletableFuture` para composición asíncrona

```java
CompletableFuture<ResultadoPago> pagoFuturo = CompletableFuture
    .supplyAsync(() -> procesarPago(pedido), executor) // executor explícito, no el ForkJoinPool común por defecto
    .thenApply(this::registrarPago)
    .exceptionally(ex -> {
        log.error("Error procesando pago", ex);
        return ResultadoPago.fallido(ex.getMessage());
    });
```

Especificar siempre un `Executor` explícito en `supplyAsync`/`thenApplyAsync` en código de servidor — el `ForkJoinPool.commonPool()` por defecto es compartido con `parallelStream()` (ver [`streams.md`](./streams.md)) y con el resto de la aplicación; saturarlo con tareas de un componente afecta al rendimiento de otros.

## Estructuras concurrentes

```java
Map<String, Pedido> cache = new ConcurrentHashMap<>(); // sin bloqueo global, seguro para lectura/escritura concurrente
BlockingQueue<Tarea> cola = new LinkedBlockingQueue<>(1000); // productor-consumidor con backpressure
```

`ConcurrentHashMap` sobre `Collections.synchronizedMap` en prácticamente todos los casos — mejor throughput bajo contención al no bloquear el mapa completo por cada operación.

## Deadlocks: causa y prevención

Un deadlock ocurre cuando dos o más hilos esperan indefinidamente un recurso bloqueado por el otro. Prevención: **orden consistente de adquisición de locks** en todo el código — si el hilo A siempre adquiere `lockPedido` antes que `lockCliente`, y el hilo B hace lo mismo (nunca al revés), un deadlock circular entre esos dos locks es estructuralmente imposible. Ver [`../debugging.md`](./debugging.md) para diagnosticar deadlocks ya ocurridos con `jstack`.

## Race conditions en operaciones "leer-modificar-escribir"

```java
// Race condition: entre el get() y el put(), otro hilo puede modificar el mismo valor
if (!cache.containsKey(id)) {
    cache.put(id, calcularValorCostoso(id));
}

// Correcto: operación atómica
cache.computeIfAbsent(id, k -> calcularValorCostoso(k));
```

`ConcurrentHashMap` provee operaciones atómicas (`computeIfAbsent`, `merge`, `compute`) precisamente para evitar este patrón de "leer-decidir-escribir" no atómico, que es una fuente común y sutil de bugs de concurrencia.

## Checklist

- [ ] ¿Se usan utilidades de `java.util.concurrent` en lugar de `synchronized` manual salvo necesidad muy específica?
- [ ] ¿Los `ExecutorService`/hilos tienen timeout explícito al esperar resultados, nunca espera indefinida?
- [ ] ¿`CompletableFuture` especifica un `Executor` propio, sin saturar el `ForkJoinPool` común compartido?
- [ ] ¿Las estructuras compartidas entre hilos usan colecciones concurrentes, no colecciones normales sincronizadas manualmente?
- [ ] ¿El orden de adquisición de locks es consistente en todo el código, para descartar deadlocks estructurales?

Ver también: [`virtual-threads.md`](./virtual-threads.md), [`debugging.md`](./debugging.md), [`../../patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md).
