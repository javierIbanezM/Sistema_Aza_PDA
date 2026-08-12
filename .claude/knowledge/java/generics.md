# Generics

> Ver [`../architecture/solid.md`](../architecture/solid.md) (ISP, DIP) para el razonamiento de diseño de interfaces genéricas reutilizables.

## Wildcards: `? extends` vs. `? super` (PECS)

**PECS: Producer Extends, Consumer Super.**

```java
// El método SOLO LEE de la lista (produce elementos hacia el llamador) → extends
public BigDecimal sumarTotales(List<? extends Pedido> pedidos) {
    return pedidos.stream().map(Pedido::calcularTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
}

// El método SOLO ESCRIBE en la lista (consume elementos del llamador) → super
public void agregarPedidosPendientes(List<? super PedidoUrgente> destino) {
    destino.add(new PedidoUrgente(/* ... */));
}
```

Sin wildcards, un método `sumarTotales(List<Pedido> pedidos)` no aceptaría un `List<PedidoUrgente>` aunque `PedidoUrgente extends Pedido`, porque los genéricos de Java son invariantes por defecto — `List<? extends Pedido>` relaja esa restricción específicamente para el caso de solo-lectura.

## Métodos genéricos

```java
public static <T extends Comparable<T>> T maximo(List<T> lista) {
    return lista.stream().max(Comparator.naturalOrder()).orElseThrow();
}
```

El parámetro de tipo `<T>` se declara antes del tipo de retorno — necesario cuando el tipo genérico no está ya ligado a la clase que contiene el método (métodos estáticos, o métodos que introducen su propio parámetro de tipo independiente de la clase).

## Bounded type parameters

```java
public class Repositorio<T extends EntidadBase> {
    // T garantizado a tener los métodos de EntidadBase (getId(), etc.)
}
```

## Type erasure: implicaciones prácticas

Los genéricos de Java existen solo en tiempo de compilación — en runtime, `List<String>` y `List<Integer>` son ambos simplemente `List`. Implicaciones:

```java
// No se puede hacer esto: el tipo genérico no existe en runtime
if (lista instanceof List<String>) { } // error de compilación

// Tampoco se puede crear un array de un tipo genérico directamente
T[] array = new T[10]; // error de compilación
```

Al diseñar APIs genéricas, no depender de comprobar el tipo parametrizado en runtime — si se necesita esa información, pasarla explícitamente (`Class<T>` como parámetro) en lugar de intentar inferirla por reflexión sobre el genérico erosionado.

## Genéricos en Repository/patrones (aplicación práctica)

```java
public interface Repository<T, ID> {
    void guardar(T entidad);
    Optional<T> buscarPorId(ID id);
}

public interface PedidoRepository extends Repository<Pedido, PedidoId> {
    List<Pedido> buscarPendientesDeEnvio(); // métodos específicos del agregado, más allá del CRUD genérico
}
```

Ver [`../../patterns/repository.md`](../../patterns/repository.md) — un `Repository<T, ID>` genérico como base es aceptable para las operaciones CRUD comunes, siempre que cada Repository concreto añada las operaciones específicas de su Agregado en lugar de forzar todo a través de una interfaz genérica única (ver el error común documentado en `repository.md` sobre repositorios "todo genérico").

## Checklist

- [ ] ¿Se ha aplicado PECS (`? extends` para lectura, `? super` para escritura) en parámetros de métodos genéricos?
- [ ] ¿Se evita depender de comprobar el tipo parametrizado en runtime (`instanceof List<String>`)?
- [ ] ¿Una interfaz genérica base (`Repository<T, ID>`) se complementa con interfaces específicas por Agregado, sin forzar todo a la genérica?

Ver también: [`../architecture/solid.md`](../architecture/solid.md), [`../../patterns/repository.md`](../../patterns/repository.md).
