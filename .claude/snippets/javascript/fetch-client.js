// snippets/javascript/fetch-client.js
// Cliente fetch con timeout, verificación de response.ok y reintentos — ver recipes/js-create-fetch-client.md.

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

    return response.status === 204 ? null : response.json();
  } catch (error) {
    clearTimeout(timeoutId);
    if (error.name === 'AbortError') throw new ErrorTimeout(url);
    throw error;
  }
}

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

export { clienteFetch, clienteFetchConReintentos, ErrorHttp, ErrorTimeout };
