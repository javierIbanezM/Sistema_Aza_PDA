# Debugging (Node.js)

> Ver [`../architecture/debugging.md`](../architecture/debugging.md) para la metodología sistemática completa. Este documento cubre herramientas específicas de Node.js.

## Inspector nativo con Chrome DevTools

```bash
node --inspect-brk app.js  # --inspect-brk pausa en la primera línea, esperando el debugger
```

Abrir `chrome://inspect` en Chrome, conectar al proceso — permite breakpoints, inspección de variables, y el Profiler de rendimiento (ver [`performance.md`](./performance.md)) sobre el proceso real en ejecución.

## Depuración remota de un proceso en contenedor/staging

```bash
node --inspect=0.0.0.0:9229 app.js
```

```bash
# En el contenedor/servidor, exponer el puerto 9229; en local, hacer forward si es necesario
kubectl port-forward pod/mi-app 9229:9229
```

Igual que en Java (ver [`../java/debugging.md`](../java/debugging.md)), permite investigar un fallo que solo se reproduce con datos/configuración presentes en un entorno específico, sin depender únicamente de reproducirlo en local.

## Leer un stack trace de Node.js

```
TypeError: Cannot read properties of undefined (reading 'email')
    at NotificarClienteUseCase.ejecutar (/app/src/application/notificar-cliente.ts:15:23)
    at async PedidoController.confirmar (/app/src/interface/pedido.controller.ts:28:5)
```

Igual que en Java: leer de arriba hacia abajo, el primer frame es el punto exacto del fallo. `async` en el frame indica que ese punto de la pila corresponde a una función asíncrona — el resto de la cadena "por encima" de una función async puede no reflejar el orden real de ejecución en tiempo real, ya que el Event Loop intercala trabajo entre awaits.

## `console.trace()` para localizar el origen de una llamada

```typescript
function funcionSospechosa() {
  console.trace('Llamada a funcionSospechosa'); // imprime el stack trace SIN lanzar una excepción
}
```

Útil cuando una función se llama desde múltiples lugares y hay que identificar cuál de ellos produce un valor inesperado, sin necesidad de un breakpoint interactivo.

## Depurar Promises no resueltas / colgadas

```typescript
// Añadir timeout explícito para diagnosticar si una Promise nunca resuelve ni rechaza
function conTimeout<T>(promesa: Promise<T>, ms: number): Promise<T> {
  return Promise.race([
    promesa,
    new Promise<T>((_, reject) => setTimeout(() => reject(new Error(`Timeout tras ${ms}ms`)), ms)),
  ]);
}

const resultado = await conTimeout(operacionSospechosa(), 5000);
```

Una Promise que nunca resuelve ni rechaza (ej. una conexión de red sin timeout configurado) cuelga silenciosamente el flujo — envolver con un timeout explícito durante el diagnóstico ayuda a confirmar la hipótesis y localizar el punto exacto que nunca completa.

## `AsyncLocalStorage` para depurar contexto perdido entre llamadas asíncronas

```typescript
import { AsyncLocalStorage } from 'async_hooks';

const contextoPeticion = new AsyncLocalStorage<{ traceId: string }>();

app.use((req, res, next) => {
  contextoPeticion.run({ traceId: crypto.randomUUID() }, next);
});

function logConTraceId(mensaje: string) {
  const contexto = contextoPeticion.getStore();
  logger.info(mensaje, { traceId: contexto?.traceId }); // disponible en CUALQUIER punto async posterior, sin pasarlo manualmente
}
```

Ver [`../../standards/logging-standard.md`](../../standards/logging-standard.md) — `AsyncLocalStorage` es el equivalente de Node.js al `MDC` de Java para propagar contexto (trace-id) a través de código asíncrono sin pasarlo explícitamente por cada función.

## `git bisect` combinado con tests

```bash
git bisect start
git bisect bad
git bisect good v2.3.0
git bisect run npm test -- --grep "confirmar pedido"
```

Ver [`../architecture/debugging.md`](../architecture/debugging.md) para el razonamiento completo de bisección.

## Checklist

- [ ] ¿Se ha usado el inspector nativo (`--inspect`) en lugar de depurar únicamente con `console.log`?
- [ ] ¿Se ha diagnosticado una Promise colgada con un timeout explícito antes de asumir la causa?
- [ ] ¿El contexto de correlación (`trace-id`) se propaga con `AsyncLocalStorage`, disponible en logs de cualquier punto async posterior?

Ver también: [`../architecture/debugging.md`](../architecture/debugging.md), [`performance.md`](./performance.md), [`../../standards/logging-standard.md`](../../standards/logging-standard.md).
