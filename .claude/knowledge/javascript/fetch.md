# Fetch API

> Ver [`promises.md`](./promises.md) para la mecánica de Promises que `fetch` usa como base, y [`../../standards/api.md`](../../standards/api.md) para las convenciones del lado servidor que un cliente `fetch` bien escrito debe respetar (códigos de estado, formato de error).

## Uso básico y el error más común

```javascript
// Antipatrón: fetch NO rechaza la Promise ante un 404/500 — solo ante fallos de red
const response = await fetch('/api/pedidos/123');
const datos = await response.json(); // si el servidor devolvió un 404, esto puede parsear el body de error como si fuera éxito

// Correcto: verificar response.ok explícitamente
const response = await fetch('/api/pedidos/123');
if (!response.ok) {
  const error = await response.json().catch(() => null); // el body de error puede no ser JSON válido
  throw new Error(error?.error?.message || `Error HTTP ${response.status}`);
}
const datos = await response.json();
```

`fetch` solo rechaza su Promise ante errores de **red** (sin conexión, DNS no resuelto) — un `404 Not Found` o `500 Internal Server Error` es una respuesta HTTP válida desde la perspectiva de `fetch`, y hay que comprobar `response.ok` (o `response.status`) explícitamente para tratarlo como error.

## Configuración de la petición

```javascript
const response = await fetch('/api/pedidos', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  },
  body: JSON.stringify({ clienteId, lineas }),
});
```

## Cancelación con `AbortController`

```javascript
const controller = new AbortController();
const timeoutId = setTimeout(() => controller.abort(), 5000); // timeout manual, fetch no tiene uno nativo

try {
  const response = await fetch('/api/pedidos', { signal: controller.signal });
  clearTimeout(timeoutId);
} catch (error) {
  if (error.name === 'AbortError') {
    console.log('Petición cancelada por timeout');
  }
}
```

`fetch` no soporta timeout nativamente — `AbortController` es el mecanismo estándar tanto para timeouts manuales como para cancelar una petición en curso si el usuario navega fuera de la página o cancela una acción explícitamente.

## Reintentos con backoff exponencial

```javascript
async function fetchConReintentos(url, opciones, maxIntentos = 3) {
  for (let intento = 1; intento <= maxIntentos; intento++) {
    try {
      const response = await fetch(url, opciones);
      if (response.ok) return response;
      if (response.status < 500) return response; // no reintentar errores de cliente (4xx)
    } catch (error) {
      if (intento === maxIntentos) throw error;
    }
    await new Promise(r => setTimeout(r, 2 ** intento * 100)); // backoff exponencial
  }
}
```

Solo reintentar errores potencialmente transitorios (5xx, fallos de red) — nunca reintentar un `400`/`404`/`422`, que representan un error del propio request y no van a cambiar en un reintento idéntico (ver [`../../standards/exceptions.md`](../../standards/exceptions.md), distinción entre errores transitorios y permanentes).

## Streaming de respuestas grandes

```javascript
const response = await fetch('/api/export/pedidos.csv');
const reader = response.body.getReader();
const decoder = new TextDecoder();

while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  procesarChunk(decoder.decode(value, { stream: true }));
}
```

Ver [`../node/streams.md`](../node/streams.md) para el equivalente del lado servidor — procesar la respuesta por chunks evita cargar una respuesta grande completa en memoria antes de empezar a procesarla.

## Checklist

- [ ] ¿Se verifica `response.ok` explícitamente, sin asumir que `fetch` rechaza ante un 404/500?
- [ ] ¿Las peticiones críticas tienen timeout explícito vía `AbortController`?
- [ ] ¿Los reintentos automáticos excluyen errores 4xx (no transitorios)?

Ver también: [`promises.md`](./promises.md), [`web-apis.md`](./web-apis.md), [`../../standards/api.md`](../../standards/api.md).
