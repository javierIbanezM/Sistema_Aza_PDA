# Patrón: Builder

## Descripción

Builder separa la construcción de un objeto complejo (con muchos parámetros, algunos opcionales) de su representación final, permitiendo construirlo paso a paso con una API fluida y legible, evitando constructores con listas largas de parámetros posicionales.

## Cuándo usarlo

- Cuando un objeto tiene muchos campos, especialmente si varios son opcionales, y un constructor tradicional se volvería ambiguo o propenso a errores de orden de argumentos.
- Cuando la construcción del objeto requiere pasos condicionales o validaciones intermedias antes de ensamblar el resultado final.
- Para construir objetos de test con valores por defecto razonables y solo sobrescribir los campos relevantes para cada test concreto (Test Data Builder).

## Cuándo NO usarlo

- Para clases con pocos campos (2-3), todos obligatorios — un constructor normal o un `record`/data class es más simple y directo (ver KISS en [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).
- Cuando el lenguaje ya ofrece parámetros con nombre y valores por defecto de forma nativa de manera suficientemente legible (ej. Kotlin con `data class` y `named arguments` suele necesitar Builder con mucha menos frecuencia que Java).

## Ventajas

- Legibilidad: `Pedido.builder().cliente(c).fechaEntrega(f).build()` es más claro que `new Pedido(c, null, null, f, null, true)`.
- Permite validar el objeto completo en el método `build()`, garantizando que nunca existe en un estado a medio construir e inválido.
- Facilita la creación de variantes en tests (Test Data Builder) sin repetir la construcción completa en cada test.

## Desventajas

- Añade código repetitivo (una clase Builder por cada clase compleja), mitigado en Java con Lombok (`@Builder`).
- Puede ocultar qué campos son realmente obligatorios si no se fuerzan explícitamente en la API del Builder (ver ejemplo de Builder por pasos más abajo).

## Ejemplos

### Builder clásico (Java, con Lombok)

```java
@Builder
public class Pedido {
    private final String clienteId;
    private final LocalDate fechaEntrega;
    private final List<LineaPedido> lineas;
    private final String notas; // opcional
}

// Uso
Pedido pedido = Pedido.builder()
    .clienteId("c-123")
    .fechaEntrega(LocalDate.now().plusDays(3))
    .lineas(lineas)
    .build(); // "notas" queda null, es opcional
```

### Builder por pasos (fuerza el orden de campos obligatorios en tiempo de compilación)

```java
public class PeticionHttpBuilder {

    public interface ConUrl {
        ConMetodo url(String url);
    }
    public interface ConMetodo {
        Buildable metodo(String metodo);
    }
    public interface Buildable {
        Buildable header(String nombre, String valor);
        PeticionHttp build();
    }

    // La implementación va exponiendo solo la siguiente interfaz válida en cada paso,
    // haciendo imposible en tiempo de compilación construir una petición sin URL o sin método.
}
```

Esta variante (a veces llamada *Staged Builder* o *Telescoping Builder tipado*) es más costosa de implementar, pero elimina en tiempo de compilación la posibilidad de olvidar un campo obligatorio — útil cuando las consecuencias de un objeto mal construido son graves.

### Test Data Builder

```typescript
class PedidoTestBuilder {
  private clienteId = 'cliente-default';
  private lineas: LineaPedido[] = [{ productoId: 'p-1', cantidad: 1 }];
  private estado: EstadoPedido = EstadoPedido.BORRADOR;

  conCliente(clienteId: string): this { this.clienteId = clienteId; return this; }
  conLineas(lineas: LineaPedido[]): this { this.lineas = lineas; return this; }
  confirmado(): this { this.estado = EstadoPedido.CONFIRMADO; return this; }

  build(): Pedido {
    return Pedido.reconstruir('p-test', this.clienteId, this.lineas, this.estado);
  }
}

// En un test, solo se especifica lo relevante para ese caso:
const pedidoSinLineas = new PedidoTestBuilder().conLineas([]).build();
```

Este patrón reduce drásticamente el ruido en los tests, ya que cada test solo declara los campos que le importan para su escenario concreto, con valores por defecto razonables para el resto.

## Errores comunes

- Builder con `build()` que no valida el estado final, permitiendo construir objetos inválidos igual que un constructor sin validación.
- Builders mutables compartidos entre threads sin sincronización, cuando se reutiliza la misma instancia de Builder para construir varios objetos concurrentemente.
- Usar Builder para objetos inmutables simples de 2-3 campos, añadiendo complejidad sin necesidad real.

Ver también: [`Factory.md`](./factory.md), [`../ddd.md`](../knowledge/architecture/ddd.md) (Value Objects), [`../testing.md`](../knowledge/architecture/testing.md).
