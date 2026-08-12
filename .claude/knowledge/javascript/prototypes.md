# Prototypes

> Ver [`language.md`](./language.md) para `this`, íntimamente relacionado con el modelo de prototipos.

## Herencia prototípica: el modelo real detrás de `class`

```javascript
function Animal(nombre) {
  this.nombre = nombre;
}
Animal.prototype.hablar = function () {
  return `${this.nombre} hace un sonido`;
};

function Perro(nombre) {
  Animal.call(this, nombre);
}
Perro.prototype = Object.create(Animal.prototype);
Perro.prototype.constructor = Perro;
Perro.prototype.hablar = function () {
  return `${this.nombre} ladra`;
};

const rex = new Perro('Rex');
rex.hablar(); // "Rex ladra" — busca en Perro.prototype primero, encuentra el override
```

`class` (ES6+) es **azúcar sintáctico** sobre este mismo mecanismo — no introduce un modelo de herencia nuevo, solo una sintaxis más familiar para quien viene de lenguajes basados en clases:

```javascript
class Animal {
  constructor(nombre) { this.nombre = nombre; }
  hablar() { return `${this.nombre} hace un sonido`; }
}

class Perro extends Animal {
  hablar() { return `${this.nombre} ladra`; }
}
```

Ambos ejemplos son equivalentes en tiempo de ejecución — `class` es preferible en código nuevo por legibilidad, pero entender la herencia prototípica subyacente explica comportamientos que de otro modo parecen "mágicos" (por qué modificar `Array.prototype` afecta a todos los arrays, por qué `instanceof` funciona como funciona).

## La cadena de prototipos

```javascript
rex.hablar();       // 1. ¿Perro.prototype tiene "hablar"? Sí, se usa esa
rex.toString();      // 2. ¿Perro.prototype tiene "toString"? No.
                      //    ¿Animal.prototype? No. ¿Object.prototype? Sí, se usa esa
```

Cada acceso a una propiedad/método no encontrado directamente en el objeto sube por la cadena de prototipos hasta encontrarlo o llegar a `null` (`Object.prototype.__proto__ === null`, el final de la cadena).

## `Object.create` para herencia sin `class`

```javascript
const animalBase = {
  hablar() { return `${this.nombre} hace un sonido`; },
};

const perro = Object.create(animalBase);
perro.nombre = 'Rex';
perro.hablar(); // "Rex hace un sonido" — hereda directamente del objeto base, sin función constructora
```

Útil para composición de objetos sin la ceremonia de `class`/constructor — menos común en código de aplicación moderno, pero presente en algunas librerías y en el propio modelo interno del lenguaje.

## Modificar prototipos nativos: evitar

```javascript
// Antipatrón: modificar un prototipo nativo afecta a TODO el código que use ese tipo,
// incluidas librerías de terceros que no esperan el cambio
Array.prototype.ultimo = function () {
  return this[this.length - 1];
};
```

Puede parecer conveniente, pero introduce comportamiento global no local — cualquier librería externa que itere sobre las propiedades de un array (`for...in`, poco común pero posible) puede comportarse de forma inesperada. Preferir una función utilitaria (`ultimoElemento(array)`) o extender mediante composición, nunca modificar `Array.prototype`/`Object.prototype`/`String.prototype` directamente.

## `hasOwnProperty` vs. propiedades heredadas

```javascript
const obj = Object.create({ heredada: true });
obj.propia = true;

obj.hasOwnProperty('propia');    // true
obj.hasOwnProperty('heredada');  // false — existe, pero viene del prototipo, no es propia

for (const key in obj) { /* recorre TANTO propias como heredadas enumerables */ }
Object.keys(obj); // solo propiedades propias enumerables — preferible en la mayoría de casos
```

## Checklist

- [ ] ¿Se entiende que `class` es azúcar sintáctico sobre prototipos, no un modelo distinto?
- [ ] ¿Se ha evitado modificar prototipos de tipos nativos (`Array.prototype`, `Object.prototype`)?
- [ ] ¿Se usa `Object.keys`/`Object.entries` en lugar de `for...in` cuando solo interesan las propiedades propias?

Ver también: [`language.md`](./language.md), [`closures.md`](./closures.md).
