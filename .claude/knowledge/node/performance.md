# Performance (Node.js)

> Ver [`event-loop.md`](./event-loop.md) para el modelo de concurrencia del que depende todo diagnóstico de rendimiento en Node. Ver [`../../prompts/performance.md`](../../prompts/performance.md) para la metodología general.

## Profiling con el inspector nativo

```bash
node --prof app.js
# tras generar carga representativa...
node --prof-process isolate-*.log > perfil.txt
```

```bash
# Alternativa interactiva con Chrome DevTools
node --inspect app.js
# abrir chrome://inspect en Chrome, conectar, usar la pestaña Profiler
```

## Worker Threads para trabajo CPU-bound

```typescript
// main.ts
import { Worker } from 'worker_threads';

function calcularEnWorker(datos: number[]): Promise<number> {
  return new Promise((resolve, reject) => {
    const worker = new Worker('./calculo-worker.js', { workerData: datos });
    worker.on('message', resolve);
    worker.on('error', reject);
  });
}
```

```typescript
// calculo-worker.ts
import { parentPort, workerData } from 'worker_threads';
const resultado = calculoCostosoSincrono(workerData);
parentPort?.postMessage(resultado);
```

Ver [`event-loop.md`](./event-loop.md) — un cálculo CPU-bound síncrono bloquea el hilo único del Event Loop; moverlo a un Worker Thread libera el hilo principal para seguir atendiendo peticiones I/O-bound mientras el cálculo se ejecuta en paralelo.

## Clustering para aprovechar múltiples núcleos

```typescript
import cluster from 'cluster';
import os from 'os';

if (cluster.isPrimary) {
  const nucleos = os.cpus().length;
  for (let i = 0; i < nucleos; i++) cluster.fork();

  cluster.on('exit', (worker) => {
    logger.warn(`Worker ${worker.process.pid} murió, reiniciando`);
    cluster.fork(); // reiniciar automáticamente ante caída
  });
} else {
  iniciarServidorHttp(); // cada worker es una instancia independiente del proceso Node
}
```

Node.js es single-threaded para JavaScript, pero una instancia solo aprovecha un núcleo de CPU — `cluster` (o un orquestador externo como PM2/Kubernetes con múltiples réplicas) permite escalar horizontalmente dentro de la misma máquina, aprovechando todos los núcleos disponibles.

## Medir antes de cachear/optimizar

```typescript
console.time('buscarPedidos');
const pedidos = await pedidoRepository.buscarTodos();
console.timeEnd('buscarPedidos');
```

Para medición estructurada en producción, usar `perf_hooks`:

```typescript
import { performance } from 'perf_hooks';

const inicio = performance.now();
await operacionCostosa();
logger.info('duracion_ms', performance.now() - inicio);
```

## Memory leaks: patrones comunes en Node.js

Ver [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md) para el patrón general. Específico de Node.js:

```typescript
// Closures que retienen referencias grandes innecesariamente en listeners de larga duración
function registrarListener(datosGrandes: BigData) {
  eventEmitter.on('evento', () => {
    // aunque no se use datosGrandes aquí, el closure lo retiene mientras el listener exista
    procesar();
  });
}

// EventEmitter sin límite de listeners — señal de listeners no removidos acumulándose
emitter.setMaxListeners(20); // por defecto Node.js avisa (warning) a partir de 10
```

Un warning `MaxListenersExceededWarning` es una señal directa de listeners registrados repetidamente sin des-registrarse — investigar la causa en lugar de simplemente aumentar el límite.

## `--max-old-space-size`: dimensionar el heap de V8

```bash
node --max-old-space-size=4096 app.js  # 4GB de heap máximo
```

El límite por defecto del heap de V8 puede ser insuficiente para aplicaciones con datasets grandes en memoria — ajustar explícitamente según el uso real medido (con `process.memoryUsage()` o profiling), no arbitrariamente.

## Checklist

- [ ] ¿El trabajo CPU-bound significativo se ha movido a Worker Threads, liberando el Event Loop?
- [ ] ¿La aplicación aprovecha múltiples núcleos vía `cluster` o múltiples réplicas orquestadas externamente?
- [ ] ¿Se ha perfilado con `--prof`/Chrome DevTools antes de optimizar, en lugar de por intuición?
- [ ] ¿Se ha investigado la causa ante un warning `MaxListenersExceededWarning`, no solo aumentado el límite?

Ver también: [`event-loop.md`](./event-loop.md), [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md), [`../../prompts/performance.md`](../../prompts/performance.md).
