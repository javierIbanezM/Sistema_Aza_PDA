# JPA (Jakarta Persistence API)

> JPA es la especificación; Hibernate es la implementación más común. Este documento cubre el mapeo de entidades y consultas a nivel de la especificación JPA. Ver [`hibernate.md`](./hibernate.md) para comportamientos y optimizaciones específicas de Hibernate como implementación concreta.

## Entidad de persistencia vs. entidad de dominio

**Nunca la misma clase.** La entidad `@Entity` de JPA es un detalle de infraestructura; la entidad de dominio (ver [`../architecture/ddd.md`](../architecture/ddd.md)) es el modelo de negocio. Se mapean explícitamente entre sí (ver [`../../standards/dto.md`](../../standards/dto.md) para el principio general de no exponer una capa a través de otra).

```java
// Entidad de persistencia (infrastructure/)
@Entity
@Table(name = "pedidos")
public class PedidoEntity {
    @Id
    private String id;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Enumerated(EnumType.STRING)
    private EstadoPedidoEntity estado;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaPedidoEntity> lineas = new ArrayList<>();

    protected PedidoEntity() {} // constructor sin argumentos requerido por JPA

    // getters/setters solo para uso de JPA/Hibernate, no expuestos fuera de infrastructure/
}
```

```java
// Entidad de dominio (domain/) — sin ninguna anotación JPA
public class Pedido {
    private final PedidoId id;
    private final ClienteId clienteId;
    private EstadoPedido estado;
    private final List<LineaPedido> lineas = new ArrayList<>();

    // comportamiento de negocio, invariantes — ver ddd.md
}
```

```java
// Mapper explícito entre ambas
public class PedidoMapper {
    public static PedidoEntity aEntity(Pedido pedido) { /* ... */ }
    public static Pedido aDominio(PedidoEntity entity) { /* ... */ }
}
```

## Anotaciones de mapeo esenciales

| Anotación | Uso |
|---|---|
| `@Id` / `@GeneratedValue` | Clave primaria; `GenerationType.UUID` o generación en el dominio son preferibles a `IDENTITY` para IDs asignados antes de persistir |
| `@Column` | Mapeo explícito de nombre de columna, `nullable`, `length` — nunca dejar que Hibernate infiera todo implícitamente en proyectos con convenciones de BD ya definidas |
| `@OneToMany` / `@ManyToOne` | Relaciones; ver `fetch` más abajo |
| `@Enumerated(EnumType.STRING)` | **Obligatorio** para enums — `EnumType.ORDINAL` (por defecto) rompe silenciosamente si se reordena el enum |
| `@Version` | Bloqueo optimista — ver sección siguiente |
| `@Embeddable` / `@Embedded` | Mapear un Value Object (ver [`../architecture/ddd.md`](../architecture/ddd.md)) como columnas de la misma tabla, sin tabla propia |

## `FetchType`: LAZY por defecto

```java
@ManyToOne(fetch = FetchType.LAZY) // NUNCA EAGER por defecto
@JoinColumn(name = "cliente_id")
private ClienteEntity cliente;
```

`EAGER` como default silencioso es la causa raíz más común del anti-patrón [N+1 Query](../../anti-patterns/n-plus-one.md) — cada carga de una colección de entidades dispara además la carga de cada relación `EAGER`. Cargar relaciones explícitamente cuando se necesitan, con `JOIN FETCH` o `@EntityGraph`:

```java
@Query("SELECT p FROM PedidoEntity p JOIN FETCH p.lineas WHERE p.clienteId = :clienteId")
List<PedidoEntity> findByClienteIdConLineas(@Param("clienteId") String clienteId);
```

Ver [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md) para el diagnóstico completo del problema y más alternativas.

## Bloqueo optimista con `@Version`

```java
@Entity
public class PedidoEntity {
    @Id private String id;

    @Version
    private Long version; // Hibernate incrementa automáticamente en cada UPDATE
}
```

Ante escrituras concurrentes sobre la misma fila, Hibernate lanza `OptimisticLockException` si la versión leída ya no coincide con la almacenada — evita que una escritura pise silenciosamente los cambios de otra transacción concurrente, sin necesidad de bloqueo pesimista (`SELECT ... FOR UPDATE`) en el caso común.

## Embeddable para Value Objects

```java
@Embeddable
public class DineroEmbeddable {
    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "moneda")
    private String moneda;
}

@Entity
public class PedidoEntity {
    @Embedded
    private DineroEmbeddable total; // columnas "monto" y "moneda" en la misma tabla pedidos
}
```

Mapea directamente el Value Object `Dinero` del dominio (ver [`../architecture/ddd.md`](../architecture/ddd.md)) sin necesidad de una tabla separada.

## JPQL vs. Criteria API vs. `@Query` nativo

- **JPQL** (`@Query("SELECT p FROM Pedido p WHERE ...")`) — preferido para la mayoría de queries: legible, portable entre motores de base de datos, parametrizado de forma segura (ver [`../../anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md)).
- **Criteria API** — para queries construidas dinámicamente en tiempo de ejecución según filtros opcionales del usuario (búsqueda avanzada con múltiples criterios combinables).
- **`@Query(nativeQuery = true)`** — solo cuando se necesita una característica específica del motor de base de datos no expresable en JPQL (funciones propietarias de SQL Server/PostgreSQL). Siempre parametrizado, nunca concatenado.

## Migraciones de esquema

**Nunca `ddl-auto: update` en producción.** Migraciones versionadas con Flyway o Liquibase:

```sql
-- V1__crear_tabla_pedidos.sql (Flyway)
CREATE TABLE pedidos (
    id VARCHAR(36) PRIMARY KEY,
    cliente_id VARCHAR(36) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
```

`ddl-auto: validate` en producción (verifica que el esquema coincide con las entidades, sin modificarlo); `ddl-auto: update` únicamente aceptable en desarrollo local.

## Checklist

- [ ] ¿La entidad JPA está confinada a `infrastructure/`, sin filtrarse a `domain/` ni a la capa de presentación?
- [ ] ¿Todas las relaciones son `FetchType.LAZY` por defecto, con `JOIN FETCH`/`@EntityGraph` explícito donde se necesitan?
- [ ] ¿Los enums usan `@Enumerated(EnumType.STRING)`?
- [ ] ¿Las entidades con alta concurrencia de escritura tienen `@Version`?
- [ ] ¿Las migraciones de esquema están versionadas (Flyway/Liquibase), sin `ddl-auto: update` en producción?

Ver también: [`hibernate.md`](./hibernate.md), [`../architecture/ddd.md`](../architecture/ddd.md), [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md), `CLAUDE.md` sección 4.4.
