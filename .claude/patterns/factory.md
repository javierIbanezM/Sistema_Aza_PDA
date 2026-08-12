# Patrón: Factory

## Descripción

Factory encapsula la lógica de creación de objetos complejos o con variantes, evitando que el código cliente conozca los detalles de qué clase concreta instanciar o cómo construirla correctamente. Existen dos variantes principales: **Factory Method** (un método, normalmente en una subclase o interfaz, decide qué implementación concreta crear) y **Abstract Factory** (una familia de métodos de creación relacionados, agrupados en una interfaz).

## Cuándo usarlo

- Cuando la creación de un objeto requiere lógica condicional no trivial (elegir la implementación según un tipo, configuración o entorno).
- Cuando se quiere desacoplar el código cliente de las clases concretas que se instancian, permitiendo añadir nuevas variantes sin modificar el código cliente (cumple OCP, ver [`../solid.md`](../knowledge/architecture/solid.md)).
- Cuando la construcción de un Agregado (ver [`../ddd.md`](../knowledge/architecture/ddd.md)) requiere validar invariantes complejas antes de que el objeto exista en un estado válido.

## Cuándo NO usarlo

- Para instanciar clases simples con un único constructor y sin variantes — usar `new` directamente es más claro (ver KISS en [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).
- Cuando solo existe una implementación real y no hay ningún plan confirmado de añadir otra (ver YAGNI) — introducir una Factory para un único caso es indirección sin beneficio.

## Ventajas

- Centraliza la lógica de creación en un único lugar, evitando que se disperse por todo el código cliente.
- Facilita añadir nuevas variantes sin tocar el código que consume la Factory.
- Permite encapsular validaciones de construcción que garantizan que el objeto nace en un estado válido (útil en combinación con Value Objects, ver [`../ddd.md`](../knowledge/architecture/ddd.md)).

## Desventajas

- Añade una capa de indirección adicional que puede dificultar seguir el flujo de "qué clase concreta se está usando realmente" al leer el código.
- Mal aplicada a jerarquías pequeñas y estables, genera complejidad innecesaria.

## Ejemplos

### Factory Method simple

```java
public interface ProcesadorDocumento {
    void procesar(byte[] contenido);
}

public class ProcesadorDocumentoFactory {
    public static ProcesadorDocumento crear(TipoDocumento tipo) {
        return switch (tipo) {
            case PDF -> new ProcesadorPdf();
            case CSV -> new ProcesadorCsv();
            case XML -> new ProcesadorXml();
        };
    }
}

// Uso
ProcesadorDocumento procesador = ProcesadorDocumentoFactory.crear(tipo);
procesador.procesar(contenido);
```

### Factory como método estático de creación de dominio (garantiza invariantes)

```typescript
export class Pedido {
  private constructor(
    public readonly id: string,
    private lineas: LineaPedido[],
    private estado: EstadoPedido
  ) {}

  // Factory method: único punto de entrada para crear un Pedido válido
  static crear(id: string): Pedido {
    if (!id || id.trim().length === 0) {
      throw new Error('El id del pedido no puede estar vacío');
    }
    return new Pedido(id, [], EstadoPedido.BORRADOR);
  }

  // Factory method para reconstrucción desde persistencia (sin re-validar invariantes de creación)
  static reconstruir(id: string, lineas: LineaPedido[], estado: EstadoPedido): Pedido {
    return new Pedido(id, lineas, estado);
  }
}
```

Este uso de Factory Method (constructor privado + métodos estáticos con nombre explícito) es habitual en DDD táctico: distingue entre "crear un objeto nuevo desde cero validando todas sus invariantes de creación" y "reconstruir un objeto existente desde datos ya persistidos y previamente válidos", evitando revalidar cada vez que se lee un objeto de la base de datos.

## Errores comunes

- Usar una Factory para devolver un tipo genérico (`Object`, `any`) en lugar de un tipo bien definido, perdiendo la seguridad de tipos que el patrón debería preservar.
- Mezclar lógica de negocio no relacionada con la creación dentro de la Factory (una Factory debe crear objetos, no ejecutar reglas de negocio posteriores a la creación).
- Anidar Factories de Factories sin necesidad real, generando indirección excesiva (ver [`../anti-patterns/spaghetti.md`](../anti-patterns/spaghetti.md)).

Ver también: [`../solid.md`](../knowledge/architecture/solid.md) (OCP), [`Builder.md`](./builder.md), [`Strategy.md`](./strategy.md).
