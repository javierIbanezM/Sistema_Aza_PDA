# Lombok

> Ver [`../architecture/ddd.md`](../architecture/ddd.md) para el razonamiento de por qué las entidades de dominio no deben ser anémicas — este documento aclara qué anotaciones de Lombok respetan esa regla y cuáles la violan si se usan sin criterio.

## Qué usar y qué evitar

| Anotación | Uso recomendado |
|---|---|
| `@Getter` | Sí, en general — reduce ruido sin ocultar comportamiento |
| `@RequiredArgsConstructor` | Sí, para inyección de dependencias por constructor (ver [`spring.md`](../spring/spring-core.md)) sobre campos `final` |
| `@Builder` | Sí, en DTOs y Value Objects con muchos campos (ver [`../../patterns/builder.md`](../../patterns/builder.md)) |
| `@Value` | Sí, para Value Objects/DTOs inmutables (`final` + sin setters + `equals`/`hashCode` por valor) |
| `@Data` | **Evitar en entidades JPA** — genera `@Setter` para todos los campos (rompe encapsulación de invariantes, ver [`../../anti-patterns/anemic-domain-model.md`](../../anti-patterns/anemic-domain-model.md)) y `equals`/`hashCode` basados en todos los campos (problemático con proxies de Hibernate, ver más abajo) |
| `@Setter` (a nivel de clase) | Evitar en entidades de dominio — expone mutación sin control de invariantes; usar métodos de negocio con nombre explícito en su lugar |
| `@ToString` | Cuidado con relaciones `LAZY` — puede disparar carga no deseada al loguear la entidad; excluir explícitamente relaciones con `@ToString.Exclude` |
| `@EqualsAndHashCode` | En entidades JPA, basar `equals`/`hashCode` **solo en el ID**, nunca en todos los campos (ver más abajo) |

## Por qué `@Data` es problemático en entidades JPA

```java
// Evitar
@Data
@Entity
public class PedidoEntity {
    @Id private String id;
    private String clienteId;
    private EstadoPedidoEntity estado;
}
```

Problemas concretos:

1. **Setters públicos para todo**: cualquier código puede mutar `estado` directamente, saltándose las invariantes de negocio (ver [`../../anti-patterns/anemic-domain-model.md`](../../anti-patterns/anemic-domain-model.md) — aunque aquí se trata de la entidad de persistencia, no de dominio, el hábito de exponer setters sin control se traslada fácilmente al resto del código).
2. **`equals`/`hashCode` sobre todos los campos**: con un proxy de Hibernate (relación `LAZY`), comparar por todos los campos puede comparar contra un proxy no inicializado, produciendo resultados incorrectos o forzando cargas no deseadas.
3. **`toString()` sobre todos los campos**: si incluye una relación `LAZY`, loguear la entidad dispara una consulta adicional a base de datos de forma no obvia para quien solo quería loguear un ID.

## Alternativa recomendada para entidades JPA

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA, no de uso público
public class PedidoEntity {

    @Id
    private String id;

    private String clienteId;

    @Enumerated(EnumType.STRING)
    private EstadoPedidoEntity estado;

    public PedidoEntity(String id, String clienteId, EstadoPedidoEntity estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoEntity that)) return false;
        return id != null && id.equals(that.id); // igualdad SOLO por ID
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // constante, evita problemas con colecciones antes de persistir
    }
}
```

Solo `@Getter` (sin setters públicos indiscriminados), constructor explícito para uso real y uno protegido vacío para JPA, `equals`/`hashCode` manuales basados únicamente en el ID.

## Lombok en Value Objects (sí recomendado)

```java
@Value // final class, todos los campos final, sin setters, equals/hashCode por valor, toString
public class Dinero {
    BigDecimal monto;
    Moneda moneda;

    public Dinero {
        if (monto.signum() < 0) throw new IllegalArgumentException("El monto no puede ser negativo");
    }
}
```

`@Value` de Lombok encaja naturalmente con el concepto de Value Object de DDD (ver [`../architecture/ddd.md`](../architecture/ddd.md)): inmutable, comparado por valor, sin identidad propia.

## `@Builder` en DTOs y Value Objects complejos

```java
@Value
@Builder
public class CrearPedidoRequest {
    String clienteId;
    List<LineaPedidoRequest> lineas;
    String notas;
}

CrearPedidoRequest request = CrearPedidoRequest.builder()
    .clienteId("c-123")
    .lineas(lineas)
    .build(); // "notas" queda null, opcional
```

Ver [`../../patterns/builder.md`](../../patterns/builder.md) para el razonamiento completo del patrón.

## Checklist

- [ ] ¿Se ha evitado `@Data` en entidades JPA?
- [ ] ¿Las entidades de dominio/persistencia tienen `equals`/`hashCode` basados únicamente en el ID, no en todos los campos?
- [ ] ¿`@ToString` excluye explícitamente relaciones `LAZY` que podrían disparar cargas no deseadas?
- [ ] ¿Los Value Objects usan `@Value` (inmutable, por valor) en lugar de `@Data`?

Ver también: [`spring.md`](../spring/spring-core.md), [`mapstruct.md`](../mapstruct/mapstruct.md), [`../architecture/ddd.md`](../architecture/ddd.md), [`../../anti-patterns/anemic-domain-model.md`](../../anti-patterns/anemic-domain-model.md).
