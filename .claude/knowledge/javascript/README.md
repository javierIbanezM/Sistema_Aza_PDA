# Knowledge — JavaScript

Índice del **lenguaje JavaScript y el entorno navegador**. Node.js (runtime de servidor) tiene su propia carpeta en [`../node/`](../node/) — donde un tema coincide conceptualmente (async/await, promises, event loop, módulos, performance, debugging), este dominio cubre la mecánica del lenguaje/navegador y **enlaza** a `node/` para la aplicación en servidor, sin duplicarla.

| Documento | Descripción |
|---|---|
| [`language.md`](./language.md) | `var`/`let`/`const`, `===` vs `==`, `this`, hoisting, spread/destructuring |
| [`closures.md`](./closures.md) | Mecanismo, uso idiomático, el error clásico en bucles, relación con fugas de memoria |
| [`prototypes.md`](./prototypes.md) | Herencia prototípica, `class` como azúcar sintáctico, cadena de prototipos |
| [`promises.md`](./promises.md) | Los tres estados, encadenamiento, combinadores, microtask queue |
| [`async-await.md`](./async-await.md) | Sintaxis, manejo de errores, errores comunes de paralelización |
| [`event-loop.md`](./event-loop.md) | Modelo de navegador (Call Stack + Web APIs + colas) — distinto del modelo de Node |
| [`modules.md`](./modules.md) | ES Modules nativos del navegador, import dinámico, import maps, tree-shaking |
| [`dom.md`](./dom.md) | Selección eficiente, `DocumentFragment`, delegación de eventos, limpieza de listeners |
| [`fetch.md`](./fetch.md) | Uso correcto, `AbortController`, reintentos, streaming de respuestas |
| [`web-apis.md`](./web-apis.md) | `localStorage`, `IndexedDB`, `WebSocket`, `IntersectionObserver`, `Web Workers` |
| [`memory-management.md`](./memory-management.md) | Recolección generacional de V8, alcanzabilidad, `WeakMap`/`WeakSet` |
| [`performance.md`](./performance.md) | Hidden classes, layout thrashing, debounce/throttle, lazy loading |
| [`debugging.md`](./debugging.md) | Chrome DevTools: breakpoints, console avanzado, panel Network/Performance/Memory |
| [`best-practices.md`](./best-practices.md) | Síntesis de las reglas más importantes del lenguaje/navegador |
| [`pitfalls.md`](./pitfalls.md) | Errores frecuentes específicos de JavaScript/navegador |
| [`examples.md`](./examples.md) | Casos de uso completos |

## Dominio relacionado (no duplicado aquí)

| Tecnología | Carpeta |
|---|---|
| Node.js (runtime de servidor) | [`../node/`](../node/) |

## Cómo usar este índice

- Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí ni en [`../node/`](../node/) (regla de oro nº2).
- Si el concepto es transversal a cualquier lenguaje (SOLID, DDD, Clean Architecture, testing en general), no lo dupliques aquí — está en [`../architecture/`](../architecture/).
