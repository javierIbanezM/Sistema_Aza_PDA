# Pitfalls (Java — lenguaje) — Errores frecuentes

> Errores específicos del **lenguaje Java puro**. Para trampas específicas de Spring, Hibernate, Lombok o el build, ver el documento correspondiente en su propia carpeta (`../spring/`, `../hibernate/`, `../lombok/`, `../maven/`, `../gradle/`) — cada uno documenta sus propios errores comunes, no se duplican aquí. Para anti-patrones de diseño generales (God Object, N+1 Query...) ver [`../../anti-patterns/`](../../anti-patterns/).

## `Optional` como parámetro de método o campo de entidad

```java
// Evitar
public void procesar(Optional<String> notas) { /* ... */ }

// Correcto: parámetro nullable explícito, Optional solo en el retorno
public void procesar(String notas) { /* ... */ }
```

Ver [`language.md`](./language.md).

## Reutilizar un `Stream` ya consumido

```java
Stream<Pedido> stream = pedidos.stream();
stream.count();
stream.forEach(System.out::println); // IllegalStateException: el stream ya se consumió
```

Ver [`streams.md`](./streams.md).

## Modificar una colección mientras se itera con `for-each`

```java
for (Pedido p : pedidos) {
    if (p.estaCancelado()) pedidos.remove(p); // ConcurrentModificationException
}
// Correcto: pedidos.removeIf(Pedido::estaCancelado);
```

Ver [`collections.md`](./collections.md).

## `synchronized` con virtual threads (pinning)

```java
synchronized (lock) {
    resultado = llamadaDeRedBloqueante(); // "clava" el virtual thread al platform thread subyacente
}
```

Ver [`virtual-threads.md`](./virtual-threads.md) — usar `ReentrantLock` en su lugar dentro de código que se ejecuta en virtual threads.

## Capturar `Exception` genérico fuera del borde de la aplicación

```java
try {
    procesarPedido(pedido);
} catch (Exception e) {
    log.error("Error", e); // silencia el tipo real, dificulta diferenciar errores de negocio de técnicos
}
```

Ver [`exceptions.md`](../../standards/exceptions.md) y [`../../standards/exceptions.md`](../../standards/exceptions.md).

## `record` para una Entidad con identidad propia

```java
// Incorrecto: un record compara por valor de TODOS los campos, no por identidad
public record Pedido(String id, EstadoPedido estado, List<LineaPedido> lineas) {}
```

Una Entidad de dominio (ver [`../architecture/ddd.md`](../architecture/ddd.md)) debe comparar por ID únicamente — un `record` no es la herramienta correcta aquí. Ver [`records.md`](./records.md).

## `equals`/`hashCode` sobre campos mutables usados como clave de `HashMap`

Mutar un campo usado en `hashCode()` después de insertar el objeto en un `HashSet`/`HashMap` lo vuelve "inencontrable". Ver [`collections.md`](./collections.md).

## Checklist de revisión rápida

- [ ] ¿Hay algún `Optional` usado como parámetro o campo de entidad?
- [ ] ¿Se reutiliza algún `Stream` ya consumido?
- [ ] ¿Hay capturas de `Exception` genérico fuera del manejador global?
- [ ] ¿Se ha usado `record` para modelar una Entidad con identidad propia por error?

Ver también: [`best-practices.md`](./best-practices.md), [`../../anti-patterns/`](../../anti-patterns/).
