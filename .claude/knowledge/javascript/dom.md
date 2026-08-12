# DOM (Document Object Model)

> Específico del entorno navegador — sin equivalente en Node.js. Ver [`web-apis.md`](./web-apis.md) para otras APIs del navegador más allá de la manipulación del documento.

## Selección de elementos

```javascript
document.getElementById('pedido-123'); // el más rápido, por índice interno del navegador
document.querySelector('.pedido.confirmado'); // cualquier selector CSS, el primero que coincide
document.querySelectorAll('.pedido'); // NodeList estático de todos los que coinciden
```

`querySelectorAll` devuelve un `NodeList` **estático** (una foto del DOM en ese momento) — a diferencia de `getElementsByClassName`, que devuelve una colección **viva** que se actualiza automáticamente si el DOM cambia después. Esta diferencia es una fuente sutil de bugs si se asume el comportamiento contrario al real.

## Manipulación eficiente: minimizar reflow/repaint

```javascript
// Ineficiente: cada modificación puede forzar un recálculo de layout del navegador
for (const pedido of pedidos) {
  const li = document.createElement('li');
  li.textContent = pedido.id;
  lista.appendChild(li); // inserción una a una, reflow potencial en cada iteración
}

// Eficiente: construir fuera del DOM, insertar una única vez
const fragmento = document.createDocumentFragment();
for (const pedido of pedidos) {
  const li = document.createElement('li');
  li.textContent = pedido.id;
  fragmento.appendChild(li); // no toca el DOM real todavía
}
lista.appendChild(fragmento); // una única inserción real, un único reflow
```

`DocumentFragment` permite construir una estructura completa en memoria antes de insertarla de una vez en el DOM real — evita forzar múltiples recálculos de layout/pintado del navegador durante la construcción.

## Delegación de eventos

```javascript
// Ineficiente: un listener por cada elemento, y no cubre elementos añadidos después
document.querySelectorAll('.pedido-item').forEach(el => {
  el.addEventListener('click', manejarClick);
});

// Eficiente: un único listener en el contenedor padre, usando bubbling
listaContenedor.addEventListener('click', (evento) => {
  const item = evento.target.closest('.pedido-item');
  if (item) manejarClick(item);
});
```

La delegación de eventos aprovecha que los eventos DOM "burbujean" (*bubble*) desde el elemento origen hasta sus ancestros — un único listener en el contenedor gestiona clics en cualquier elemento hijo actual **o futuro**, sin necesidad de re-registrar listeners cada vez que se añaden elementos dinámicamente.

## `addEventListener` vs. propiedades `on*`

```javascript
elemento.onclick = handler1;
elemento.onclick = handler2; // SOBREESCRIBE handler1, solo puede haber uno

elemento.addEventListener('click', handler1);
elemento.addEventListener('click', handler2); // AMBOS se ejecutan, sin sobreescribir
```

`addEventListener` es siempre preferible — permite múltiples listeners independientes y opciones adicionales (`{ once: true }`, captura vs. bubbling).

## Limpieza de listeners: evitar fugas de memoria

```javascript
function montarComponente() {
  const handler = () => actualizar();
  window.addEventListener('resize', handler);

  return function desmontar() {
    window.removeEventListener('resize', handler); // misma referencia de función, obligatorio
  };
}
```

Ver [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md) — un listener en `window`/`document` registrado por un componente que luego se destruye (en una SPA con enrutamiento del lado del cliente) debe des-registrarse explícitamente al desmontar, o el componente completo (y todo lo que su closure retiene) queda retenido en memoria indefinidamente.

## Checklist

- [ ] ¿Las inserciones múltiples al DOM usan `DocumentFragment` en lugar de insertar una a una?
- [ ] ¿Los listeners en colecciones dinámicas usan delegación de eventos en lugar de un listener por elemento?
- [ ] ¿Todo listener registrado en `window`/`document` tiene su `removeEventListener` correspondiente al desmontar el componente?

Ver también: [`web-apis.md`](./web-apis.md), [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md).
