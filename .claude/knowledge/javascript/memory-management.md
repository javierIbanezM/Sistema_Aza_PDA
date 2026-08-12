# Memory Management

> Ver [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md) para el patrón general de fuga de memoria (aplicable a cualquier lenguaje). Este documento cubre la mecánica específica del recolector de basura de V8 (motor de JavaScript usado en Chrome y Node.js).

## Recolección de basura generacional (V8)

V8 divide el heap en generaciones: **espacio joven** (*young generation*, objetos recién creados, recolectado frecuentemente con un algoritmo rápido — *Scavenge*) y **espacio viejo** (*old generation*, objetos que han sobrevivido varios ciclos del joven, recolectado con menor frecuencia pero de forma más exhaustiva — *Mark-Sweep-Compact*). La premisa detrás de este diseño: la mayoría de objetos mueren jóvenes (variables locales de corta vida), por lo que optimizar para ese caso común mejora el rendimiento general.

## Qué mantiene un objeto vivo: alcanzabilidad, no conteo de referencias

```javascript
let objetoA = { nombre: 'A' };
let objetoB = { nombre: 'B', referenciaA: objetoA };
objetoA = null; // el objeto original {nombre: 'A'} SIGUE vivo, porque objetoB.referenciaA aún lo referencia
```

V8 (como la mayoría de recolectores modernos) usa alcanzabilidad desde un conjunto de raíces (variables globales, el stack de ejecución actual) — un objeto se libera solo cuando ninguna cadena de referencias alcanzables lo mantiene vivo, no simplemente cuando su variable original se pone a `null`.

## Patrones que retienen memoria sin que sea obvio

```javascript
// Closures que retienen ámbitos completos — ver closures.md
function crearHandler(datosGrandes) {
  return () => procesar(); // el closure retiene TODO el ámbito de crearHandler, incluyendo datosGrandes
}

// Listeners no removidos — ver dom.md
window.addEventListener('resize', handlerQueNuncaSeRemueve);

// Referencias en estructuras de caché sin límite ni TTL
const cache = new Map(); // crece indefinidamente si nada la limpia
```

Ver [`closures.md`](./closures.md) y [`dom.md`](./dom.md) para el detalle de cada patrón — la causa raíz común es siempre la misma: una referencia alcanzable que ya no debería existir.

## `WeakMap` y `WeakSet`: referencias que no impiden la recolección

```javascript
const metadatosPorElemento = new WeakMap();

function asociarMetadatos(elementoDom, datos) {
  metadatosPorElemento.set(elementoDom, datos); // si elementoDom se elimina del DOM y pierde otras referencias,
}                                                 // tanto él como sus datos asociados pueden liberarse igualmente
```

A diferencia de un `Map` normal, las claves de un `WeakMap` no cuentan como referencia "fuerte" para el recolector — si la única referencia restante a un objeto es como clave de un `WeakMap`, ese objeto puede liberarse igualmente. Útil específicamente para asociar metadatos a objetos (elementos DOM, por ejemplo) sin impedir su recolección cuando ya no se usan por ningún otro motivo.

## Diagnóstico con Chrome DevTools / Node.js

```javascript
// En Node.js, forzar snapshot del heap para comparar entre dos momentos
const v8 = require('v8');
v8.writeHeapSnapshot('antes.heapsnapshot');
// ... ejecutar la operación sospechosa varias veces ...
v8.writeHeapSnapshot('despues.heapsnapshot');
```

Comparar dos snapshots del heap (en Chrome DevTools → Memory, o herramientas equivalentes para Node.js) tomados en momentos distintos bajo carga similar es la técnica estándar para identificar qué tipo de objeto está creciendo en número de instancias de forma anómala — ver [`../node/performance.md`](../node/performance.md) para el flujo de diagnóstico completo del lado servidor.

## Checklist

- [ ] ¿Se entiende que la recolección de basura se basa en alcanzabilidad, no en poner variables a `null`?
- [ ] ¿Las cachés en memoria tienen límite de tamaño y/o TTL, en lugar de crecer indefinidamente?
- [ ] ¿Se ha considerado `WeakMap`/`WeakSet` para asociar metadatos a objetos sin impedir su recolección?

Ver también: [`closures.md`](./closures.md), [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md), [`../node/performance.md`](../node/performance.md).
