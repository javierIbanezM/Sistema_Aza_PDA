# Performance (JavaScript / Navegador)

> Ver [`../node/performance.md`](../node/performance.md) para el rendimiento del lado servidor (Worker Threads, clustering). Este documento cubre optimización a nivel de lenguaje/motor y específicamente del navegador.

## Hidden Classes y optimización JIT (V8)

```javascript
// Favorece la optimización: siempre se crean los objetos con la misma "forma"
function crearPunto(x, y) { return { x, y }; } // V8 puede reutilizar la misma "hidden class" internamente

// Dificulta la optimización: formas de objeto inconsistentes
function crearPuntoInconsistente(x, y, conZ) {
  const punto = { x, y };
  if (conZ) punto.z = 0; // añadir propiedades condicionalmente crea una "hidden class" distinta
  return punto;
}
```

V8 optimiza el acceso a propiedades asumiendo que objetos creados de forma similar tienen la misma estructura interna ("hidden class") — código que crea objetos con propiedades añadidas en distinto orden o condicionalmente impide que V8 aplique esta optimización, cayendo a un modo de acceso más lento (modo diccionario).

## Evitar deoptimización: tipos consistentes

```javascript
// Favorece optimización: el array siempre contiene el mismo tipo
const numeros = [1, 2, 3, 4]; // "array de enteros", V8 puede optimizar operaciones sobre él

// Dificulta optimización: tipos mixtos fuerzan una representación interna más genérica y lenta
const mixto = [1, 'dos', 3, null];
```

## Renderizado del navegador: minimizar reflow/repaint

```javascript
// Antipatrón: leer y escribir estilos intercalado fuerza recálculos de layout repetidos
elementos.forEach(el => {
  el.style.width = el.offsetWidth + 10 + 'px'; // leer (offsetWidth) fuerza layout, luego escribir invalida
});

// Correcto: separar todas las lecturas de todas las escrituras (batching)
const anchos = elementos.map(el => el.offsetWidth); // todas las lecturas primero
elementos.forEach((el, i) => { el.style.width = anchos[i] + 10 + 'px'; }); // luego todas las escrituras
```

Alternar lectura/escritura de propiedades que afectan al layout (`offsetWidth`, `getBoundingClientRect`) fuerza al navegador a recalcular el layout de forma síncrona en cada iteración ("layout thrashing") — agrupar todas las lecturas antes que todas las escrituras evita este coste.

## `debounce` y `throttle` para eventos de alta frecuencia

```javascript
function debounce(fn, delayMs) {
  let timeoutId;
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn(...args), delayMs); // solo ejecuta tras "silencio" de delayMs
  };
}

const buscarConDebounce = debounce(buscarEnServidor, 300);
inputBusqueda.addEventListener('input', (e) => buscarConDebounce(e.target.value));
```

Eventos como `scroll`, `resize` o `input` pueden dispararse decenas de veces por segundo — `debounce` (ejecuta solo tras un periodo de inactividad) y `throttle` (ejecuta como máximo una vez cada cierto intervalo) evitan sobrecargar el hilo principal ejecutando un handler costoso en cada disparo.

## Lazy loading de imágenes y código

```html
<img src="placeholder.jpg" data-src="imagen-real.jpg" loading="lazy"> <!-- nativo del navegador -->
```

```javascript
const modulo = await import('./funcionalidad-pesada.js'); // code splitting, ver modules.md
```

## Checklist

- [ ] ¿Los objetos con la misma estructura se crean de forma consistente (mismas propiedades, mismo orden)?
- [ ] ¿Se ha evitado alternar lectura/escritura de propiedades de layout en un bucle (layout thrashing)?
- [ ] ¿Los eventos de alta frecuencia (`scroll`, `input`, `resize`) usan `debounce`/`throttle`?
- [ ] ¿Las imágenes fuera del viewport inicial usan `loading="lazy"` o `IntersectionObserver`?

Ver también: [`../node/performance.md`](../node/performance.md), [`memory-management.md`](./memory-management.md), [`dom.md`](./dom.md).
