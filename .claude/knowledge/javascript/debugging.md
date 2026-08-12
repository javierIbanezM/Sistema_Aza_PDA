# Debugging (JavaScript / Navegador)

> Ver [`../architecture/debugging.md`](../architecture/debugging.md) para la metodología sistemática completa, y [`../node/debugging.md`](../node/debugging.md) para las herramientas específicas de depuración en Node.js. Este documento cubre Chrome DevTools y la depuración en el navegador.

## Breakpoints en Chrome DevTools

- **Breakpoint de línea**: click en el número de línea en el panel Sources.
- **Breakpoint condicional**: click derecho → "Add conditional breakpoint" — se detiene solo cuando la condición es verdadera, esencial para depurar un fallo que ocurre solo con datos concretos dentro de un bucle grande (mismo principio que en Java/Node, ver [`../architecture/debugging.md`](../architecture/debugging.md)).
- **Breakpoint de DOM**: en el panel Elements, click derecho sobre un nodo → "Break on... subtree modifications" — útil cuando un elemento cambia inesperadamente y no está claro qué código lo modifica.
- **`debugger;`**: sentencia que pausa la ejecución igual que un breakpoint, útil para depuración rápida sin abrir DevTools manualmente primero.

## Métodos de `console` más allá de `console.log`

```javascript
console.table(pedidos); // muestra un array de objetos como tabla, mucho más legible que log anidado
console.group('Procesando pedido'); // agrupa logs relacionados, colapsable
console.log('Paso 1');
console.log('Paso 2');
console.groupEnd();
console.trace('Llamada sospechosa'); // imprime el stack trace sin lanzar excepción
console.time('operacion'); console.timeEnd('operacion'); // mide duración entre ambas llamadas
console.assert(condicion, 'Este mensaje solo se imprime si la condición es falsa');
```

## El panel Network para depurar peticiones

Antes de asumir un bug en el código JavaScript, verificar en el panel Network: ¿la petición se envió con los headers/body esperados? ¿la respuesta tiene el código de estado y el payload esperados? Un bug que "parece" estar en el manejo de la respuesta a menudo tiene su causa raíz en una petición mal formada — verificar en este orden evita depurar el código cuando el problema real está en la petición misma.

## Depurar código minificado con Source Maps

```javascript
//# sourceMappingURL=app.js.map
```

Sin source maps, un error en producción (código minificado/bundleado) muestra números de línea sin sentido respecto al código fuente original — habilitar la generación de source maps en el build (incluso si no se sirven públicamente, para poder subirlos a un servicio de monitorización de errores) es esencial para poder depurar fallos reportados desde producción.

## El panel Performance para diagnosticar lentitud

Grabar una sesión mientras se reproduce el problema de lentitud, y revisar el timeline: ¿el tiempo se va en JavaScript (Scripting), en cálculo de layout (Rendering), o en pintado (Painting)? Cada categoría apunta a una causa distinta — ver [`performance.md`](./performance.md) para las técnicas de optimización según cada caso.

## Depurar memory leaks con el panel Memory

1. Tomar un heap snapshot en un estado "limpio" (recién cargada la página, o tras una acción y su reversión).
2. Repetir la acción sospechosa varias veces.
3. Tomar otro heap snapshot y comparar (`Comparison` view) — objetos que crecen en número sin caer de vuelta al estado inicial son candidatos a fuga (ver [`memory-management.md`](./memory-management.md)).

## Checklist

- [ ] ¿Se ha usado un breakpoint condicional en lugar de detenerse manualmente en cada iteración de un bucle grande?
- [ ] ¿Se ha verificado el panel Network antes de asumir que el bug está en el manejo de la respuesta?
- [ ] ¿El build de producción genera source maps, aunque no se sirvan públicamente, para depurar errores reportados?

Ver también: [`../architecture/debugging.md`](../architecture/debugging.md), [`../node/debugging.md`](../node/debugging.md), [`performance.md`](./performance.md), [`memory-management.md`](./memory-management.md).
