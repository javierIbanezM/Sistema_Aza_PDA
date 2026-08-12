# Records (Java 16+)

> Ver [`../architecture/ddd.md`](../architecture/ddd.md) para el concepto de Value Object que `record` implementa de forma idiomática en Java.

## Qué es un record

Un `record` es una clase inmutable de datos: el compilador genera automáticamente constructor, getters (sin prefijo `get`), `equals`/`hashCode`/`toString` basados en todos los componentes.

```java
public record Dinero(BigDecimal monto, String moneda) {}

Dinero precio = new Dinero(new BigDecimal("19.99"), "EUR");
precio.monto();  // getter generado, sin prefijo "get"
precio.moneda();
```

Elimina el boilerplate manual que antes requería Lombok (`@Value`, ver [`../lombok/lombok.md`](../lombok/lombok.md)) o escribirlo a mano — para Value Objects simples, `record` es preferible por ser parte del lenguaje estándar, sin dependencia externa.

## Validación de invariantes en el constructor compacto

```java
public record Dinero(BigDecimal monto, String moneda) {
    public Dinero { // constructor compacto: sin lista de parámetros repetida
        if (monto.signum() < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        moneda = moneda.toUpperCase(); // normalización permitida en el constructor compacto
    }
}
```

El constructor compacto permite validar y normalizar antes de la asignación implícita a los campos — el `record` sigue siendo inmutable, pero garantiza sus invariantes de construcción, igual que un Value Object bien diseñado en DDD (ver [`../architecture/ddd.md`](../architecture/ddd.md)).

## Métodos adicionales

```java
public record Dinero(BigDecimal monto, String moneda) {
    public Dinero sumar(Dinero otro) {
        if (!moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("No se pueden sumar monedas distintas");
        }
        return new Dinero(monto.add(otro.monto), moneda); // devuelve una nueva instancia, no muta
    }
}
```

Un `record` puede tener métodos de negocio adicionales igual que cualquier clase — no se limita a ser un contenedor pasivo de datos, siempre que esos métodos no muten estado (imposible de todas formas, dado que los campos son `final`).

## Records como DTOs

```java
public record CrearPedidoRequest(
    UUID clienteId,
    @NotEmpty List<LineaPedidoRequest> lineas
) {}

public record PedidoResponse(String id, String estado, BigDecimal total) {}
```

Uso idiomático estándar en este framework para DTOs (ver [`../../standards/dto.md`](../../standards/dto.md)) — inmutables por naturaleza, sin necesidad de `@Value` de Lombok.

## Records vs. clases normales: cuándo NO usar un record

- Cuando el objeto tiene **identidad** propia más allá de sus valores (una Entidad de dominio, ver [`../architecture/ddd.md`](../architecture/ddd.md)) — un `record` compara por valor de todos los campos, lo cual es incorrecto para una Entidad, cuya igualdad debe basarse solo en su ID.
- Cuando se necesita herencia de una clase base (los records no pueden extender otra clase, aunque sí implementar interfaces).
- Como entidad JPA (`@Entity`) — JPA requiere un constructor sin argumentos y campos mutables gestionables por el proveedor de persistencia, incompatible con la inmutabilidad estricta de un record. Ver [`../hibernate/jpa.md`](../hibernate/jpa.md).

## Deconstrucción con pattern matching (Java 21+)

```java
if (resultado instanceof PagoExitoso(String transaccionId, BigDecimal monto)) {
    log.info("Pago exitoso: {} por {}", transaccionId, monto);
}
```

Ver [`sealed-classes.md`](./sealed-classes.md) para el uso combinado de records con jerarquías selladas en pattern matching exhaustivo.

## Checklist

- [ ] ¿Se ha considerado `record` antes de escribir una clase inmutable manual o usar `@Value` de Lombok?
- [ ] ¿Las invariantes de construcción se validan en el constructor compacto?
- [ ] ¿Se ha evitado usar `record` para una Entidad de dominio con identidad propia (donde `equals` debe basarse solo en el ID)?

Ver también: [`sealed-classes.md`](./sealed-classes.md), [`../architecture/ddd.md`](../architecture/ddd.md), [`../../standards/dto.md`](../../standards/dto.md).
