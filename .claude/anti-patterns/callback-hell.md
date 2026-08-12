# Antipatrón: Callback Hell

## Descripción

Anidamiento profundo de funciones de callback en código asíncrono, donde cada operación depende del resultado de la anterior y se anida dentro de ella, produciendo código con forma de "pirámide" hacia la derecha, difícil de leer, de manejar errores y de razonar sobre el orden real de ejecución.

## Cómo detectarlo

- Más de 2-3 niveles de callbacks anidados dentro de otros callbacks.
- Manejo de errores repetido y disperso en cada nivel de anidamiento, o directamente ausente en algunos de ellos.
- Indentación creciente que empuja el código útil cada vez más hacia la derecha de la pantalla.
- Dificultad para insertar manejo de errores centralizado, porque cada callback tiene su propio contexto de error aislado.

## Ejemplo del antipatrón

```javascript
obtenerUsuario(usuarioId, function (error, usuario) {
  if (error) {
    manejarError(error);
  } else {
    obtenerCarrito(usuario.id, function (error, carrito) {
      if (error) {
        manejarError(error);
      } else {
        calcularTotal(carrito, function (error, total) {
          if (error) {
            manejarError(error);
          } else {
            procesarPago(usuario, total, function (error, resultado) {
              if (error) {
                manejarError(error);
              } else {
                enviarConfirmacion(usuario, resultado, function (error) {
                  if (error) {
                    manejarError(error);
                  } else {
                    console.log('Compra completada');
                  }
                });
              }
            });
          }
        });
      }
    });
  }
});
```

Cada nivel repite el mismo patrón de manejo de error, el flujo real (obtener usuario → obtener carrito → calcular total → procesar pago → enviar confirmación) queda oculto entre la indentación, y añadir un paso nuevo en medio de la secuencia requiere reindentar todo el bloque que le sigue.

## Cómo evitarlo

- Usar `Promise` en lugar de callbacks anidados, encadenando con `.then()` — ya reduce el anidamiento, aunque no lo elimina completamente en cadenas largas con lógica condicional.
- Usar `async/await` (la solución preferida en JavaScript/TypeScript moderno), que permite escribir código asíncrono con la misma estructura secuencial y legible que código síncrono, con manejo de errores centralizado en un único `try/catch`.
- Extraer cada paso a una función con nombre propio (Extract Method, ver [`../refactoring.md`](../knowledge/architecture/refactoring.md)) en lugar de anidar funciones anónimas inline.

### Ejemplo corregido (async/await)

```javascript
async function procesarCompra(usuarioId) {
  try {
    const usuario = await obtenerUsuario(usuarioId);
    const carrito = await obtenerCarrito(usuario.id);
    const total = await calcularTotal(carrito);
    const resultado = await procesarPago(usuario, total);
    await enviarConfirmacion(usuario, resultado);

    console.log('Compra completada');
  } catch (error) {
    manejarError(error); // un único punto de manejo de errores para toda la secuencia
  }
}
```

El flujo secuencial es ahora directamente legible de arriba a abajo, sin indentación creciente, y el manejo de errores está centralizado en un único `catch` que cubre toda la cadena de operaciones.

### Operaciones en paralelo (evitar serializar innecesariamente)

Un error común al migrar de callbacks a `async/await` es serializar con `await` operaciones que en realidad son independientes entre sí y podrían ejecutarse en paralelo:

```javascript
// Innecesariamente secuencial si ambas operaciones son independientes
const usuario = await obtenerUsuario(usuarioId);
const configuracion = await obtenerConfiguracion(); // no depende de "usuario"

// Correcto: en paralelo cuando no hay dependencia real entre ellas
const [usuario, configuracion] = await Promise.all([
  obtenerUsuario(usuarioId),
  obtenerConfiguracion(),
]);
```

## Cómo solucionarlo (código legado con callbacks anidados)

1. Identificar la secuencia real de dependencias entre los pasos (¿cuáles dependen genuinamente del resultado del anterior, y cuáles podrían ser paralelos?).
2. Envolver las funciones basadas en callback en `Promise` si la librería/API no ofrece ya una versión basada en promesas (`util.promisify` en Node.js para APIs del propio Node).
3. Reescribir el flujo con `async/await`, extrayendo cada paso significativo a una función nombrada.
4. Centralizar el manejo de errores en un único `try/catch` por flujo, o en un middleware de manejo de errores si se trata de una petición HTTP (ver la Skill de Node/Express correspondiente).

## Checklist

- [ ] ¿Hay más de 2-3 niveles de callbacks anidados en algún flujo asíncrono?
- [ ] ¿El manejo de errores está centralizado, o se repite en cada nivel de anidamiento?
- [ ] ¿Existen operaciones asíncronas independientes ejecutadas secuencialmente sin necesidad, en lugar de en paralelo con `Promise.all`?

Ver también: [`../refactoring.md`](../knowledge/architecture/refactoring.md), sección 5 de `CLAUDE.md` (Node.js).
