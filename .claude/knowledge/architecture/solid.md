# SOLID

> Documento único de referencia para los principios SOLID en este framework. Cualquier otra Skill o documento que necesite hablar de SOLID debe enlazar aquí en lugar de reexplicarlo.

## Introducción

SOLID es un acrónimo de cinco principios de diseño orientado a objetos formulados (en su forma moderna) por Robert C. Martin. No son leyes físicas: son heurísticas que, aplicadas con criterio, producen código más fácil de mantener, extender y testear. Aplicados sin criterio, producen exactamente lo contrario: abstracciones innecesarias, indirección excesiva y código difícil de seguir.

La regla general de este framework es: **aplicar SOLID cuando resuelve un problema real de mantenibilidad, no como ritual**. Ver también [`dry-kiss-yagni.md`](./dry-kiss-yagni.md) para el equilibrio con YAGNI.

---

## S — Single Responsibility Principle (SRP)

### Definición

> Una clase debe tener una, y solo una, razón para cambiar.

"Razón para cambiar" no significa "una sola función". Significa que la clase sirve a un único **actor** o **stakeholder**. Si dos partes distintas del negocio (por ejemplo, el departamento de facturación y el departamento de RRHH) pueden pedir cambios sobre la misma clase por motivos distintos, esa clase tiene más de una responsabilidad.

### Señales de violación

- Una clase `Empleado` que calcula el salario, genera un informe en PDF y guarda datos en la base de datos.
- Un método que mezcla validación, lógica de negocio y logging de auditoría.
- Nombres de clase genéricos: `Manager`, `Helper`, `Processor`, `Utils` que van acumulando métodos no relacionados con el tiempo.
- Un archivo que crece indefinidamente porque "es donde va todo lo relacionado con X".

### Ejemplo — Antes (violación)

```java
public class Empleado {
    private String nombre;
    private BigDecimal salarioBase;

    public BigDecimal calcularSalario() {
        // lógica de negocio de nómina
        return salarioBase.multiply(BigDecimal.valueOf(1.1));
    }

    public void guardarEnBaseDeDatos() {
        // acceso a datos directamente desde la entidad de dominio
        String sql = "INSERT INTO empleados ...";
        // ...
    }

    public String generarReportePdf() {
        // generación de un artefacto de presentación
        return "%PDF-1.4 ...";
    }
}
```

Esta clase cambia si cambia la fórmula de nómina, si cambia el esquema de base de datos, o si cambia el formato del PDF. Tres motivos de cambio distintos, tres actores distintos (RRHH, DBA, diseño de reportes).

### Ejemplo — Después (corregido)

```java
public class Empleado {
    private String nombre;
    private BigDecimal salarioBase;
    // Solo estado y comportamiento intrínseco del concepto "Empleado"
}

public class CalculadoraDeNomina {
    public BigDecimal calcularSalario(Empleado empleado) {
        return empleado.getSalarioBase().multiply(BigDecimal.valueOf(1.1));
    }
}

public interface EmpleadoRepository {
    void guardar(Empleado empleado);
}

public class GeneradorReportePdfEmpleado {
    public byte[] generar(Empleado empleado) { /* ... */ }
}
```

Cada clase tiene ahora un único motivo de cambio. `Empleado` pertenece al dominio; `CalculadoraDeNomina` es un domain service; `EmpleadoRepository` es un puerto de persistencia (ver [`clean-architecture.md`](./clean-architecture.md)); `GeneradorReportePdfEmpleado` es un adaptador de presentación.

### Cómo aplicarlo sin exagerar

No es necesario crear una clase por cada método. SRP se viola cuando **motivos de cambio distintos** conviven en la misma clase, no simplemente cuando una clase tiene varios métodos relacionados entre sí. Una clase `Pedido` con métodos `agregarLinea()`, `calcularTotal()` y `aplicarDescuento()` no viola SRP si las tres operaciones pertenecen al mismo concepto de negocio y cambian juntas.

---

## O — Open/Closed Principle (OCP)

### Definición

> Las entidades de software deben estar abiertas a extensión, pero cerradas a modificación.

Se debe poder añadir comportamiento nuevo sin modificar código existente ya probado y estable, para evitar el riesgo de introducir regresiones en funcionalidad que ya funcionaba.

### Ejemplo — Antes (violación)

```java
public class CalculadoraDescuento {
    public BigDecimal calcular(Pedido pedido, String tipoCliente) {
        if (tipoCliente.equals("REGULAR")) {
            return pedido.getTotal().multiply(BigDecimal.valueOf(0.95));
        } else if (tipoCliente.equals("VIP")) {
            return pedido.getTotal().multiply(BigDecimal.valueOf(0.85));
        } else if (tipoCliente.equals("EMPLEADO")) {
            return pedido.getTotal().multiply(BigDecimal.valueOf(0.70));
        }
        // Cada nuevo tipo de cliente obliga a modificar este método
        return pedido.getTotal();
    }
}
```

Cada nuevo tipo de cliente exige tocar un método ya probado, con riesgo de romper los casos existentes.

### Ejemplo — Después (corregido, patrón Strategy)

```java
public interface PoliticaDescuento {
    BigDecimal aplicar(Pedido pedido);
}

public class DescuentoRegular implements PoliticaDescuento {
    public BigDecimal aplicar(Pedido pedido) {
        return pedido.getTotal().multiply(BigDecimal.valueOf(0.95));
    }
}

public class DescuentoVip implements PoliticaDescuento {
    public BigDecimal aplicar(Pedido pedido) {
        return pedido.getTotal().multiply(BigDecimal.valueOf(0.85));
    }
}

public class CalculadoraDescuento {
    public BigDecimal calcular(Pedido pedido, PoliticaDescuento politica) {
        return politica.aplicar(pedido);
    }
}
```

Añadir un nuevo tipo de cliente ahora significa **crear una clase nueva** (`DescuentoEmpleado implements PoliticaDescuento`), sin tocar `CalculadoraDescuento`. Ver [`patterns/Strategy.md`](../../patterns/strategy.md).

### Cuándo NO aplicarlo

Si el conjunto de casos es cerrado y estable (por ejemplo, los cuatro puntos cardinales), un `switch`/`if-else` simple es más legible que una jerarquía de clases. Introducir Strategy para dos casos que nunca van a crecer es sobre-ingeniería — viola YAGNI (ver [`dry-kiss-yagni.md`](./dry-kiss-yagni.md)).

---

## L — Liskov Substitution Principle (LSP)

### Definición

> Si `S` es un subtipo de `T`, los objetos de tipo `T` deben poder sustituirse por objetos de tipo `S` sin alterar la corrección del programa.

Un subtipo no debe:
- Lanzar excepciones que el tipo base no declara ni espera.
- Debilitar postcondiciones (devolver menos de lo que el tipo base garantiza).
- Fortalecer precondiciones (exigir más de lo que el tipo base exige).

### Ejemplo clásico — Antes (violación)

```java
public class Rectangulo {
    protected int ancho;
    protected int alto;

    public void setAncho(int ancho) { this.ancho = ancho; }
    public void setAlto(int alto) { this.alto = alto; }
    public int area() { return ancho * alto; }
}

public class Cuadrado extends Rectangulo {
    @Override
    public void setAncho(int ancho) {
        this.ancho = ancho;
        this.alto = ancho; // efecto secundario inesperado
    }

    @Override
    public void setAlto(int alto) {
        this.alto = alto;
        this.ancho = alto; // efecto secundario inesperado
    }
}
```

```java
void redimensionar(Rectangulo r) {
    r.setAncho(5);
    r.setAlto(10);
    assert r.area() == 50; // Falla si r es un Cuadrado: area() == 100
}
```

`Cuadrado` no puede sustituir a `Rectangulo` sin romper el comportamiento esperado por el código cliente. Herencia modelada incorrectamente: "un cuadrado es un rectángulo" es cierto matemáticamente, pero no en términos de comportamiento mutable.

### Ejemplo — Después (corregido)

```java
public interface Forma {
    int area();
}

public final class Rectangulo implements Forma {
    private final int ancho;
    private final int alto;
    public Rectangulo(int ancho, int alto) { this.ancho = ancho; this.alto = alto; }
    public int area() { return ancho * alto; }
}

public final class Cuadrado implements Forma {
    private final int lado;
    public Cuadrado(int lado) { this.lado = lado; }
    public int area() { return lado * lado; }
}
```

Eliminar la relación de herencia y modelar ambos como implementaciones independientes de `Forma`, inmutables, elimina el problema de raíz.

### Regla práctica

Antes de heredar, pregúntate: *¿todo lo que el código cliente puede hacer con el tipo base sigue siendo válido y con el mismo significado si le paso el subtipo?* Si la respuesta no es un "sí" incondicional, usa composición en lugar de herencia.

---

## I — Interface Segregation Principle (ISP)

### Definición

> Ningún cliente debe verse forzado a depender de métodos que no usa.

Interfaces grandes y genéricas ("fat interfaces") obligan a las clases que las implementan a proporcionar implementaciones vacías o que lanzan `UnsupportedOperationException` para métodos que no tienen sentido en su contexto.

### Ejemplo — Antes (violación)

```java
public interface Trabajador {
    void trabajar();
    void comer();
    void dormir();
}

public class Robot implements Trabajador {
    public void trabajar() { /* ... */ }
    public void comer() {
        throw new UnsupportedOperationException("Un robot no come");
    }
    public void dormir() {
        throw new UnsupportedOperationException("Un robot no duerme");
    }
}
```

### Ejemplo — Después (corregido)

```java
public interface Trabajable {
    void trabajar();
}

public interface Alimentable {
    void comer();
}

public interface Durmiente {
    void dormir();
}

public class Robot implements Trabajable {
    public void trabajar() { /* ... */ }
}

public class Humano implements Trabajable, Alimentable, Durmiente {
    public void trabajar() { /* ... */ }
    public void comer() { /* ... */ }
    public void dormir() { /* ... */ }
}
```

Cada clase implementa exactamente las capacidades que tiene sentido que tenga, y el código cliente puede depender de la interfaz mínima que necesita (`Trabajable` para un sistema de asignación de tareas, sin acoplarse a `comer()`/`dormir()`).

### Aplicación en TypeScript/NestJS

El mismo principio aplica a los DTOs y contratos de servicio: preferir interfaces `Readable`, `Writable`, `Auditable` pequeñas y componibles sobre una única interfaz `Repository<T>` con quince métodos que la mayoría de implementaciones no necesitan.

---

## D — Dependency Inversion Principle (DIP)

### Definición

> Los módulos de alto nivel no deben depender de módulos de bajo nivel. Ambos deben depender de abstracciones. Las abstracciones no deben depender de detalles; los detalles deben depender de abstracciones.

Este es el principio que hace posible la [Clean Architecture](./clean-architecture.md): el dominio y los casos de uso (alto nivel, contienen las reglas de negocio) no deben depender de frameworks, bases de datos ni librerías externas (bajo nivel, detalles de implementación).

### Ejemplo — Antes (violación)

```java
public class ServicioPedidos {
    private final MySqlPedidoRepository repository = new MySqlPedidoRepository();

    public void crearPedido(Pedido pedido) {
        repository.guardar(pedido); // acoplado directamente a MySQL
    }
}
```

`ServicioPedidos` (módulo de alto nivel, contiene lógica de aplicación) depende directamente de `MySqlPedidoRepository` (módulo de bajo nivel, detalle de infraestructura). Cambiar de base de datos, o testear el servicio sin una base de datos real, se vuelve costoso.

### Ejemplo — Después (corregido)

```java
public interface PedidoRepository {
    void guardar(Pedido pedido);
}

public class ServicioPedidos {
    private final PedidoRepository repository;

    public ServicioPedidos(PedidoRepository repository) { // inyectado
        this.repository = repository;
    }

    public void crearPedido(Pedido pedido) {
        repository.guardar(pedido);
    }
}

// En la capa de infraestructura:
public class MySqlPedidoRepository implements PedidoRepository {
    public void guardar(Pedido pedido) { /* JDBC/JPA */ }
}
```

Ahora `ServicioPedidos` depende de la abstracción `PedidoRepository`, no de una implementación concreta. `MySqlPedidoRepository` (detalle) depende de la abstracción, no al revés. Esto permite:
- Testear `ServicioPedidos` con un `PedidoRepository` falso/en memoria, sin base de datos real.
- Cambiar de MySQL a PostgreSQL o MongoDB sin tocar la lógica de negocio.

### Relación con la inyección de dependencias

DIP es el **principio**; la inyección de dependencias (constructor injection en Spring, `@Injectable()` en NestJS, Hilt en Android) es la **técnica** que lo implementa en la práctica. Ver `dependency-injection.md` de cada Skill de framework específico (Java, Node, Android) para el detalle de configuración de cada uno.

---

## Errores comunes al aplicar SOLID

1. **Aplicar OCP/DIP a código que no va a cambiar nunca** — genera abstracciones muertas. Ver [`dry-kiss-yagni.md`](./dry-kiss-yagni.md).
2. **Confundir SRP con "una clase, un método"** — SRP habla de motivos de cambio (actores), no de cantidad de métodos.
3. **Crear una interfaz por cada clase "por si acaso"** — viola ISP en sentido inverso (interfaces innecesarias que nadie segrega realmente) y YAGNI.
4. **Usar herencia para reutilizar código sin verificar LSP** — preferir composición cuando la relación "es un" no se sostiene en todos los casos de uso.

## Checklist rápido

- [ ] ¿Esta clase tiene un único motivo de cambio identificable?
- [ ] ¿Puedo añadir un caso nuevo sin modificar código ya probado?
- [ ] ¿Cualquier subtipo puede sustituir a su tipo base sin sorpresas para el código cliente?
- [ ] ¿Las interfaces que expongo son del tamaño mínimo que cada cliente necesita?
- [ ] ¿La lógica de negocio depende de abstracciones, no de frameworks o librerías concretas?

Ver también: [`patterns/Strategy.md`](../../patterns/strategy.md), [`patterns/Repository.md`](../../patterns/repository.md), [`anti-patterns/GodObject.md`](../../anti-patterns/god-object.md).
