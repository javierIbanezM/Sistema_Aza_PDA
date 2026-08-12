# Antipatrón: Spaghetti Code

## Descripción

Código con estructura de control enrevesada y flujo difícil de seguir: dependencias cruzadas entre módulos sin una dirección clara, condicionales profundamente anidados, saltos de control implícitos (excepciones usadas como control de flujo, variables globales mutadas desde múltiples lugares) que hacen que entender "qué pasa cuando ejecuto esto" requiera saltar entre muchos archivos sin un orden lógico evidente.

## Cómo detectarlo

- Funciones con más de 4 niveles de indentación anidada (ver KISS en [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).
- Dependencias circulares entre módulos (el módulo A importa de B, B importa de C, C importa de A).
- Variables de estado global mutadas desde múltiples puntos no relacionados del código, sin un único punto de control.
- Flujo de control que depende de efectos secundarios ocultos (una función que, además de calcular un valor, modifica silenciosamente un estado externo del que depende otra parte del código más adelante).
- Imposibilidad de predecir el resultado de una función leyendo solo su firma y su cuerpo, porque depende del orden de llamadas previas o de estado mutable externo.

## Ejemplo del antipatrón

```javascript
let estadoGlobal = { usuario: null, carrito: [], descuentoAplicado: false };

function procesarCompra(item) {
  if (estadoGlobal.usuario) {
    if (estadoGlobal.usuario.tipo === 'VIP') {
      if (!estadoGlobal.descuentoAplicado) {
        if (estadoGlobal.carrito.length > 3) {
          aplicarDescuento(); // muta estadoGlobal.descuentoAplicado internamente, efecto oculto
          if (estadoGlobal.descuentoAplicado) {
            estadoGlobal.carrito.push(item);
            notificar(); // depende de un estado mutado tres funciones atrás
          }
        } else {
          estadoGlobal.carrito.push(item);
        }
      } else {
        estadoGlobal.carrito.push(item);
      }
    } else {
      estadoGlobal.carrito.push(item);
    }
  } else {
    throw new Error('sin usuario'); // control de flujo mezclado con validación en mitad de la lógica
  }
}
```

Entender qué hace `procesarCompra` requiere leer `aplicarDescuento()` y `notificar()` para saber qué mutan, y no hay forma de predecir el resultado solo con los parámetros de entrada.

## Cómo evitarlo

- **Guard clauses** en lugar de anidamiento: retornar/lanzar temprano ante condiciones inválidas, dejando el flujo principal sin indentación adicional.
- Evitar estado mutable compartido entre funciones; preferir funciones puras que reciben todo lo que necesitan como parámetros y devuelven un resultado explícito, sin efectos secundarios ocultos.
- Aplicar Extract Method (ver [`../refactoring.md`](../knowledge/architecture/refactoring.md)) para dar nombre explícito a cada bloque de lógica anidada.
- Diseñar la dependencia entre módulos en una única dirección (ver la regla de dependencia de Clean Architecture en [`../clean-architecture.md`](../knowledge/architecture/clean-architecture.md)), evitando importaciones circulares.

### Ejemplo corregido (guard clauses + sin estado global oculto)

```javascript
function procesarCompra(usuario, carrito, item) {
  if (!usuario) throw new Error('Se requiere un usuario para procesar la compra');

  const nuevoCarrito = [...carrito, item];
  const aplicaDescuento = usuario.tipo === 'VIP' && nuevoCarrito.length > 3;
  const total = calcularTotal(nuevoCarrito, aplicaDescuento);

  return { carrito: nuevoCarrito, total, descuentoAplicado: aplicaDescuento };
}
```

La función ahora es pura: su resultado depende solo de sus parámetros, no de estado externo mutado en otro lugar, y se puede testear de forma aislada (ver [`../testing.md`](../knowledge/architecture/testing.md)) sin preparar un `estadoGlobal` previo.

## Cómo solucionarlo (código legado ya afectado)

1. Escribir tests de caracterización sobre el comportamiento observable actual, aunque sea difícil por la falta de límites claros (ver [`../refactoring.md`](../knowledge/architecture/refactoring.md)).
2. Identificar y eliminar dependencias circulares primero — son la causa raíz de que el resto del código sea difícil de razonar de forma aislada.
3. Aplicar guard clauses para reducir el anidamiento antes de intentar cualquier extracción de método.
4. Ir sustituyendo estado mutable compartido por parámetros y valores de retorno explícitos, función por función, verificando tests tras cada paso.

## Checklist

- [ ] ¿Puedo predecir el resultado de esta función leyendo solo su firma, sin rastrear otras funciones que mutan estado compartido?
- [ ] ¿Existen dependencias circulares entre módulos del proyecto?
- [ ] ¿Hay más de 3-4 niveles de indentación anidada en alguna función?

Ver también: [`../refactoring.md`](../knowledge/architecture/refactoring.md), [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md), [`GodObject.md`](./god-object.md).
