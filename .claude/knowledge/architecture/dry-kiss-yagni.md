# DRY, KISS y YAGNI

> Documento único de referencia. Enlaza aquí desde cualquier otra Skill en lugar de reexplicar estos principios.

## DRY — Don't Repeat Yourself

### Definición precisa

> Todo conocimiento debe tener una representación única, inequívoca y autorizada dentro de un sistema.

La formulación original (Hunt & Thomas, *The Pragmatic Programmer*) habla de **conocimiento**, no de líneas de código idénticas. Esta distinción es la fuente del error más común al aplicar DRY.

### El error de la "duplicación incidental"

Dos fragmentos de código pueden ser textualmente idénticos hoy y representar **conceptos de negocio distintos** que evolucionarán de forma independiente:

```java
// Validación de un email de usuario al registrarse
boolean esValido = email.matches("^[^@]+@[^@]+\\.[^@]+$");

// Validación de un email de contacto de proveedor
boolean esValido = email.matches("^[^@]+@[^@]+\\.[^@]+$");
```

Unificar esto en una única función `validarEmail()` compartida **parece** correcto por DRY. Pero si mañana el negocio decide que los emails de proveedor deben aceptar direcciones internas sin dominio público y los de usuario no, unificar prematuramente obliga a añadir parámetros/flags a una función que en realidad representa dos reglas de negocio distintas que solo coincidían por casualidad.

**Regla práctica de este framework**: aplicar la *regla de las tres repeticiones* — duplicar una vez está bien, duplicar una segunda vez es una señal, la tercera vez que se repite el mismo conocimiento es cuando se extrae la abstracción, momento en el que ya hay suficiente evidencia de que es el mismo concepto y no una coincidencia.

### Duplicación que SIEMPRE hay que eliminar

- Reglas de negocio (cálculo de precios, impuestos, descuentos, validaciones de dominio).
- Configuración (URLs de servicios, timeouts, constantes de negocio) — centralizar en un único lugar de configuración.
- Esquemas de datos (definir el contrato una vez: OpenAPI, Protobuf, JSON Schema; generar clientes/tipos a partir de ahí, no mantenerlos a mano en varios sitios).

### Cómo eliminarla correctamente

- Extraer funciones puras para lógica de cálculo/validación repetida.
- Extraer clases/servicios cuando la responsabilidad repetida es coherente (ver [`solid.md`](./solid.md) — SRP).
- Usar herencia/composición o Template Method solo cuando el algoritmo compartido es genuinamente el mismo en todos los casos.
- Generar código a partir de una única fuente de verdad (ej. tipos TypeScript generados desde el esquema OpenAPI) en lugar de mantenerlo sincronizado a mano.

---

## KISS — Keep It Simple, Stupid

### Definición

> Ante dos soluciones que resuelven el mismo problema con la misma corrección, elige la más simple de leer, entender y mantener.

"Simple" no significa "con menos líneas de código" ni "con menos archivos". Significa **menor carga cognitiva** para quien lea el código después (incluido tu propio yo dentro de seis meses).

### Ejemplo — Complejidad innecesaria

```typescript
// Sobre-ingenierizado: un "motor de reglas" genérico para una validación fija
class RuleEngine<T> {
  private rules: Array<(value: T) => boolean> = [];
  addRule(rule: (value: T) => boolean) { this.rules.push(rule); return this; }
  evaluate(value: T): boolean { return this.rules.every(r => r(value)); }
}

const engine = new RuleEngine<number>()
  .addRule(v => v > 0)
  .addRule(v => v < 150);

const esEdadValida = engine.evaluate(edad);
```

```typescript
// KISS: la regla de negocio es simple y fija, exprésala de forma simple
const esEdadValida = edad > 0 && edad < 150;
```

Un "motor de reglas" genérico solo se justifica si las reglas son dinámicas (configurables por el usuario, cargadas desde base de datos, etc.). Si son fijas y conocidas en tiempo de compilación, la abstracción genérica es puro coste sin beneficio.

### Señales de que se está violando KISS

- Se necesita un diagrama para explicar el flujo de una función que "solo" valida un formulario.
- Hay más de 3-4 niveles de indentación anidada.
- Se usa reflexión, metaprogramación o generación dinámica de código para un caso que podría resolverse con una función directa.
- El código introduce un patrón de diseño (Strategy, Factory, Visitor) para un único caso que no va a variar.

### Cómo aplicar KISS sin caer en simplismo

KISS no significa evitar la complejidad **inherente** del dominio (un motor de cálculo de impuestos multi-país es complejo porque el problema lo es). Significa no añadir complejidad **accidental** encima de la complejidad inherente.

---

## YAGNI — You Aren't Gonna Need It

### Definición

> No implementes una funcionalidad, una capa de abstracción o un parámetro de configuración hasta que exista un requisito real y presente que lo necesite.

### Ejemplo — Violación típica

```java
public interface ProveedorDePago {
    ResultadoPago procesar(Pago pago);
}

public class ProveedorPagoStripe implements ProveedorDePago { /* única implementación real */ }

// Se genera además, "por si en el futuro añadimos más proveedores":
public class ProveedorPagoFactory {
    public static ProveedorDePago crear(String tipo) {
        switch (tipo) {
            case "STRIPE": return new ProveedorPagoStripe();
            default: throw new IllegalArgumentException("Proveedor no soportado: " + tipo);
        }
    }
}
```

Si hoy el negocio solo usa Stripe y no hay ningún plan confirmado de añadir otro proveedor, la `Factory` y la interfaz `ProveedorDePago` son abstracción especulativa: código que hay que mantener, testear y entender, sin que resuelva ningún problema actual.

### Cuándo SÍ anticiparse está justificado

- Existe un requisito de negocio **confirmado** para el futuro cercano (ej. "en Q3 lanzamos en LATAM y necesitaremos un segundo proveedor de pago" ya está en el roadmap).
- El coste de no abstraer ahora es desproporcionadamente alto de revertir después (ej. elegir un formato de almacenamiento de datos que sería muy costoso migrar).
- Se trata de un punto de extensión ya exigido explícitamente por el equipo/arquitectura (ej. un plugin system que es el producto en sí mismo).

En cualquier otro caso: implementa la solución más simple que resuelve el requisito actual (KISS), sin añadir capas para necesidades hipotéticas.

### La tensión entre YAGNI y SOLID/OCP

YAGNI y el principio Open/Closed (ver [`solid.md`](./solid.md)) pueden parecer contradictorios. La resolución práctica de este framework:

- OCP se aplica **reactivamente**: cuando aparece el segundo caso real que requiere el mismo punto de extensión, se refactoriza hacia una abstracción (Strategy, Factory...).
- No se aplica **preventivamente** basándose en casos hipotéticos.

---

## Resumen de aplicación conjunta

| Principio | Pregunta que responde | Riesgo de sobre-aplicar |
|---|---|---|
| DRY | ¿Este conocimiento ya existe en otro lugar? | Acoplar conceptos que solo coinciden por casualidad |
| KISS | ¿Existe una forma más simple de resolver esto? | Simplificar en exceso un problema genuinamente complejo |
| YAGNI | ¿Este requisito existe ya, o es especulativo? | Bloquear evolución futura legítima ya planificada |

Ver también: [`solid.md`](./solid.md), [`../anti-patterns/god-object.md`](../../anti-patterns/god-object.md), [`../anti-patterns/spaghetti.md`](../../anti-patterns/spaghetti.md).
