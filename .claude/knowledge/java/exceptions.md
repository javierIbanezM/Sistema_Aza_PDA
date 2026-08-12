# Exceptions (Java)

> Ver [`../../standards/exceptions.md`](../../standards/exceptions.md) para la norma prescriptiva transversal (jerarquía, manejo, mensajes). Este documento cubre la mecánica **específica del lenguaje Java**: checked vs. unchecked, try-with-resources, multi-catch.

## Checked vs. Unchecked

```java
public class PedidoNoEncontradoException extends RuntimeException { // unchecked, no declarada en throws
    public PedidoNoEncontradoException(PedidoId id) {
        super("Pedido no encontrado: " + id.valor());
    }
}
```

Este framework usa **excepciones unchecked** (`extends RuntimeException`) para excepciones de dominio, siguiendo la práctica moderna de Spring y la mayoría de frameworks Java actuales — las excepciones checked (`extends Exception`, con `throws` obligatorio en la firma) tienden a filtrarse a través de capas que no pueden hacer nada útil con ellas, obligando a `catch` vacíos o relanzamientos mecánicos solo para satisfacer al compilador. Ver [`../../standards/exceptions.md`](../../standards/exceptions.md) para la jerarquía completa recomendada (`DomainException` como base).

## `try-with-resources`

```java
try (BufferedReader reader = new BufferedReader(new FileReader(ruta));
     var conexion = dataSource.getConnection()) {
    // ambos recursos se cierran automáticamente, en orden inverso, incluso si ocurre una excepción
}
```

Cualquier clase que implemente `AutoCloseable` puede usarse aquí — obligatorio para archivos, conexiones, streams, y cualquier recurso propio que gestione algo externo a la JVM (ver también [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md)).

## Multi-catch

```java
try {
    procesarPedido(pedido);
} catch (PedidoNoEncontradoException | PedidoYaConfirmadoException e) {
    // mismo manejo para ambos tipos relacionados, sin duplicar el bloque catch
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e));
}
```

Usar quando dos o más tipos de excepción requieren exactamente el mismo manejo — si el manejo difiere, preferir bloques `catch` separados por claridad, incluso si hay algo de repetición.

## Excepciones personalizadas con contexto estructurado

```java
public class PedidoNoEncontradoException extends DomainException {
    private final String pedidoId;

    public PedidoNoEncontradoException(String pedidoId) {
        super("Pedido no encontrado: " + pedidoId);
        this.pedidoId = pedidoId;
    }

    public String getPedidoId() { return pedidoId; } // disponible para el manejador global, no solo el mensaje
}
```

Exponer los datos relevantes como campos, no solo embebidos en el mensaje de texto — permite que el `@RestControllerAdvice` (ver [`../spring/spring-web.md`](../spring/spring-web.md)) construya una respuesta estructurada sin parsear el mensaje.

## Suprimir excepciones (`try` con múltiples fallos)

```java
try (var recurso1 = abrirRecurso1(); var recurso2 = abrirRecurso2()) {
    // si recurso1.close() falla DESPUÉS de que el bloque ya lanzó una excepción,
    // la excepción de close() se añade como "suppressed", no reemplaza a la original
} catch (Exception e) {
    for (Throwable suprimida : e.getSuppressed()) {
        log.warn("Excepción suprimida durante el cierre", suprimida);
    }
}
```

`try-with-resources` preserva la excepción original como la principal y añade cualquier fallo posterior de `close()` como "suprimida" — evita perder la causa raíz real cuando tanto la operación como la limpieza de recursos fallan.

## Checklist

- [ ] ¿Las excepciones de dominio son unchecked, extendiendo una base común propia del proyecto?
- [ ] ¿Se usa `try-with-resources` para todo recurso `AutoCloseable`?
- [ ] ¿Las excepciones personalizadas exponen los datos relevantes como campos, no solo en el mensaje de texto?

Ver también: [`../../standards/exceptions.md`](../../standards/exceptions.md), [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md).
