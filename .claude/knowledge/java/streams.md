# Streams (Java)

> Ver [`collections.md`](./collections.md) para la elección de la estructura de datos subyacente que un stream recorre.

## Cuándo usar streams y cuándo un bucle simple

Streams aportan claridad en transformaciones/filtrados/agregaciones declarativas. Un bucle `for` explícito sigue siendo preferible cuando la lógica tiene múltiples efectos secundarios entrelazados o rupturas de flujo complejas (`break`/`continue` condicionales anidados) — forzar esa lógica dentro de un stream reduce la legibilidad en lugar de aumentarla (ver KISS en [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)).

## Operaciones esenciales

```java
List<String> nombresVip = clientes.stream()
    .filter(c -> c.getTipo() == TipoCliente.VIP)
    .map(Cliente::getNombre)
    .sorted()
    .toList(); // Java 16+; antes: .collect(Collectors.toList())

BigDecimal totalPedidos = pedidos.stream()
    .map(Pedido::calcularTotal)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

Map<EstadoPedido, List<Pedido>> pedidosPorEstado = pedidos.stream()
    .collect(Collectors.groupingBy(Pedido::getEstado));

Map<String, BigDecimal> totalPorCliente = pedidos.stream()
    .collect(Collectors.groupingBy(
        Pedido::getClienteId,
        Collectors.reducing(BigDecimal.ZERO, Pedido::calcularTotal, BigDecimal::add)
    ));
```

## `Optional` como resultado de un stream

```java
Optional<Pedido> pedidoMasReciente = pedidos.stream()
    .max(Comparator.comparing(Pedido::getFechaCreacion));

pedidoMasReciente.ifPresentOrElse(
    p -> log.info("Pedido más reciente: {}", p.getId()),
    () -> log.info("No hay pedidos")
);
```

## Streams paralelos: no por defecto

```java
// Solo si el volumen de datos y el coste por elemento lo justifican, y se ha medido
resultado = listaGrande.parallelStream()
    .map(this::calculoCostoso)
    .toList();
```

`parallelStream()` usa el `ForkJoinPool` común compartido por toda la JVM — un uso indiscriminado puede degradar el rendimiento de **otras** partes de la aplicación que también dependen de ese pool (incluyendo `CompletableFuture` sin executor explícito). Usar solo tras medir con [`performance.md`](./performance.md) que el coste de paralelización compensa, con datasets suficientemente grandes y operaciones suficientemente costosas por elemento.

## Errores comunes

```java
// Efecto secundario dentro de un stream — antipatrón, dificulta razonar sobre el flujo
List<Pedido> resultado = new ArrayList<>();
pedidos.stream().forEach(p -> { if (p.esValido()) resultado.add(p); }); // usar .filter().toList()

// Reutilizar un stream ya consumido — lanza IllegalStateException
Stream<Pedido> stream = pedidos.stream();
stream.count();
stream.forEach(System.out::println); // falla: el stream ya se consumió
```

Un `Stream` se consume una única vez — si se necesita recorrer los datos más de una vez, crear un nuevo stream desde la colección origen (`pedidos.stream()` de nuevo) o materializar el resultado intermedio (`.toList()`) y trabajar sobre la lista.

## Streams infinitos con `limit`

```java
Stream.iterate(1, n -> n * 2)
    .limit(10)
    .forEach(System.out::println); // potencias de 2 hasta la décima
```

Siempre acotar con `.limit()` un stream generado con `Stream.iterate`/`Stream.generate` — sin límite, es un stream infinito que nunca termina de evaluarse en una operación terminal.

## Checklist

- [ ] ¿El stream mejora la legibilidad frente a un bucle explícito, o la reduce por lógica entrelazada compleja?
- [ ] ¿Se ha evitado un efecto secundario dentro de `.map()`/`.filter()` (deberían ser funciones puras)?
- [ ] ¿`parallelStream()` se usa solo tras medir que aporta beneficio real, no por defecto?
- [ ] ¿Se ha evitado reutilizar un `Stream` ya consumido?

Ver también: [`collections.md`](./collections.md), [`performance.md`](./performance.md), [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md).
