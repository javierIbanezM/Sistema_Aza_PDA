# Language (JavaScript)

> Fundamentos del lenguaje JavaScript, independientes del entorno de ejecución (navegador o Node.js). Ver [`../node/`](../node/) para las especificidades del runtime de servidor.

## `var` vs. `let` vs. `const`

```javascript
var x = 1; // function-scoped, hoisted con valor undefined, evitar en código nuevo
let y = 2; // block-scoped, hoisted pero en "temporal dead zone" hasta la declaración
const z = 3; // block-scoped, no reasignable (pero el contenido de un objeto/array SÍ es mutable)
```

`const` por defecto; `let` solo cuando la reasignación es genuinamente necesaria; `var` prácticamente nunca en código nuevo — su scope de función (no de bloque) es fuente frecuente de bugs sutiles en bucles y condicionales.

## Coerción de tipos y `===` vs. `==`

```javascript
'5' == 5;   // true — coerción de tipos antes de comparar
'5' === 5;  // false — sin coerción, tipos distintos

null == undefined;  // true
null === undefined; // false
```

`===` siempre por defecto — `==` con sus reglas de coerción implícita es una fuente histórica de bugs difíciles de razonar. La única excepción idiomática aceptada es `== null` para comprobar `null`/`undefined` a la vez de forma explícita e intencional.

## `this`: el aspecto más confuso del lenguaje

```javascript
const obj = {
  nombre: 'Pedido',
  saludar() { console.log(this.nombre); }, // this = obj cuando se llama como obj.saludar()
};

const fnSuelta = obj.saludar;
fnSuelta(); // this ya NO es obj — es undefined (modo estricto) o el objeto global

const arrow = () => console.log(this); // this se captura léxicamente del contexto de definición, nunca cambia
```

`this` en una función normal depende de **cómo se llama**, no de dónde se define — la causa más común de bugs con `this` es pasar un método como callback (`onClick={obj.metodo}`) sin `bind` ni arrow function, perdiendo el contexto original.

## Hoisting

```javascript
console.log(x); // undefined, no ReferenceError — var se "iza" con valor undefined
var x = 5;

console.log(y); // ReferenceError: Cannot access 'y' before initialization — temporal dead zone
let y = 5;

foo(); // funciona: las function declarations se izan completas, con su cuerpo
function foo() { console.log('hola'); }
```

## Spread, rest, destructuring

```javascript
const { id, ...resto } = pedido; // destructuring + rest: extrae id, agrupa el resto
const nuevoArray = [...arrayOriginal, nuevoElemento]; // spread: copia + añade, sin mutar el original
function sumar(...numeros) { return numeros.reduce((a, b) => a + b, 0); } // rest en parámetros
```

Preferir spread/destructuring sobre `Object.assign`/acceso manual por índice — más legible y, en el caso de spread, garantiza inmutabilidad del original sin esfuerzo adicional.

## Checklist

- [ ] ¿Se usa `const` por defecto, `let` solo cuando hay reasignación real, nunca `var` en código nuevo?
- [ ] ¿Se usa `===` en vez de `==`, salvo el caso idiomático de `== null`?
- [ ] ¿Se ha verificado el valor de `this` en cualquier función pasada como callback (arrow function o `bind` si es necesario)?

Ver también: [`closures.md`](./closures.md), [`prototypes.md`](./prototypes.md), [`../node/typescript.md`](../node/typescript.md) para el superset tipado.
