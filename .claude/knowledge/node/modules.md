# Modules (CommonJS vs. ESM)

> Ver [`typescript.md`](./typescript.md) para la configuración del compilador asociada a cada sistema de módulos.

## CommonJS vs. ECMAScript Modules

```javascript
// CommonJS (require/module.exports) — el estándar histórico de Node.js
const { PedidoRepository } = require('./pedido-repository');
module.exports = { CrearPedidoUseCase };
```

```typescript
// ESM (import/export) — el estándar moderno de JavaScript/TypeScript
import { PedidoRepository } from './pedido-repository';
export class CrearPedidoUseCase { /* ... */ }
```

**Recomendación de este framework**: ESM para proyectos nuevos (`"type": "module"` en `package.json`, o `.mts`/`.cts` para archivos individuales que necesiten mezclar) — es el estándar del lenguaje JavaScript, con mejor tree-shaking en bundlers y alineado con el ecosistema TypeScript moderno. CommonJS permanece relevante solo por compatibilidad con dependencias/proyectos legados que aún no han migrado.

## Diferencias prácticas relevantes

| | CommonJS | ESM |
|---|---|---|
| Carga | Síncrona | Asíncrona (soporta `import()` dinámico nativamente) |
| Resolución | En tiempo de ejecución | Estática, analizable antes de ejecutar (habilita tree-shaking) |
| `__dirname`/`__filename` | Disponibles nativamente | No disponibles — usar `import.meta.url` + `fileURLToPath` |
| Interoperabilidad | Puede importar ESM con `import()` dinámico | Importar CommonJS requiere manejo de `default export` a veces inconsistente entre paquetes |

```typescript
// ESM: equivalente a __dirname
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
```

## Import dinámico: carga condicional/perezosa

```typescript
async function cargarProcesadorSegunProveedor(proveedor: string) {
  if (proveedor === 'stripe') {
    const { StripeProcesador } = await import('./procesadores/stripe-procesador');
    return new StripeProcesador();
  }
  // ...
}
```

Útil para reducir el tiempo de arranque cargando módulos pesados solo cuando realmente se necesitan (plugins opcionales, integraciones de terceros usadas condicionalmente), o para romper dependencias circulares en casos concretos.

## Barrel files (`index.ts`): usar con moderación

```typescript
// pedidos/index.ts — re-exporta todo el módulo desde un único punto
export * from './pedido';
export * from './pedido-repository';
export * from './crear-pedido-use-case';
```

Convenientes para simplificar imports externos al módulo (`import { Pedido, CrearPedidoUseCase } from './pedidos'`), pero pueden introducir dependencias circulares sutiles y dificultar el tree-shaking si se abusa de ellos en un proyecto grande — usar en los límites de un módulo/feature (ver organización por feature en [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md)), no como patrón universal para cada carpeta.

## Resolución de paths absolutos

```json
// tsconfig.json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@domain/*": ["src/domain/*"],
      "@application/*": ["src/application/*"]
    }
  }
}
```

```typescript
// En lugar de rutas relativas frágiles ante mover archivos: ../../../domain/pedido
import { Pedido } from '@domain/pedido';
```

Requiere configuración adicional en el bundler/runtime (`tsconfig-paths` en Node directo, soporte nativo en la mayoría de bundlers modernos) para resolver estos alias en tiempo de ejecución, no solo en tiempo de compilación de TypeScript.

## Checklist

- [ ] ¿El proyecto usa ESM de forma consistente (o CommonJS de forma consistente), sin mezclar sin necesidad?
- [ ] ¿Se ha evitado abusar de barrel files en un módulo grande, con riesgo de dependencias circulares?
- [ ] ¿Los imports usan paths absolutos configurados cuando la profundidad relativa dificulta la legibilidad?

Ver también: [`typescript.md`](./typescript.md), [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md).
