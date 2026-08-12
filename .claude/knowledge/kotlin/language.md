# Language (Kotlin)

> Fundamentos del lenguaje Kotlin, independientes de Android. Ver [`../android/`](../android/) para la plataforma que lo consume mayoritariamente en este framework.

## Null-safety en el sistema de tipos

```kotlin
var nombre: String = "Ana"       // no puede ser null, verificado en compilación
var apodo: String? = null        // tipo nullable explícito, requiere manejo

val longitud = apodo?.length     // safe call: null si apodo es null, sin excepción
val valor = apodo ?: "sin apodo" // elvis operator: valor por defecto si es null
val forzado = apodo!!.length     // non-null assertion: lanza NPE si es null — evitar salvo certeza absoluta
```

A diferencia de Java (ver [`../java/language.md`](../java/language.md)), la nulidad es parte del sistema de tipos — el compilador rechaza asignar `null` a un tipo no nullable, eliminando una clase entera de `NullPointerException` en tiempo de compilación. `!!` reintroduce ese riesgo deliberadamente; usarlo es una señal de que el diseño podría expresar la nulidad de forma más segura.

## Data classes: equivalente a los records de Java

```kotlin
data class Pedido(val id: String, val clienteId: String, val total: BigDecimal)
```

Genera automáticamente `equals`/`hashCode`/`toString`/`copy` basados en las propiedades del constructor primario — ver [`../java/records.md`](../java/records.md) para el mismo concepto en Java. `copy()` permite crear una nueva instancia con algunos campos modificados sin mutar la original: `pedido.copy(total = nuevoTotal)`.

## Sealed classes/interfaces: exhaustividad verificada

```kotlin
sealed interface ResultadoPago {
    data class Exitoso(val transaccionId: String) : ResultadoPago
    data class Rechazado(val motivo: String) : ResultadoPago
    object Pendiente : ResultadoPago
}

when (resultado) {
    is ResultadoPago.Exitoso -> "Pago exitoso: ${resultado.transaccionId}"
    is ResultadoPago.Rechazado -> "Rechazado: ${resultado.motivo}"
    ResultadoPago.Pendiente -> "Pendiente"
    // sin "else": el compilador exige cubrir todos los casos de la jerarquía sellada
}
```

Ver [`../java/sealed-classes.md`](../java/sealed-classes.md) para el mismo concepto en Java — Kotlin lo tiene desde mucho antes y es idiomático desde el principio del lenguaje.

## Extension functions

```kotlin
fun String.esEmailValido(): Boolean = matches(Regex("^[^@]+@[^@]+\\.[^@]+$"))

"test@ejemplo.com".esEmailValido() // true — añade comportamiento a un tipo existente sin heredar de él
```

Permite enriquecer APIs de terceros (o tipos del propio lenguaje) sin modificar su código fuente ni usar herencia — ampliamente usado en Android para dar una API más idiomática a clases del SDK.

## Scope functions: `let`, `run`, `apply`, `also`, `with`

```kotlin
val pedido = Pedido(id = "p-1", clienteId = "c-1", total = BigDecimal.ZERO).apply {
    // "apply" devuelve el propio receptor tras configurar — típico en construcción/configuración
}

pedido?.let { p -> // ejecuta el bloque solo si pedido no es null, con p como referencia no-nullable
    procesar(p)
}
```

Usar con criterio, no todos indiscriminadamente — `let` para transformar/operar sobre un valor nullable, `apply`/`also` para configurar un objeto durante su construcción, `run`/`with` para agrupar operaciones sobre un receptor. El abuso de scope functions anidadas dificulta la legibilidad tanto como su ausencia total.

## Checklist

- [ ] ¿Se ha evitado `!!` salvo certeza absoluta y documentada de que el valor no será null?
- [ ] ¿Se usan `data class` para modelos inmutables en lugar de clases con boilerplate manual?
- [ ] ¿Los conjuntos cerrados de variantes usan `sealed class`/`sealed interface` con `when` exhaustivo?

Ver también: [`coroutines.md`](./coroutines.md), [`flow.md`](./flow.md), [`../android/architecture.md`](../android/architecture.md).
