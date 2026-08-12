# Event Loop

> Fundamento del modelo de concurrencia de Node.js, necesario para entender [`async-patterns.md`](./async-patterns.md) y diagnosticar problemas de rendimiento en [`performance.md`](./performance.md).

## Modelo de un único hilo, I/O no bloqueante

Node.js ejecuta JavaScript en un **único hilo principal**, pero delega operaciones de I/O (lectura de archivos, red, timers) a la libuv thread pool o al sistema operativo, y las retoma vía callbacks cuando completan — el hilo principal nunca se bloquea esperando I/O, solo espera bloqueado durante cálculo puro (CPU-bound).

## Fases del Event Loop

```
┌───────────────────────────┐
│           timers          │  setTimeout, setInterval
├───────────────────────────┤
│     pending callbacks     │  callbacks de I/O diferidos del ciclo anterior
├───────────────────────────┤
│           poll            │  recupera nuevos eventos de I/O, ejecuta sus callbacks
├───────────────────────────┤
│           check           │  setImmediate
├───────────────────────────┤
│      close callbacks      │  ej. socket.on('close', ...)
└───────────────────────────┘
```

Entre cada fase (y entre cada callback individual), Node.js drena la **microtask queue** (Promises resueltas, `queueMicrotask`) completamente antes de continuar — por eso una Promise resuelta se ejecuta antes que un `setTimeout(fn, 0)`, aunque ambos se programen en el mismo instante.

```javascript
setTimeout(() => console.log('timeout'), 0);
Promise.resolve().then(() => console.log('promise'));
console.log('sync');

// Orden de salida: sync, promise, timeout
```

## Bloquear el Event Loop: el error más costoso

```javascript
// Bloquea el hilo único durante todo el cálculo — NINGUNA otra petición se atiende mientras tanto
app.get('/reporte-costoso', (req, res) => {
  const resultado = calcularFibonacciRecursivo(45); // CPU-bound, síncrono, bloqueante
  res.json({ resultado });
});
```

Mientras este cálculo se ejecuta, el servidor **no puede atender ninguna otra petición**, aunque sean miles de conexiones I/O-bound esperando — el hilo único está ocupado. Ver [`performance.md`](./performance.md) para estrategias (Worker Threads, dividir el trabajo en chunks con `setImmediate`) ante trabajo genuinamente CPU-bound.

## `process.nextTick` vs. microtasks vs. macrotasks

| Cola | Prioridad | Ejemplo |
|---|---|---|
| `process.nextTick` | Máxima — se vacía completamente antes que cualquier microtask | `process.nextTick(fn)` |
| Microtask queue | Alta — se vacía completamente entre cada fase del loop | `Promise.then()`, `queueMicrotask()` |
| Macrotask (fases del loop) | Normal | `setTimeout`, `setImmediate`, I/O callbacks |

Un uso recursivo/excesivo de `process.nextTick` puede **inanir** (starve) el Event Loop — si cada `nextTick` programa otro `nextTick`, el loop nunca avanza a la siguiente fase, bloqueando I/O indefinidamente. Usar con moderación, nunca en un bucle sin condición de salida clara.

## Diagnóstico: medir la latencia del Event Loop

```javascript
const { monitorEventLoopDelay } = require('perf_hooks');
const histogram = monitorEventLoopDelay({ resolution: 20 });
histogram.enable();

setInterval(() => {
  console.log(`Event Loop delay p99: ${histogram.percentile(99) / 1e6}ms`);
}, 5000);
```

Un `delay` creciente indica que el Event Loop está saturado con trabajo síncrono — señal directa de código bloqueante que debería moverse a un Worker Thread o dividirse en chunks asíncronos (ver [`performance.md`](./performance.md)).

## Checklist

- [ ] ¿Se ha evitado código CPU-bound síncrono largo dentro de un handler de petición?
- [ ] ¿Se entiende que Promises resueltas se ejecutan antes que `setTimeout`, incluso con delay 0?
- [ ] ¿Se ha evitado un uso recursivo de `process.nextTick` que podría inanir el Event Loop?
- [ ] ¿Existe monitorización de la latencia del Event Loop en producción?

Ver también: [`async-patterns.md`](./async-patterns.md), [`performance.md`](./performance.md).
