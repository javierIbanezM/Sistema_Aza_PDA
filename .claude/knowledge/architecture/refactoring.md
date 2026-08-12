# Refactoring

> Documento único de referencia sobre metodología y técnicas de refactoring.

## Definición

Refactoring es el proceso de cambiar la estructura interna de un código sin alterar su comportamiento observable externo. Un refactor que cambia el comportamiento no es un refactor: es una modificación funcional disfrazada de refactor, y debe tratarse (y testearse) como tal.

## Precondición obligatoria: red de seguridad

Nunca se refactoriza código sin una red de seguridad de tests que verifique el comportamiento actual. Si el código a refactorizar no tiene tests:

1. **Escribir tests de caracterización** primero: tests que documentan el comportamiento *actual* del código (incluidos posibles bugs conocidos, que se marcan explícitamente para no "arreglarlos" accidentalmente durante el refactor).
2. Ejecutar los tests de caracterización y confirmar que pasan contra el código sin modificar.
3. Solo entonces empezar el refactor, ejecutando los tests tras cada paso pequeño.

```typescript
// Test de caracterización de un método legado sin tests previos
describe('calcularDescuento (caracterización)', () => {
  it('aplica 10% para clientes con más de 5 años de antigüedad', () => {
    // Documenta el comportamiento actual, no necesariamente el "correcto"
    expect(calcularDescuento({ antiguedadAnios: 6, total: 100 })).toBe(90);
  });

  it('NO aplica descuento a antigüedad exacta de 5 años (posible off-by-one, documentado)', () => {
    expect(calcularDescuento({ antiguedadAnios: 5, total: 100 })).toBe(100);
  });
});
```

## Principios del refactor seguro

1. **Pasos pequeños**: cada cambio individual debe ser lo suficientemente pequeño para revertirlo mentalmente sin esfuerzo. Ejecutar los tests tras cada paso, no al final de una sesión completa de cambios.
2. **Un tipo de cambio a la vez**: no mezclar un rename con una extracción de método con un cambio de estructura de datos en el mismo paso. Cada uno es un commit potencial independiente.
3. **Herramientas automatizadas sobre cambios manuales**: usar el rename/extract-method del IDE en lugar de buscar y reemplazar a mano, para evitar errores de transcripción.
4. **No mezclar refactor con feature nueva**: un PR que refactoriza Y añade funcionalidad es más difícil de revisar y de revertir si algo falla. Separar en commits o PRs distintos siempre que sea viable.

## Catálogo de refactors comunes

### Extract Method (Extraer Método)

Cuando un fragmento de código dentro de un método más grande puede agruparse y nombrarse con un propósito propio.

```java
// Antes
public void procesarPedido(Pedido pedido) {
    double total = 0;
    for (LineaPedido linea : pedido.getLineas()) {
        total += linea.getPrecio() * linea.getCantidad();
    }
    if (total > 1000) {
        total = total * 0.95;
    }
    pedido.setTotal(total);
}

// Después
public void procesarPedido(Pedido pedido) {
    double total = calcularSubtotal(pedido);
    total = aplicarDescuentoPorVolumen(total);
    pedido.setTotal(total);
}

private double calcularSubtotal(Pedido pedido) {
    return pedido.getLineas().stream()
        .mapToDouble(l -> l.getPrecio() * l.getCantidad())
        .sum();
}

private double aplicarDescuentoPorVolumen(double total) {
    return total > 1000 ? total * 0.95 : total;
}
```

### Extract Class (Extraer Clase)

Cuando un grupo de campos y métodos dentro de una clase tienen cohesión propia y motivo de cambio distinto del resto (ver SRP en [`solid.md`](./solid.md)).

### Replace Conditional with Polymorphism

Cuando un `switch`/`if-else` se repite en varios lugares del código discriminando por el mismo tipo, y se espera que el conjunto de casos crezca. Ver [`solid.md`](./solid.md) (OCP) y [`../patterns/strategy.md`](../../patterns/strategy.md).

### Introduce Parameter Object

Cuando un método acumula muchos parámetros relacionados entre sí, agruparlos en un objeto con nombre propio.

```java
// Antes
void crearReserva(String nombreCliente, String emailCliente, LocalDate fechaInicio, LocalDate fechaFin, int numeroHabitacion) { }

// Después
void crearReserva(DatosCliente cliente, RangoFechas fechas, int numeroHabitacion) { }
```

Esto además suele ser el primer paso hacia identificar un Value Object de dominio (ver [`ddd.md`](./ddd.md)).

### Replace Magic Number/String with Named Constant

```java
// Antes
if (usuario.getEdad() >= 18) { }

// Después
private static final int EDAD_MINIMA_LEGAL = 18;
if (usuario.getEdad() >= EDAD_MINIMA_LEGAL) { }
```

### Inline (el refactor inverso)

A veces la abstracción existente (un método, una clase) ya no aporta claridad y es mejor "en-línearla" de vuelta. El refactor no es unidireccional: simplificar eliminando una abstracción que quedó obsoleta también es refactoring legítimo.

## Refactors de mayor envergadura

Para cambios estructurales grandes (cambiar de arquitectura en capas, dividir un monolito, cambiar el modelo de persistencia):

1. Documentar la decisión y el plan en un ADR (ver [`documentacion-tecnica.md`](./technical-documentation.md)).
2. Aplicar el patrón **Strangler Fig**: introducir la nueva estructura en paralelo a la antigua, migrar funcionalidad de forma incremental (feature por feature), y retirar la estructura antigua solo cuando ya no tiene consumidores.
3. Nunca hacer un "big bang rewrite" sin una razón de negocio que justifique el riesgo — la probabilidad de introducir regresiones graves es alta y la reversión es costosa.

## Cuándo NO refactorizar

- Justo antes de una entrega crítica con poco margen de tiempo para verificar regresiones.
- Sobre código que va a eliminarse o reemplazarse en el corto plazo (no invertir esfuerzo en mejorar algo que no va a vivir).
- Como excusa para reescribir código que funciona correctamente solo porque el estilo no coincide con la preferencia personal de quien lo toca — el refactor debe resolver un problema real de mantenibilidad, testabilidad o extensibilidad.

## Checklist

- [ ] ¿Existen tests que verifican el comportamiento actual antes de empezar?
- [ ] ¿Cada paso del refactor es lo suficientemente pequeño para verificarse de forma aislada?
- [ ] ¿Los tests siguen en verde después de cada paso?
- [ ] ¿El refactor está separado de cualquier cambio funcional nuevo en el mismo PR?
- [ ] ¿El comportamiento observable externo es idéntico antes y después?

Ver también: [`testing-strategy.md`](./testing.md), [`solid.md`](./solid.md), [`../anti-patterns/spaghetti.md`](../../anti-patterns/spaghetti.md).
