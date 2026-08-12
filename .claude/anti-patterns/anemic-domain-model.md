# Antipatrón: Anemic Domain Model (Modelo de Dominio Anémico)

## Descripción

Las entidades de dominio son simples contenedores de datos (solo getters/setters, sin comportamiento propio), mientras que toda la lógica de negocio vive en "servicios" externos que manipulan esos datos desde fuera. Aunque las clases se llamen igual que los conceptos del dominio (`Pedido`, `Cliente`), el diseño es en realidad procedural: el comportamiento y los datos que debería encapsular están separados, lo que contradice el principio básico de la programación orientada a objetos y de DDD (ver [`../ddd.md`](../knowledge/architecture/ddd.md)).

## Cómo detectarlo

- Entidades con únicamente getters/setters públicos (`@Data` de Lombok aplicado a entidades de dominio sin ningún método de negocio propio).
- "Servicios" que reciben entidades, leen sus campos con getters, aplican lógica de negocio externamente, y vuelven a escribir el resultado con setters.
- Invariantes de negocio (reglas que siempre deben cumplirse) validadas de forma dispersa en cada servicio que toca la entidad, en lugar de garantizadas por la propia entidad.
- Es posible poner la entidad en un estado inválido desde cualquier punto del código, porque nada impide llamar a un setter con un valor que rompe una regla de negocio.

## Ejemplo del antipatrón

```java
// Entidad anémica: solo datos, sin comportamiento
public class Pedido {
    private String id;
    private List<LineaPedido> lineas;
    private EstadoPedido estado;
    // getters y setters de todo, sin ninguna lógica
}

// Toda la lógica de negocio vive fuera, manipulando la entidad como una simple bolsa de datos
public class PedidoService {
    public void confirmar(Pedido pedido) {
        if (pedido.getLineas().isEmpty()) {
            throw new IllegalStateException("No se puede confirmar sin líneas");
        }
        pedido.setEstado(EstadoPedido.CONFIRMADO); // nada impide llamar esto desde cualquier otro lugar sin la validación
    }

    public void agregarLinea(Pedido pedido, LineaPedido linea) {
        if (pedido.getEstado() != EstadoPedido.BORRADOR) {
            throw new IllegalStateException("No se puede modificar un pedido confirmado");
        }
        pedido.getLineas().add(linea); // exposición directa de la lista mutable
    }
}
```

El problema central: `pedido.setEstado(EstadoPedido.CONFIRMADO)` se puede llamar desde **cualquier** parte del código sin pasar por la validación de `PedidoService.confirmar()`. La entidad no tiene forma de protegerse a sí misma de un uso incorrecto — la responsabilidad de mantener la invariante recae enteramente en la disciplina de quien escribe cada servicio, lo cual es frágil y no escala con el tamaño del equipo/proyecto.

## Cómo evitarlo

- Mover el comportamiento a la entidad: los métodos que cambian el estado de la entidad deben vivir en la propia entidad, validando sus invariantes internamente (ver ejemplo corregido en [`../ddd.md`](../knowledge/architecture/ddd.md), sección de Agregados).
- Eliminar setters públicos que permitan mutar el estado sin pasar por una regla de negocio. Exponer únicamente métodos con nombre de negocio explícito (`confirmar()`, `agregarLinea()`, `cancelar()`), nunca `setEstado()`/`setLineas()` genéricos.
- No exponer colecciones mutables directamente (`getLineas()` no debe devolver la lista interna modificable; devolver una vista inmutable o una copia).
- Reservar los "servicios" para lógica que genuinamente no pertenece a una única entidad (Domain Services, ver [`../ddd.md`](../knowledge/architecture/ddd.md)) — por ejemplo, una operación que involucra a dos Agregados distintos — no como contenedor por defecto de toda la lógica de negocio.

### Ejemplo corregido

```java
public class Pedido {
    private final String id;
    private final List<LineaPedido> lineas = new ArrayList<>();
    private EstadoPedido estado = EstadoPedido.BORRADOR;

    public void agregarLinea(LineaPedido linea) {
        if (estado != EstadoPedido.BORRADOR) {
            throw new IllegalStateException("No se puede modificar un pedido confirmado");
        }
        lineas.add(linea);
    }

    public void confirmar() {
        if (lineas.isEmpty()) {
            throw new IllegalStateException("No se puede confirmar sin líneas");
        }
        this.estado = EstadoPedido.CONFIRMADO;
    }

    public List<LineaPedido> getLineas() {
        return Collections.unmodifiableList(lineas); // no se puede mutar desde fuera
    }

    // No existe setEstado() público: el estado solo cambia a través de métodos con reglas de negocio
}
```

Ahora es **imposible** poner el `Pedido` en un estado inválido desde fuera de la clase — la propia entidad garantiza sus invariantes en cualquier punto del código que la use, sin depender de la disciplina de cada desarrollador que la manipule.

## Checklist

- [ ] ¿Las entidades de dominio tienen métodos de negocio propios, o son solo getters/setters?
- [ ] ¿Es posible poner una entidad en un estado inválido llamando a un setter directamente, sin pasar por un método que valide la invariante?
- [ ] ¿Los "servicios" del proyecto contienen lógica que en realidad pertenece a una entidad concreta?
- [ ] ¿Se exponen colecciones internas de forma mutable desde los getters?

Ver también: [`../ddd.md`](../knowledge/architecture/ddd.md), [`../solid.md`](../knowledge/architecture/solid.md), [`../patterns/repository.md`](../patterns/repository.md).
