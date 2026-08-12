# Virtual Threads (Project Loom, Java 21+)

> Ver [`concurrency.md`](./concurrency.md) para el modelo de concurrencia tradicional (platform threads) que los virtual threads complementan, especialmente relevante en cargas I/O-bound como las típicas APIs REST.

## Qué resuelven

Un platform thread (hilo tradicional de la JVM) mapea 1:1 a un hilo del sistema operativo — crear miles es costoso en memoria y en cambios de contexto. Un virtual thread es gestionado por la JVM, mucho más ligero (se pueden crear millones), y especialmente eficiente en operaciones **bloqueantes de I/O** (llamadas de red, queries de base de datos, lectura de archivos): cuando un virtual thread bloquea en I/O, la JVM libera el hilo de plataforma subyacente para que ejecute otro virtual thread, en lugar de dejarlo ocioso esperando.

## Cuándo aportan valor real

- Aplicaciones con **muchas peticiones concurrentes, cada una con trabajo mayormente I/O-bound** (llamadas a base de datos, a otros servicios) — el caso típico de una API REST.
- **No** aportan beneficio en trabajo CPU-bound (cálculo intensivo) — ahí el cuello de botella son los núcleos de CPU disponibles, no la gestión de hilos.

## Uso básico

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<ResultadoPago>> futuros = pedidos.stream()
        .map(pedido -> executor.submit(() -> procesarPago(pedido))) // cada tarea en su propio virtual thread
        .toList();

    for (Future<ResultadoPago> futuro : futuros) {
        futuro.get();
    }
}
```

`Executors.newVirtualThreadPerTaskExecutor()` crea un nuevo virtual thread por cada tarea enviada — a diferencia de un pool fijo de platform threads, no hay un límite práctico que gestionar manualmente para cargas I/O-bound.

## Integración con Spring Boot 3.2+

```yaml
spring:
  threads:
    virtual:
      enabled: true # Tomcat/el servidor embebido procesa cada petición HTTP en un virtual thread
```

Con esta configuración, cada petición HTTP entrante se ejecuta en su propio virtual thread automáticamente, sin cambios en el código de los controllers — el modelo de programación sigue siendo el mismo código bloqueante/síncrono habitual (`@RestController` normal), pero con la eficiencia de recursos de los virtual threads por debajo.

## `synchronized` y virtual threads: cuidado con el "pinning"

```java
// Evitar: un bloque synchronized "fija" (pin) el virtual thread al platform thread subyacente
// durante toda la operación bloqueante, anulando el beneficio de los virtual threads
synchronized (lock) {
    resultado = llamadaDeRedBloqueante(); // el platform thread queda bloqueado, no se libera
}

// Preferir ReentrantLock, que no produce pinning
private final ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    resultado = llamadaDeRedBloqueante();
} finally {
    lock.unlock();
}
```

Este es el principal "gotcha" a vigilar al adoptar virtual threads en código existente que usa `synchronized`: dentro de un bloque `synchronized`, el virtual thread queda "clavado" (*pinned*) a su platform thread durante toda la operación bloqueante, anulando la ventaja de liberar el hilo subyacente. `ReentrantLock` no tiene este problema.

## No usar como pool limitado

```java
// Antipatrón: los virtual threads no están pensados como un pool de tamaño fijo
ExecutorService pool = Executors.newFixedThreadPool(10, Thread.ofVirtual().factory());
```

Si se necesita limitar la concurrencia (por ejemplo, no saturar un servicio externo con más de N llamadas simultáneas), usar un `Semaphore` explícito junto con `newVirtualThreadPerTaskExecutor()`, en lugar de forzar un tamaño de pool fijo sobre virtual threads — el valor de los virtual threads es precisamente no necesitar ese límite artificial para la gestión de recursos de la propia JVM.

## Checklist

- [ ] ¿La carga de trabajo es mayormente I/O-bound (red, base de datos), donde los virtual threads aportan valor real?
- [ ] ¿Se ha revisado el código en busca de bloques `synchronized` que envuelven operaciones bloqueantes (riesgo de pinning)?
- [ ] ¿Se usa `Semaphore` en lugar de un pool de tamaño fijo cuando se necesita limitar concurrencia hacia un recurso externo?

Ver también: [`concurrency.md`](./concurrency.md), [`performance.md`](./performance.md), [`../reference/java-versions.md`](../../reference/java-versions.md).
