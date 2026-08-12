# Async/Await

> Ver [`promises.md`](./promises.md) para el mecanismo subyacente que `async/await` envuelve, y [`../node/async-patterns.md`](../node/async-patterns.md) para patrones de aplicación en backend.

## Reglas básicas de sintaxis

```javascript
async function obtenerDatos() { // "async" convierte la función en generadora de Promise
  const resultado = await operacionAsincrona(); // "await" solo válido dentro de una función async
  return resultado; // se envuelve automáticamente en una Promise resuelta
}

const arrowAsync = async () => { /* también válido en arrow functions */ };
```

`await` de nivel superior (*top-level await*, fuera de cualquier función) es válido en módulos ES (ver [`modules.md`](./modules.md)), pero no en scripts clásicos ni en el cuerpo de una función no-async.

## Manejo de errores: `try/catch` reemplaza a `.catch()`

```javascript
async function confirmarPedido(id) {
  try {
    const pedido = await pedidoRepository.buscarPorId(id);
    if (!pedido) throw new Error('Pedido no encontrado');
    pedido.confirmar();
    await pedidoRepository.guardar(pedido);
  } catch (error) {
    // captura TANTO errores lanzados con throw como rechazos de cualquier await anterior
    logger.error('Error confirmando pedido', error);
    throw error;
  }
}
```

Una excepción lanzada con `throw` dentro de una función `async`, o el rechazo de cualquier Promise `await`-eada, terminan ambos en el mismo bloque `catch` — no hay distinción sintáctica entre "error lanzado" y "Promise rechazada" desde la perspectiva de quien consume la función async.

## El error de "olvidar" el `await`

```javascript
async function procesar() {
  const resultado = operacionAsincrona(); // FALTA await — resultado es una Promise, no el valor
  console.log(resultado); // imprime "Promise { <pending> }", no el valor esperado
}
```

Un `await` olvidado no siempre produce un error visible inmediatamente — el código sigue "funcionando" pero operando sobre el objeto Promise en lugar del valor resuelto, produciendo bugs silenciosos que solo se manifiestan más adelante cuando se intenta usar ese valor como si fuera el dato real.

## Iterar de forma asíncrona secuencial vs. paralela

```javascript
// Secuencial (uno espera al anterior) — solo si hay dependencia real entre iteraciones
for (const pedido of pedidos) {
  await procesarPedido(pedido);
}

// Paralelo — cuando las iteraciones son independientes entre sí
await Promise.all(pedidos.map(pedido => procesarPedido(pedido)));
```

`array.forEach(async (item) => await ...)` **no espera** a que las promesas se resuelvan — `forEach` no está diseñado para funciones async y no captura ni compone las Promises devueltas por cada callback; usar un `for...of` con `await` (secuencial) o `Promise.all(array.map(...))` (paralelo) en su lugar.

## `await` en un bucle sin necesidad: antipatrón de rendimiento

```javascript
// Lento: cada llamada espera a la anterior, aunque no hay dependencia entre ellas
for (const id of ids) {
  const dato = await obtenerDato(id); // serializa innecesariamente N llamadas de red
  resultados.push(dato);
}

// Correcto cuando son independientes
const resultados = await Promise.all(ids.map(id => obtenerDato(id)));
```

Ver [`../node/async-patterns.md`](../node/async-patterns.md) para el mismo principio aplicado con más detalle en contexto de backend.

## Checklist

- [ ] ¿Todo `await` está dentro de una función `async` (o top-level await en un módulo ES)?
- [ ] ¿El manejo de errores usa `try/catch` alrededor de los `await`, capturando tanto `throw` como rechazos?
- [ ] ¿Se ha evitado el error de "olvidar" el `await` en una llamada que devuelve una Promise?
- [ ] ¿Las iteraciones independientes usan `Promise.all` en lugar de un bucle secuencial innecesario?
- [ ] ¿Se ha evitado usar `await` dentro de `array.forEach` (no funciona como se espera)?

Ver también: [`promises.md`](./promises.md), [`../node/async-patterns.md`](../node/async-patterns.md), [`../../anti-patterns/callback-hell.md`](../../anti-patterns/callback-hell.md).
