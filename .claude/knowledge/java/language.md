# Java Language

> Fundamentos del lenguaje Java en sí, independientes de cualquier framework. Spring vive en [`../spring/`](../spring/), Hibernate/JPA en [`../hibernate/`](../hibernate/) — no se duplican aquí.

## Tipado y null-safety

- Java no tiene null-safety a nivel de tipos (a diferencia de Kotlin). La disciplina de evitar `NullPointerException` es responsabilidad del código: usar `Optional<T>` en retornos que pueden estar ausentes (nunca en parámetros ni campos — ver [`pitfalls.md`](./pitfalls.md)), validar entradas en el borde (ver [`../../standards/validation.md`](../../standards/validation.md)).
- `Objects.requireNonNull(valor, "mensaje")` para fallar rápido y explícito ante un valor nulo inesperado, en lugar de dejar que el NPE aparezca más adelante sin contexto.

## Inmutabilidad

```java
public final class Dinero {
    private final BigDecimal monto;
    private final String moneda;

    public Dinero(BigDecimal monto, String moneda) {
        this.monto = Objects.requireNonNull(monto);
        this.moneda = Objects.requireNonNull(moneda);
    }
    // sin setters — cualquier "modificación" devuelve una nueva instancia
}
```

Preferir clases `final` con campos `final` por defecto — ver [`records.md`](./records.md) para la forma idiomática moderna de expresar esto.

## Interfaces con métodos por defecto

```java
public interface Notificador {
    void enviar(String mensaje);

    default void enviarConReintento(String mensaje, int intentos) {
        for (int i = 0; i < intentos; i++) {
            try { enviar(mensaje); return; }
            catch (Exception e) { if (i == intentos - 1) throw e; }
        }
    }
}
```

Los métodos `default` permiten evolucionar una interfaz añadiendo comportamiento sin romper implementaciones existentes — relevante para el principio Open/Closed (ver [`../architecture/solid.md`](../architecture/solid.md)).

## `var` (inferencia de tipo local, Java 10+)

```java
var pedidos = new ArrayList<Pedido>(); // el tipo se infiere del lado derecho
```

Usar `var` cuando el tipo es evidente por el contexto (constructor explícito a la derecha); evitarlo cuando reduce la legibilidad (`var resultado = procesar(x);` no comunica qué es `resultado` sin mirar la firma de `procesar`).

## Text Blocks (Java 15+)

```java
String json = """
    {
      "pedidoId": "%s",
      "estado": "%s"
    }
    """.formatted(pedidoId, estado);
```

Preferible a concatenación de strings para bloques de texto multilínea (SQL embebido, JSON de ejemplo, mensajes largos).

## Checklist

- [ ] ¿Se usa `Optional<T>` solo en retornos, nunca en parámetros ni campos?
- [ ] ¿Las clases que representan conceptos inmutables son `final` con campos `final`?
- [ ] ¿Se ha considerado `record` (ver [`records.md`](./records.md)) en lugar de una clase mutable con boilerplate manual?

Ver también: [`records.md`](./records.md), [`sealed-classes.md`](./sealed-classes.md), [`generics.md`](./generics.md), [`../architecture/solid.md`](../architecture/solid.md).
