# Promises

> Mecánica del lenguaje. Ver [`../node/async-patterns.md`](../node/async-patterns.md) para los patrones de aplicación de Promises en un backend Node.js (paralelización, manejo de errores en casos de uso) — este documento cubre cómo funciona una Promise internamente, sin el cual esos patrones son más difíciles de razonar con precisión.

## Los tres estados

```javascript
const promesa = new Promise((resolve, reject) => {
  // pending: estado inicial, mientras se ejecuta el "executor"
  const exito = true;
  if (exito) {
    resolve('valor'); // pasa a fulfilled, una única vez, irreversible
  } else {
    reject(new Error('fallo')); // pasa a rejected, una única vez, irreversible
  }
});
```

Una Promise **solo puede transicionar una vez** — de `pending` a `fulfilled` o de `pending` a `rejected`, nunca de vuelta, y llamar a `resolve`/`reject` más de una vez no tiene efecto tras la primera llamada.

## `.then()`, `.catch()`, `.finally()` y el encadenamiento

```javascript
fetch('/api/pedidos')
  .then(response => response.json()) // cada .then() devuelve una NUEVA Promise
  .then(datos => procesar(datos))
  .catch(error => console.error('Fallo en algún punto de la cadena', error))
  .finally(() => ocultarSpinner()); // se ejecuta siempre, éxito o fallo
```

Un único `.catch()` al final captura cualquier rechazo ocurrido en **cualquier** eslabón anterior de la cadena — no es necesario un `.catch()` por cada `.then()`, aunque a veces es deseable para manejo de errores diferenciado por etapa.

## Cómo funciona `.then()` internamente: siempre devuelve una nueva Promise

```javascript
const p1 = Promise.resolve(1);
const p2 = p1.then(valor => valor + 1); // p2 es una Promise NUEVA, distinta de p1
```

Esto es lo que permite el encadenamiento — cada `.then()` envuelve automáticamente el valor de retorno (o la Promise devuelta, si el callback devuelve otra Promise) en una nueva Promise, aplanándola si es necesario (nunca se obtiene una "Promise de Promise").

## Combinadores: cuándo usar cada uno

| Combinador | Comportamiento |
|---|---|
| `Promise.all` | Espera a todas; si UNA rechaza, rechaza inmediatamente perdiendo el resto |
| `Promise.allSettled` | Espera a todas SIEMPRE, da el resultado individual (éxito o fallo) de cada una |
| `Promise.race` | Resuelve/rechaza en cuanto la PRIMERA (éxito o fallo) termina |
| `Promise.any` | Resuelve en cuanto la PRIMERA tiene éxito; solo rechaza si TODAS fallan |

Ver [`../node/async-patterns.md`](../node/async-patterns.md) para ejemplos de aplicación de cada uno en un caso de uso backend real.

## `async/await`: azúcar sintáctico sobre Promises

```javascript
async function obtenerPedido(id) {
  const response = await fetch(`/api/pedidos/${id}`); // equivalente a .then() pero secuencial y legible
  return response.json();
}
```

Una función `async` **siempre devuelve una Promise**, incluso si el `return` dentro es un valor simple — `await` internamente es sintaxis para "esperar a que esta Promise se resuelva y desenvolver su valor", pero el motor sigue trabajando con Promises por debajo.

## Microtask queue: cuándo se ejecuta un `.then()` exactamente

```javascript
console.log('1');
Promise.resolve().then(() => console.log('2'));
console.log('3');
// Orden de salida: 1, 3, 2
```

El callback de `.then()` nunca se ejecuta de forma síncrona, ni siquiera si la Promise ya está resuelta en el momento de llamar a `.then()` — siempre se encola en la microtask queue, que se procesa después de que el código síncrono actual termine. Ver [`../node/event-loop.md`](../node/event-loop.md) para el detalle completo de cómo esto se integra con las fases del Event Loop en Node.js.

## Checklist

- [ ] ¿Se entiende que una Promise solo transiciona de estado una vez, de forma irreversible?
- [ ] ¿Se ha elegido el combinador correcto (`all` vs. `allSettled`) según si los fallos individuales deben abortar el resto?
- [ ] ¿Se entiende que el callback de `.then()`/`await` nunca se ejecuta síncronamente, siempre vía microtask queue?

Ver también: [`../node/async-patterns.md`](../node/async-patterns.md), [`../node/event-loop.md`](../node/event-loop.md).
