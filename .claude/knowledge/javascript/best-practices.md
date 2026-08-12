# Best Practices (JavaScript — lenguaje/navegador)

> Síntesis del lenguaje JavaScript puro y el entorno navegador. Para Node.js como runtime de servidor, ver [`../node/best-practices.md`](../node/best-practices.md) — no se duplica aquí.

## Lenguaje

- `const` por defecto, `let` solo con reasignación real, nunca `var` en código nuevo (ver [`language.md`](./language.md)).
- `===` siempre, salvo `== null` como excepción idiomática explícita.
- Entender `this` según cómo se llama la función, no dónde se define — usar arrow functions cuando se necesita capturar el `this` léxico.

## Asincronía

- `async/await` como estándar, `try/catch` para manejo de errores (ver [`async-await.md`](./async-await.md)).
- `Promise.all`/`allSettled` para paralelizar operaciones independientes, nunca `await` secuencial innecesario.
- Entender que microtasks (Promises) siempre se ejecutan antes que macrotasks (`setTimeout`), incluso con delay 0 (ver [`event-loop.md`](./event-loop.md)).

## Memoria

- Cachés en memoria con límite/TTL explícito, nunca estructuras que crecen indefinidamente (ver [`memory-management.md`](./memory-management.md)).
- Todo listener registrado en `window`/`document` con su `removeEventListener` correspondiente al desmontar.
- Cuidado con closures que retienen ámbitos completos en listeners de larga duración (ver [`closures.md`](./closures.md)).

## DOM y rendimiento

- `DocumentFragment` para inserciones múltiples, delegación de eventos para colecciones dinámicas (ver [`dom.md`](./dom.md)).
- `debounce`/`throttle` en eventos de alta frecuencia (ver [`performance.md`](./performance.md)).
- Separar lecturas y escrituras de propiedades de layout para evitar layout thrashing.

## Red

- Verificar `response.ok` explícitamente con `fetch` — nunca asumir que rechaza ante 4xx/5xx (ver [`fetch.md`](./fetch.md)).
- Timeout explícito con `AbortController` en peticiones críticas.
- Reintentos automáticos solo para errores transitorios (5xx/red), nunca para 4xx.

Ver [`pitfalls.md`](./pitfalls.md) para errores frecuentes, y [`examples.md`](./examples.md) para casos de uso completos.
