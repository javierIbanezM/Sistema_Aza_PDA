# Examples (JavaScript)

Casos de uso completos combinando varios documentos de este dominio.

---

## Ejemplo 1 — Formulario con validación, fetch y manejo de errores robusto

**Contexto**: formulario de creación de pedido en el navegador, sin framework de UI.

**Flujo aplicado**:

1. Captura del submit, previniendo el comportamiento nativo del formulario.
2. Construcción del body y llamada con `fetch`, verificando `response.ok` explícitamente — ver [`fetch.md`](./fetch.md).
3. Timeout con `AbortController` para evitar que el usuario espere indefinidamente ante un servidor lento.
4. Manejo de errores diferenciado: error de validación (400, mostrar mensajes por campo) vs. error de red (reintentar o informar).
5. Deshabilitar el botón de submit durante la petición (evitar doble envío), usando `debounce` si el formulario tiene autoguardado — ver [`performance.md`](./performance.md).

---

## Ejemplo 2 — Diagnosticar y corregir una fuga de memoria en una SPA

**Contexto**: una aplicación de página única (sin recarga completa) acumula memoria progresivamente al navegar entre vistas.

**Flujo aplicado**:

1. Se toman dos heap snapshots en Chrome DevTools tras navegar repetidamente entre dos vistas — ver [`debugging.md`](./debugging.md).
2. Se identifica un tipo de objeto (componente de vista) que crece en número sin caer de vuelta al estado esperado.
3. Se rastrea la cadena de referencias retenidas (*retainers*) hasta encontrar un listener de `window.resize` registrado en el montaje de la vista, sin `removeEventListener` en el desmontaje — ver [`dom.md`](./dom.md) y [`memory-management.md`](./memory-management.md).
4. Se corrige añadiendo la limpieza simétrica; se repite el mismo proceso de snapshots para confirmar que el crecimiento se detiene.

---

## Ejemplo 3 — Migrar un módulo de callbacks a async/await con Promises

**Contexto**: un módulo antiguo de carga de datos usa callbacks anidados de 3 niveles.

**Flujo aplicado** (siguiendo [`../../prompts/refactor.md`](../../prompts/refactor.md)):

1. Se identifica el anti-patrón [Callback Hell](../../anti-patterns/callback-hell.md).
2. Se envuelve cada función basada en callback en una Promise explícita — ver [`promises.md`](./promises.md).
3. Se reescribe el flujo con `async/await`, centralizando el manejo de errores en un único `try/catch` — ver [`async-await.md`](./async-await.md).
4. Se identifican llamadas independientes que se ejecutaban secuencialmente sin necesidad, paralelizándolas con `Promise.all`.

---

## Ejemplo 4 — Optimizar el renderizado de una lista grande

**Contexto**: una lista de 5.000 elementos se renderiza lentamente y hace que el scroll se sienta poco fluido.

**Flujo aplicado**:

1. Se graba una sesión en el panel Performance de Chrome DevTools mientras se reproduce el scroll lento — ver [`debugging.md`](./debugging.md).
2. Se identifica tiempo excesivo en "Recalculate Style"/"Layout" — señal de layout thrashing o de renderizar todos los elementos de golpe.
3. Se aplica virtualización (renderizar solo los elementos visibles en el viewport + un margen, usando `IntersectionObserver` para detectar cuándo cargar más) — ver [`web-apis.md`](./web-apis.md).
4. Se sustituye cualquier alternancia de lectura/escritura de estilos dentro del bucle de renderizado por batching explícito — ver [`performance.md`](./performance.md).
5. Se vuelve a grabar el panel Performance con la misma metodología para confirmar la mejora medida, no solo percibida.
