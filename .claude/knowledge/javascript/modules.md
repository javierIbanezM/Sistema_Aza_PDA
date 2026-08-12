# Modules (JavaScript / Navegador)

> Ver [`../node/modules.md`](../node/modules.md) para CommonJS y la resolución de módulos específica de Node.js (incluida la interoperabilidad CJS/ESM). Este documento cubre ES Modules como especificación del lenguaje, tal como se usa nativamente en el navegador.

## Sintaxis estándar

```javascript
// matematicas.js
export function sumar(a, b) { return a + b; }
export const PI = 3.14159;
export default class Calculadora { /* ... */ } // un único default export por módulo

// main.js
import Calculadora, { sumar, PI } from './matematicas.js';
```

## Módulos en el navegador: `<script type="module">`

```html
<script type="module" src="main.js"></script>
```

Los scripts con `type="module"` se ejecutan automáticamente en modo estricto, tienen su propio scope (no contaminan el objeto global), y se cargan de forma diferida (equivalente a `defer`) — el navegador espera a que el DOM esté listo antes de ejecutarlos, y resuelve el grafo de dependencias de imports antes de ejecutar cualquiera.

## Import dinámico

```javascript
button.addEventListener('click', async () => {
  const { generarReporte } = await import('./reporte-pesado.js'); // carga bajo demanda
  generarReporte();
});
```

Permite dividir el código en chunks cargados solo cuando se necesitan (*code splitting*) — reduce el tamaño del bundle inicial que el navegador debe descargar y parsear antes de que la aplicación sea interactiva.

## Import maps: resolver nombres de paquete sin bundler

```html
<script type="importmap">
{
  "imports": {
    "lodash": "https://cdn.jsdelivr.net/npm/lodash-es@4.17.21/lodash.js"
  }
}
</script>
<script type="module">
  import _ from 'lodash'; // resuelto vía el import map, sin necesidad de bundler
</script>
```

Permite usar nombres de paquete "desnudos" (como en Node) directamente en el navegador sin un paso de bundling previo — útil para prototipos rápidos o proyectos que deliberadamente evitan la complejidad de un bundler.

## CORS y módulos: restricciones del navegador

A diferencia de scripts clásicos, los módulos ES cargados vía `<script type="module" src="...">` desde un origen distinto requieren cabeceras CORS apropiadas en el servidor que los sirve — un fallo silencioso común es intentar cargar un módulo desde `file://` directamente (sin servidor HTTP), lo cual el navegador bloquea por política de mismo origen.

## Tree-shaking: por qué ESM lo habilita y CommonJS no

```javascript
// Con ESM, el bundler puede analizar ESTÁTICAMENTE qué exports se usan realmente
import { sumar } from './matematicas.js'; // el bundler puede eliminar PI y Calculadora del bundle final si no se usan

// Con CommonJS, la resolución es dinámica en tiempo de ejecución — más difícil de analizar estáticamente
const { sumar } = require('./matematicas.js');
```

La resolución estática de ESM (los imports/exports son fijos, no pueden calcularse condicionalmente en tiempo de ejecución como con `require` dinámico) es lo que permite a los bundlers eliminar código no utilizado con confianza — ver [`../node/modules.md`](../node/modules.md) para la comparación completa con CommonJS.

## Checklist

- [ ] ¿Los scripts de módulo usan `type="module"`, no scripts clásicos con imports simulados?
- [ ] ¿Se usa import dinámico para código pesado que no se necesita en la carga inicial?
- [ ] ¿El servidor que sirve los módulos configura CORS correctamente si se cargan desde otro origen?

Ver también: [`../node/modules.md`](../node/modules.md), [`fetch.md`](./fetch.md).
