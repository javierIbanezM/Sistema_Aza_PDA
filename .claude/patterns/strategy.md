# Patrón: Strategy

## Descripción

Strategy define una familia de algoritmos intercambiables, encapsula cada uno en una clase independiente con una interfaz común, y permite que el algoritmo a usar se elija/inyecte en tiempo de ejecución sin que el código cliente conozca los detalles de cada variante concreta.

## Cuándo usarlo

- Cuando existe (o se espera de forma confirmada, no especulativa) más de una forma de realizar la misma operación, seleccionable en tiempo de ejecución (cálculo de descuentos por tipo de cliente, cálculo de impuestos por país, algoritmos de ordenación/enrutamiento intercambiables).
- Cuando un `if-else`/`switch` sobre un tipo se repite en varios lugares del código y crece cada vez que aparece un caso nuevo — señal directa de violación de OCP (ver [`../solid.md`](../knowledge/architecture/solid.md)).
- Cuando se quiere testear cada variante del algoritmo de forma aislada, sin necesidad de ejecutar el flujo completo que las contiene.

## Cuándo NO usarlo

- Cuando el conjunto de casos es cerrado, pequeño y estable (por ejemplo, un `switch` sobre los cuatro puntos cardinales) — un `if-else` simple es más legible que una jerarquía de clases (ver KISS en [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).
- Cuando solo existe una única variante real hoy y no hay ningún plan confirmado de añadir otra (ver YAGNI) — introducir Strategy de forma preventiva es abstracción especulativa.

## Ventajas

- Elimina condicionales complejos y crecientes, reemplazándolos por polimorfismo.
- Cada estrategia es testeable de forma aislada.
- Añadir una variante nueva no requiere modificar el código existente (cumple OCP).

## Desventajas

- Introduce una clase adicional por cada variante, lo que aumenta el número de archivos/clases del proyecto.
- Si las variantes son muy simples y estables, la indirección puede hacer el código más difícil de seguir que un condicional directo.

## Ejemplo

### Antes (violación de OCP, ver detalle completo en `../solid.md`)

```java
public BigDecimal calcularDescuento(Pedido pedido, String tipoCliente) {
    if (tipoCliente.equals("REGULAR")) return pedido.getTotal().multiply(BigDecimal.valueOf(0.95));
    if (tipoCliente.equals("VIP")) return pedido.getTotal().multiply(BigDecimal.valueOf(0.85));
    // cada tipo nuevo obliga a modificar este método
    return pedido.getTotal();
}
```

### Después (Strategy)

```java
public interface PoliticaDescuento {
    BigDecimal aplicar(Pedido pedido);
}

public class DescuentoRegular implements PoliticaDescuento {
    public BigDecimal aplicar(Pedido pedido) {
        return pedido.getTotal().multiply(BigDecimal.valueOf(0.95));
    }
}

public class DescuentoVip implements PoliticaDescuento {
    public BigDecimal aplicar(Pedido pedido) {
        return pedido.getTotal().multiply(BigDecimal.valueOf(0.85));
    }
}

public class CalculadoraDescuento {
    public BigDecimal calcular(Pedido pedido, PoliticaDescuento politica) {
        return politica.aplicar(pedido);
    }
}
```

### Selección de la estrategia con un registro (evita un nuevo condicional al elegir la estrategia)

```typescript
const politicasDescuento: Record<TipoCliente, PoliticaDescuento> = {
  REGULAR: new DescuentoRegular(),
  VIP: new DescuentoVip(),
  EMPLEADO: new DescuentoEmpleado(),
};

function calcularDescuento(pedido: Pedido, tipoCliente: TipoCliente): number {
  return politicasDescuento[tipoCliente].aplicar(pedido);
}
```

Usar un mapa/registro para seleccionar la estrategia evita reintroducir el mismo `if-else`/`switch` que el patrón pretendía eliminar, solo que en el punto de selección en lugar de en el de ejecución — aunque este punto de selección sigue siendo mucho más simple, ya que solo mapea un tipo a una instancia, sin contener lógica de negocio.

## Errores comunes

- Reintroducir un `switch` grande en el punto donde se selecciona la estrategia, sin usar un registro/mapa, perdiendo buena parte del beneficio de OCP.
- Crear estrategias con estado mutable compartido entre invocaciones concurrentes sin sincronización, cuando la misma instancia de estrategia se reutiliza entre requests.
- Aplicar Strategy a casos con una única variante real, violando YAGNI.

Ver también: [`../solid.md`](../knowledge/architecture/solid.md) (principio OCP), [`Factory.md`](./factory.md), [`Observer.md`](./observer.md).
