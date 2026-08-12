# Recipe: Crear un Cliente Fetch Robusto (JavaScript)

Aplica: [`../knowledge/javascript/fetch.md`](../knowledge/javascript/fetch.md), [`../knowledge/javascript/promises.md`](../knowledge/javascript/promises.md), [`../standards/api.md`](../standards/api.md).

## Pasos

### 1. Definir el wrapper base con verificación de `response.ok`

```javascript
async function clienteFetch(url, opciones = {}) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), opciones.timeoutMs ?? 5000);

  try {
    const response = await fetch(url, { ...opciones, signal: controller.signal });
    clearTimeout(timeoutId);

    if (!response.ok) {
      const errorBody = await response.json().catch(() => null);
      throw new ErrorHttp(response.status, errorBody?.error?.message ?? `Error HTTP ${response.status}`, errorBody);
    }

    return response.status === 204 ? null : response.json(); // 204 No Content no tiene body
  } catch (error) {
    clearTimeout(timeoutId);
    if (error.name === 'AbortError') throw new ErrorTimeout(url);
    throw error;
  }
}
```

### 2. Definir tipos de error específicos

```javascript
class ErrorHttp extends Error {
  constructor(status, message, body) {
    super(message);
    this.name = 'ErrorHttp';
    this.status = status;
    this.body = body;
  }
}

class ErrorTimeout extends Error {
  constructor(url) {
    super(`Timeout al llamar a ${url}`);
    this.name = 'ErrorTimeout';
  }
}
```

### 3. Añadir reintentos solo para errores transitorios

```javascript
async function clienteFetchConReintentos(url, opciones = {}, maxIntentos = 3) {
  for (let intento = 1; intento <= maxIntentos; intento++) {
    try {
      return await clienteFetch(url, opciones);
    } catch (error) {
      const esTransitorio = error instanceof ErrorTimeout || (error instanceof ErrorHttp && error.status >= 500);
      if (!esTransitorio || intento === maxIntentos) throw error;
      await new Promise(r => setTimeout(r, 2 ** intento * 100));
    }
  }
}
```

Ver [`../standards/exceptions.md`](../standards/exceptions.md) — nunca reintentar un `ErrorHttp` 4xx, que no va a cambiar en un reintento idéntico.

### 4. Uso en un componente/módulo

```javascript
async function cargarPedido(id) {
  try {
    return await clienteFetchConReintentos(`/api/v1/pedidos/${id}`);
  } catch (error) {
    if (error instanceof ErrorHttp && error.status === 404) {
      mostrarMensaje('Pedido no encontrado');
      return null;
    }
    logger.error('Error cargando pedido', error);
    throw error;
  }
}
```

### 5. Test

```javascript
test('lanza ErrorHttp con el status correcto ante una respuesta 404', async () => {
  global.fetch = jest.fn().mockResolvedValue({
    ok: false, status: 404, json: () => Promise.resolve({ error: { message: 'No encontrado' } }),
  });

  await expect(clienteFetch('/api/v1/pedidos/999')).rejects.toThrow(ErrorHttp);
});
```

## Checklist final

- [ ] ¿Se verifica `response.ok` explícitamente, sin asumir que `fetch` rechaza ante 4xx/5xx?
- [ ] ¿Existe timeout explícito vía `AbortController`?
- [ ] ¿Los reintentos excluyen errores 4xx?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/javascript/fetch-client.js`](../snippets/javascript/fetch-client.js).
