# Event Loop (JavaScript / Navegador)

> Este documento cubre el modelo **genérico** de Event Loop del lenguaje JavaScript (el usado en navegadores). Ver [`../node/event-loop.md`](../node/event-loop.md) para el modelo **específico de Node.js** (fases de libuv: timers, poll, check...), que es una implementación distinta con más granularidad que el modelo de navegador descrito aquí.

## El modelo clásico: Call Stack + Web APIs + Callback Queue + Microtask Queue

```
┌─────────────┐     ┌───────────────┐
│  Call Stack  │     │   Web APIs    │  setTimeout, fetch, addEventListener
└──────┬───────┘     └───────┬───────┘
       │                     │ (al completar)
       │              ┌──────▼───────┐
       │              │ Callback     │  (macrotasks: setTimeout, eventos DOM)
       │              │ Queue        │
       │              └──────┬───────┘
       │                     │
       │              ┌──────▼───────┐
       └─────────────►│ Microtask    │  (Promises, queueMicrotask)
         (drena TODA  │ Queue        │
          la cola      └──────────────┘
          antes de
          seguir)
```

El motor ejecuta código síncrono en el **Call Stack**. Cuando el stack está vacío, el Event Loop primero vacía **completamente** la microtask queue (todas las Promises resueltas pendientes), y solo entonces toma **una** tarea de la callback queue (macrotask) para ejecutar — repitiendo el ciclo.

## Por qué una Promise se ejecuta antes que un `setTimeout(fn, 0)`

```javascript
console.log('1');
setTimeout(() => console.log('2'), 0); // macrotask, va a la callback queue
Promise.resolve().then(() => console.log('3')); // microtask, va a la microtask queue
console.log('4');

// Orden: 1, 4, 3, 2
```

Aunque el `setTimeout` tiene delay 0, sigue siendo una macrotask — se ejecuta **después** de que la microtask queue se vacíe completamente, no antes. Este es el mismo principio (microtasks con prioridad sobre macrotasks) que aplica en Node.js, aunque el modelo de fases de Node es más granular (ver [`../node/event-loop.md`](../node/event-loop.md)).

## Web APIs: dónde vive realmente `setTimeout`/`fetch`

`setTimeout`, `fetch`, `addEventListener` **no son parte del lenguaje JavaScript en sí** — son APIs provistas por el entorno (el navegador, o el runtime de Node vía sus propias implementaciones equivalentes). El motor de JavaScript delega la espera/operación a esas APIs del entorno, y solo cuando completan, encolan su callback de vuelta para ejecutarse en el hilo principal.

## Bloquear el hilo principal: el mismo problema que en Node, distinto contexto

```javascript
// Bloquea la interfaz de usuario del navegador — ningún clic, scroll ni renderizado
// ocurre mientras este cálculo síncrono se ejecuta
function calculoCostoso() {
  let resultado = 0;
  for (let i = 0; i < 1e10; i++) resultado += i;
  return resultado;
}
```

En el navegador, bloquear el hilo principal congela la interfaz visualmente (la página "no responde"); en Node.js (ver [`../node/event-loop.md`](../node/event-loop.md)), bloquea la capacidad del servidor de atender otras peticiones concurrentes — mismo mecanismo subyacente, consecuencia distinta según el entorno.

## `requestAnimationFrame` y `requestIdleCallback`: específicos del navegador

```javascript
requestAnimationFrame(() => {
  // se ejecuta justo antes del siguiente repintado del navegador — ideal para animaciones
});

requestIdleCallback(() => {
  // se ejecuta cuando el hilo principal está inactivo — ideal para trabajo de baja prioridad
});
```

No tienen equivalente directo en Node.js (no hay renderizado que sincronizar) — mencionados aquí porque son parte del modelo de scheduling del navegador que complementa al Event Loop genérico.

## Checklist

- [ ] ¿Se entiende que las microtasks (Promises) siempre se vacían completamente antes de la siguiente macrotask, incluso con `setTimeout(fn, 0)`?
- [ ] ¿Se ha evitado bloquear el hilo principal con cálculo síncrono largo en código que corre en el navegador?
- [ ] Si el código corre en Node.js, ¿se ha consultado [`../node/event-loop.md`](../node/event-loop.md) para el modelo de fases específico, más granular que el descrito aquí?

Ver también: [`promises.md`](./promises.md), [`../node/event-loop.md`](../node/event-loop.md).
