# Pitfalls (JavaScript — lenguaje/navegador) — Errores frecuentes

> Errores específicos del lenguaje JavaScript y el entorno navegador. Para trampas específicas de Node.js, ver [`../node/pitfalls.md`](../node/pitfalls.md).

## `var` en un bucle con closures asíncronos

```javascript
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100); // imprime 3, 3, 3 — no 0, 1, 2
}
```

Ver [`closures.md`](./closures.md) — usar `let`, que crea un binding nuevo por iteración.

## `==` en lugar de `===`

```javascript
if (valor == '0') { } // '0' == 0 == false == '' -- todas true entre sí, fuente de bugs
```

Ver [`language.md`](./language.md).

## Olvidar `await`

```javascript
const datos = obtenerDatos(); // falta await — datos es una Promise, no el valor
console.log(datos.length); // undefined, sin error visible inmediato
```

Ver [`async-await.md`](./async-await.md).

## `await` dentro de `Array.forEach`

```javascript
pedidos.forEach(async (p) => await procesar(p)); // NO espera a que termine — forEach ignora la Promise devuelta
```

Ver [`async-await.md`](./async-await.md) — usar `for...of` (secuencial) o `Promise.all(array.map(...))` (paralelo).

## Asumir que `fetch` rechaza ante un 404/500

```javascript
try {
  const r = await fetch('/api/pedidos/999');
  const datos = await r.json(); // no lanza excepción aunque sea un 404
} catch { /* nunca se ejecuta para errores HTTP, solo errores de red */ }
```

Ver [`fetch.md`](./fetch.md) — verificar `response.ok` explícitamente.

## Modificar prototipos nativos

```javascript
Array.prototype.ultimo = function () { return this[this.length - 1]; }; // afecta a TODO el código
```

Ver [`prototypes.md`](./prototypes.md).

## Layout thrashing por alternar lectura/escritura de estilos

```javascript
elementos.forEach(el => { el.style.width = el.offsetWidth + 10 + 'px'; }); // fuerza reflow en cada iteración
```

Ver [`performance.md`](./performance.md).

## Checklist de revisión rápida

- [ ] ¿Hay algún `var` en un bucle con closures asíncronos?
- [ ] ¿Hay algún `==` sin justificar frente a `===`?
- [ ] ¿Se ha verificado `response.ok` en cada uso de `fetch`?
- [ ] ¿Hay algún `await` dentro de `Array.forEach`?

Ver también: [`best-practices.md`](./best-practices.md), [`../../anti-patterns/`](../../anti-patterns/).
