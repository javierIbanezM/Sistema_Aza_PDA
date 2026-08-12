# Spring Validation

> Ver [`../../standards/validation.md`](../../standards/validation.md) para la norma general de validación en dos capas. Este documento cubre la implementación concreta con Bean Validation en Spring.

## Validación declarativa en el Request DTO

```java
public record CrearPedidoRequest(
    @NotNull(message = "El clienteId es obligatorio")
    UUID clienteId,

    @NotEmpty(message = "El pedido debe tener al menos una línea")
    @Size(max = 100, message = "Un pedido no puede tener más de 100 líneas")
    List<@Valid LineaPedidoRequest> lineas,

    @Email(message = "El email de contacto no es válido")
    String emailContacto
) {}
```

`@Valid` en cascada (`List<@Valid LineaPedidoRequest>`) es necesario explícitamente — sin él, Bean Validation no valida los elementos internos de la colección, solo la existencia de la colección misma.

## Activación en el Controller

```java
@PostMapping
public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody CrearPedidoRequest request) {
    // si la validación falla, Spring lanza MethodArgumentNotValidException
    // ANTES de que este método se ejecute — el body del método asume datos ya válidos
}
```

Ver [`spring-web.md`](./spring-web.md) para el `@ExceptionHandler` que traduce `MethodArgumentNotValidException` al formato de error estándar (ver [`../../standards/api.md`](../../standards/api.md)).

## Validadores personalizados

```java
@Constraint(validatedBy = CodigoPostalValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CodigoPostalValido {
    String message() default "Código postal no válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class CodigoPostalValidator implements ConstraintValidator<CodigoPostalValido, String> {
    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        return valor != null && valor.matches("^\\d{5}$");
    }
}
```

Crear un validador personalizado cuando la regla de formato es específica del dominio/país y se repite en más de un DTO — evita reimplementar la misma expresión regular con `@Pattern` en cada campo.

## Validación condicional / grupos

```java
public interface AlCrear {}
public interface AlActualizar {}

public record PedidoRequest(
    @Null(groups = AlCrear.class) // no debe venir informado al crear
    @NotNull(groups = AlActualizar.class) // obligatorio al actualizar
    String id
) {}
```

```java
@PostMapping
public void crear(@Validated(AlCrear.class) @RequestBody PedidoRequest request) { }

@PutMapping("/{id}")
public void actualizar(@Validated(AlActualizar.class) @RequestBody PedidoRequest request) { }
```

Los grupos de validación permiten reutilizar el mismo DTO con reglas distintas según la operación — evaluar primero si, siguiendo [`../../standards/dto.md`](../../standards/dto.md), es preferible tener dos DTOs distintos (`CrearPedidoRequest`/`ActualizarPedidoRequest`) en lugar de un único DTO con grupos condicionales, que suele ser más legible salvo que ambos compartan la inmensa mayoría de campos y reglas.

## Validación de invariantes de negocio: NO aquí

Bean Validation valida **forma** (formato, presencia, rangos básicos) — las invariantes de negocio (`fechaFin` debe ser posterior a `fechaInicio` en el contexto de una reserva ya existente en base de datos, un pedido no puede confirmarse dos veces) viven en el dominio, no en anotaciones del DTO. Ver [`../../standards/validation.md`](../../standards/validation.md) para la distinción completa entre las dos capas de validación.

## Checklist

- [ ] ¿Todo Request DTO tiene las anotaciones de Bean Validation correspondientes a sus reglas de forma?
- [ ] ¿Las colecciones anidadas usan `@Valid` en cascada explícitamente?
- [ ] ¿Se ha evitado meter reglas de negocio (que dependen del estado del sistema) dentro de anotaciones de validación de forma?
- [ ] ¿Los mensajes de validación son específicos y en el idioma del proyecto?

Ver también: [`../../standards/validation.md`](../../standards/validation.md), [`spring-web.md`](./spring-web.md), [`../../standards/dto.md`](../../standards/dto.md).
