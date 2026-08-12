# Closures

> Ver [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md) — los closures son la causa más común de fugas de memoria sutiles en JavaScript, cubierta en profundidad allí; este documento explica el mecanismo en sí.

## Qué es un closure

Una función "recuerda" el ámbito léxico en el que fue creada, incluso después de que ese ámbito haya terminado de ejecutarse.

```javascript
function crearContador() {
  let contador = 0; // variable "capturada" por el closure
  return function incrementar() {
    contador++;
    return contador;
  };
}

const contar = crearContador();
contar(); // 1
contar(); // 2 — "contador" persiste entre llamadas, aunque crearContador() ya terminó de ejecutarse
```

## Uso idiomático: encapsulación y fábricas de funciones

```javascript
function crearValidador(regex) {
  return function (valor) {
    return regex.test(valor);
  };
}

const esEmailValido = crearValidador(/^[^@]+@[^@]+\.[^@]+$/);
const esTelefonoValido = crearValidador(/^\d{9}$/);
```

Cada función devuelta "recuerda" su propio `regex`, sin necesidad de una clase — patrón habitual para crear funciones especializadas a partir de una función genérica configurable.

## El error clásico en bucles con `var`

```javascript
// Con var: todas las funciones capturan la MISMA variable i, que termina en 3
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100); // imprime 3, 3, 3
}

// Con let: cada iteración tiene su propia i (block-scoped)
for (let i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100); // imprime 0, 1, 2
}
```

`let` crea un nuevo binding por cada iteración del bucle; `var` comparte un único binding para todo el bucle — este es uno de los motivos históricos principales por los que `let` reemplazó a `var` como estándar (ver [`language.md`](./language.md)).

## Closures y fugas de memoria

```javascript
function registrarListener(datosGrandes) {
  elemento.addEventListener('click', () => {
    // aunque esta función no use datosGrandes, el closure retiene
    // TODO el ámbito de registrarListener, incluyendo datosGrandes
    procesar();
  });
}
```

Ver [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md) para el patrón completo — un listener registrado y nunca removido, con un closure que retiene referencias grandes, es una fuga de memoria clásica en aplicaciones de larga duración (SPAs, servidores Node de larga ejecución).

## Módulos con closures (patrón anterior a ES Modules)

```javascript
const ContadorModule = (function () {
  let contador = 0; // privado, inaccesible desde fuera del IIFE
  return {
    incrementar: () => ++contador,
    valor: () => contador,
  };
})();
```

El patrón *Immediately Invoked Function Expression* (IIFE) + closure fue la forma idiomática de encapsular estado privado antes de que `class` y ES Modules (ver [`modules.md`](./modules.md)) lo resolvieran de forma nativa — sigue apareciendo en código legado, útil reconocerlo aunque no sea el estándar actual para código nuevo.

## Checklist

- [ ] ¿Se entiende que un closure retiene el ámbito léxico completo, no solo las variables usadas explícitamente?
- [ ] ¿Los bucles con callbacks asíncronos usan `let`, no `var`, para capturar el valor correcto por iteración?
- [ ] ¿Los listeners de larga duración con closures que capturan datos grandes se des-registran cuando ya no son necesarios?

Ver también: [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md), [`modules.md`](./modules.md), [`language.md`](./language.md).
