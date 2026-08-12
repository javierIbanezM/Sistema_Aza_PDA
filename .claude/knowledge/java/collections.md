# Collections

> Ver [`streams.md`](./streams.md) para el procesamiento funcional de colecciones. Este documento cubre la elección de la estructura de datos correcta.

## Elección de la interfaz correcta

| Necesito | Usar | No usar |
|---|---|---|
| Lista ordenada, acceso por índice frecuente | `ArrayList` | `LinkedList` (acceso por índice O(n)) |
| Inserciones/borrados frecuentes en los extremos | `ArrayDeque` | `LinkedList` (peor localidad de caché) |
| Sin duplicados, orden de inserción irrelevante | `HashSet` | `ArrayList` con comprobación manual de duplicados |
| Sin duplicados, orden de inserción preservado | `LinkedHashSet` | — |
| Sin duplicados, ordenado | `TreeSet` | `HashSet` + sort manual repetido |
| Clave→valor, sin orden | `HashMap` | — |
| Clave→valor, orden de inserción preservado | `LinkedHashMap` | — |
| Clave→valor, ordenado por clave | `TreeMap` | — |
| Acceso concurrente sin bloqueo global | `ConcurrentHashMap` | `Collections.synchronizedMap(new HashMap<>())` (bloqueo global, peor throughput) |

`LinkedList` rara vez es la elección correcta en código moderno — `ArrayList`/`ArrayDeque` tienen mejor localidad de caché para la inmensa mayoría de casos de uso reales, incluso con inserciones frecuentes.

## Colecciones inmutables

```java
List<String> lista = List.of("a", "b", "c");     // inmutable, lanza UnsupportedOperationException al mutar
Map<String, Integer> mapa = Map.of("a", 1, "b", 2);
List<String> copiaInmutable = List.copyOf(listaMutable);
```

Devolver colecciones inmutables (`List.copyOf`, o `Collections.unmodifiableList`) desde getters de entidades de dominio evita que el código cliente mute el estado interno sin pasar por los métodos de negocio — ver [`../../anti-patterns/anemic-domain-model.md`](../../anti-patterns/anemic-domain-model.md).

## Comparadores

```java
pedidos.sort(Comparator.comparing(Pedido::getFechaCreacion).reversed());

pedidos.sort(
    Comparator.comparing(Pedido::getEstado)
        .thenComparing(Pedido::getFechaCreacion, Comparator.reverseOrder())
);
```

## Iteración segura con modificación

```java
// Lanza ConcurrentModificationException
for (Pedido p : pedidos) {
    if (p.estaCancelado()) pedidos.remove(p);
}

// Correcto
pedidos.removeIf(Pedido::estaCancelado);
```

`removeIf` (Java 8+) es la forma idiomática de filtrar-y-eliminar, evitando el error clásico de modificar una colección mientras se itera sobre ella con un `for-each`.

## `equals`/`hashCode` y colecciones

Un objeto usado como clave de `HashMap`/elemento de `HashSet` **debe** tener `equals`/`hashCode` consistentes y, preferiblemente, basados en campos inmutables — mutar un campo usado en `hashCode()` después de insertar el objeto en un `HashSet` lo vuelve "inencontrable" (queda en el bucket incorrecto). Ver [`../lombok/lombok.md`](../lombok/lombok.md) para el patrón recomendado en entidades (equals/hashCode basados solo en el ID, que es inmutable).

## Checklist

- [ ] ¿Se ha elegido la estructura de datos según el patrón de acceso real (lectura por índice vs. por clave vs. inserción en extremos)?
- [ ] ¿Se devuelven colecciones inmutables/copias desde getters de entidades, evitando mutación externa no controlada?
- [ ] ¿Se usa `removeIf`/streams en lugar de mutar una colección durante su propia iteración?
- [ ] ¿Los objetos usados como clave de `HashMap`/`HashSet` tienen campos inmutables en su `hashCode()`?

Ver también: [`streams.md`](./streams.md), [`../../anti-patterns/anemic-domain-model.md`](../../anti-patterns/anemic-domain-model.md).
