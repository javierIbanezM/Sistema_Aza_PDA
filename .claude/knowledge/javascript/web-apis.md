# Web APIs

> APIs del navegador más allá del DOM ([`dom.md`](./dom.md)) y `fetch` ([`fetch.md`](./fetch.md)). Todas específicas del entorno navegador, sin equivalente en Node.js.

## `localStorage` / `sessionStorage`: persistencia simple del lado cliente

```javascript
localStorage.setItem('tema', 'oscuro'); // persiste entre sesiones, hasta que se borre explícitamente
sessionStorage.setItem('borrador-pedido', JSON.stringify(borrador)); // solo dura la pestaña actual

const tema = localStorage.getItem('tema'); // siempre devuelve string o null — sin serialización automática
```

Ambos son **síncronos** (bloquean el hilo principal) y tienen un límite de tamaño (~5-10MB según navegador) — nunca almacenar datos sensibles (tokens, contraseñas) sin cifrar, ya que son accesibles por cualquier script que se ejecute en el mismo origen (superficie de ataque XSS).

## `IndexedDB`: almacenamiento estructurado del lado cliente

```javascript
const db = await new Promise((resolve, reject) => {
  const request = indexedDB.open('PedidosDB', 1);
  request.onupgradeneeded = (evento) => {
    evento.target.result.createObjectStore('pedidos', { keyPath: 'id' });
  };
  request.onsuccess = () => resolve(request.result);
  request.onerror = () => reject(request.error);
});
```

API asíncrona (a diferencia de `localStorage`), apropiada para volúmenes de datos mayores o cuando se necesita indexación/consultas — la API nativa es verbosa; en proyectos reales suele envolverse con una librería (`idb`) que la hace más ergonómica.

## `WebSocket`: comunicación bidireccional en tiempo real

```javascript
const socket = new WebSocket('wss://api.ejemplo.com/pedidos');

socket.addEventListener('open', () => socket.send(JSON.stringify({ tipo: 'suscribir', canal: 'pedidos' })));
socket.addEventListener('message', (evento) => manejarMensaje(JSON.parse(evento.data)));
socket.addEventListener('close', () => intentarReconexion());
socket.addEventListener('error', (error) => logger.error('Error de WebSocket', error));
```

Ver [`../../recipes/node-create-websocket.md`](../../recipes/node-create-websocket.md) para el lado servidor de esta misma comunicación — el cliente debe manejar reconexión ante `close` inesperado, ya que las conexiones de red no son perfectamente estables.

## `IntersectionObserver`: detectar visibilidad sin polling

```javascript
const observer = new IntersectionObserver((entradas) => {
  entradas.forEach(entrada => {
    if (entrada.isIntersecting) cargarImagenPerezosa(entrada.target); // lazy loading
  });
});

document.querySelectorAll('img[data-src]').forEach(img => observer.observe(img));
```

Reemplaza el antipatrón histórico de escuchar el evento `scroll` y calcular manualmente si un elemento es visible (costoso, se dispara con muchísima frecuencia) — `IntersectionObserver` es nativamente eficiente, gestionado por el navegador de forma asíncrona.

## `Web Workers`: paralelismo real en el navegador

```javascript
// worker.js
self.onmessage = (evento) => {
  const resultado = calculoCostoso(evento.data);
  self.postMessage(resultado);
};

// main.js
const worker = new Worker('worker.js');
worker.postMessage(datos);
worker.onmessage = (evento) => actualizarUI(evento.data);
```

Equivalente conceptual del navegador a los Worker Threads de Node.js (ver [`../node/performance.md`](../node/performance.md)) — mueve cálculo CPU-bound fuera del hilo principal, evitando congelar la interfaz de usuario.

## `Notification` API

```javascript
if (Notification.permission === 'granted') {
  new Notification('Pedido confirmado', { body: 'Tu pedido #123 ha sido confirmado' });
} else if (Notification.permission !== 'denied') {
  await Notification.requestPermission(); // requiere interacción del usuario para solicitarse
}
```

## Checklist

- [ ] ¿Se ha evitado almacenar datos sensibles en `localStorage`/`sessionStorage` sin cifrar?
- [ ] ¿Las conexiones `WebSocket` manejan reconexión ante cierre inesperado?
- [ ] ¿La detección de visibilidad usa `IntersectionObserver` en lugar de escuchar `scroll` manualmente?
- [ ] ¿El cálculo CPU-bound significativo en el navegador se mueve a un Web Worker?

Ver también: [`dom.md`](./dom.md), [`fetch.md`](./fetch.md), [`memory-management.md`](./memory-management.md).
