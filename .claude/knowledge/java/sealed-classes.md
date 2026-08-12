# Sealed Classes (Java 17+)

> Ver [`records.md`](./records.md) para el uso combinado idiomático de sealed interfaces + records en la representación de estados/resultados.

## Qué resuelven

Una `sealed` class/interface restringe explícitamente qué clases pueden extenderla/implementarla, declaradas en la cláusula `permits`. Combinado con pattern matching en `switch`, el compilador puede verificar **exhaustividad**: si se añade un nuevo caso a la jerarquía sellada y algún `switch` no lo contempla, falla en tiempo de compilación en lugar de en producción.

## Modelar un conjunto cerrado de estados

```java
public sealed interface ResultadoPago
    permits PagoExitoso, PagoRechazado, PagoPendiente {}

public record PagoExitoso(String transaccionId, BigDecimal monto) implements ResultadoPago {}
public record PagoRechazado(String motivo) implements ResultadoPago {}
public record PagoPendiente(String referencia) implements ResultadoPago {}
```

```java
String mensaje = switch (resultado) {
    case PagoExitoso(var transaccionId, var monto) ->
        "Pago %s exitoso por %s".formatted(transaccionId, monto);
    case PagoRechazado(var motivo) ->
        "Pago rechazado: " + motivo;
    case PagoPendiente(var referencia) ->
        "Pago pendiente, referencia " + referencia;
    // sin "default": el compilador VERIFICA que los tres casos posibles están cubiertos
};
```

Si mañana se añade `PagoEnDisputa` a la lista de `permits`, este `switch` **deja de compilar** hasta que se añada el caso correspondiente — a diferencia de un enum + `if-else`, donde olvidar un caso nuevo simplemente no se detecta hasta que ocurre en producción.

## Comparación con enum

| | `enum` | `sealed interface` + `record` |
|---|---|---|
| Estados sin datos asociados | Ideal | Sobre-ingeniería |
| Estados con datos distintos por caso | Requiere campos opcionales/nulos según el caso (frágil) | Cada caso lleva exactamente los datos que necesita |
| Exhaustividad verificada en compilación | Sí (switch exhaustivo sobre enum) | Sí |

Usar `enum` para estados simples sin datos asociados (`EstadoPedido { BORRADOR, CONFIRMADO, CANCELADO }`); usar `sealed` + `record` cuando cada caso necesita llevar datos distintos (un resultado de pago exitoso lleva `transaccionId`, uno rechazado lleva `motivo` — forzar ambos en la misma clase con campos opcionales sería un antipatrón de diseño).

## Sealed classes para jerarquías de dominio controladas

```java
public sealed abstract class Notificacion permits NotificacionEmail, NotificacionSms, NotificacionPush {
    public abstract void enviar();
}

public final class NotificacionEmail extends Notificacion {
    @Override public void enviar() { /* ... */ }
}
```

Útil cuando el dominio de negocio define explícitamente un conjunto cerrado de variantes (canales de notificación soportados, tipos de descuento aplicables) que no deben extenderse sin control desde fuera del módulo — refuerza en tiempo de compilación una decisión de diseño que de otro modo dependería solo de disciplina de equipo.

## `permits` implícito en el mismo archivo

```java
public sealed interface Forma {
    record Circulo(double radio) implements Forma {}
    record Rectangulo(double ancho, double alto) implements Forma {}
}
```

Si todas las implementaciones están en el mismo archivo que la interfaz sellada, la cláusula `permits` es opcional — el compilador la infiere.

## Checklist

- [ ] ¿El dominio tiene un conjunto verdaderamente cerrado de variantes que se beneficiaría de exhaustividad verificada en compilación?
- [ ] ¿Se ha evaluado `sealed` + `record` frente a `enum` cuando cada variante lleva datos distintos?
- [ ] ¿Los `switch` sobre el tipo sellado evitan un `default` innecesario, para que el compilador detecte casos nuevos no contemplados?

Ver también: [`records.md`](./records.md), [`../../patterns/strategy.md`](../../patterns/strategy.md) (alternativa OO clásica al mismo problema).
