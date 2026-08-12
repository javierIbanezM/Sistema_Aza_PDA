# Recipe: Crear una Entidad JPA (Java/Hibernate)

Aplica: [`../../knowledge/hibernate/jpa.md`](../knowledge/hibernate/jpa.md), [`../../knowledge/hibernate/hibernate.md`](../knowledge/hibernate/hibernate.md), [`../../knowledge/lombok/lombok.md`](../knowledge/lombok/lombok.md), [`../../standards/naming.md`](../standards/naming.md).

## Pasos

### 1. Diseñar primero la entidad de dominio (sin JPA)

```java
public class Pedido {
    private final PedidoId id;
    private final ClienteId clienteId;
    private final List<LineaPedido> lineas = new ArrayList<>();
    private EstadoPedido estado;

    // comportamiento de negocio — ver knowledge/architecture/ddd.md
    public void agregarLinea(LineaPedido linea) { /* ... */ }
    public void confirmar() { /* ... */ }
}
```

**No empezar por la entidad JPA** — empezar por el dominio evita que el modelo de negocio quede condicionado por limitaciones/comodidades del ORM (ver [`../../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md)).

### 2. Definir la entidad de persistencia, en `infrastructure/persistence/`

```java
@Entity
@Table(name = "pedidos", indexes = {
    @Index(name = "ix_pedidos_cliente_id", columnList = "cliente_id") // ver standards/naming.md
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PedidoEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "cliente_id", nullable = false, length = 36)
    private String clienteId;

    @Enumerated(EnumType.STRING) // NUNCA ORDINAL — ver jpa.md
    @Column(nullable = false, length = 20)
    private EstadoPedidoEntity estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Version
    private Long version; // bloqueo optimista, ver hibernate.md

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LineaPedidoEntity> lineas = new ArrayList<>();

    public PedidoEntity(String id, String clienteId, EstadoPedidoEntity estado, Instant fechaCreacion) {
        this.id = id;
        this.clienteId = clienteId;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoEntity that)) return false;
        return id != null && id.equals(that.id); // igualdad SOLO por ID, ver lombok.md
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
```

### 3. Entidad relacionada (lado "muchos")

```java
@Entity
@Table(name = "lineas_pedido")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineaPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY, no EAGER — ver jpa.md
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @Column(name = "producto_id", nullable = false, length = 36)
    private String productoId;

    @Column(nullable = false)
    private int cantidad;

    public LineaPedidoEntity(PedidoEntity pedido, String productoId, int cantidad) {
        this.pedido = pedido;
        this.productoId = productoId;
        this.cantidad = cantidad;
    }
}
```

### 4. Value Object embebido (si aplica)

```java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DineroEmbeddable {

    @Column(name = "total_monto")
    private BigDecimal monto;

    @Column(name = "total_moneda", length = 3)
    private String moneda;

    public DineroEmbeddable(BigDecimal monto, String moneda) {
        this.monto = monto;
        this.moneda = moneda;
    }
}
```

### 5. Migración de esquema (Flyway)

```sql
-- V6__crear_tabla_pedidos_y_lineas.sql
CREATE TABLE pedidos (
    id VARCHAR(36) PRIMARY KEY,
    cliente_id VARCHAR(36) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_pedidos_cliente_id ON pedidos (cliente_id);

CREATE TABLE lineas_pedido (
    id VARCHAR(36) PRIMARY KEY,
    pedido_id VARCHAR(36) NOT NULL REFERENCES pedidos(id),
    producto_id VARCHAR(36) NOT NULL,
    cantidad INT NOT NULL
);
```

### 6. Mapper Dominio ↔ Entidad (ver [`../../knowledge/mapstruct/mapstruct.md`](../knowledge/mapstruct/mapstruct.md))

```java
@Mapper(componentModel = "spring")
public interface PedidoPersistenceMapper {
    @Mapping(target = "id", source = "id.valor")
    @Mapping(target = "clienteId", source = "clienteId.valor")
    PedidoEntity aEntity(Pedido pedido);

    @Mapping(target = "id", expression = "java(new PedidoId(entity.getId()))")
    @Mapping(target = "clienteId", expression = "java(new ClienteId(entity.getClienteId()))")
    Pedido aDominio(PedidoEntity entity);
}
```

### 7. Test de mapeo (integration test, no unitario, ver [`../../knowledge/junit/junit.md`](../knowledge/junit/junit.md))

```java
@DataJpaTest
class PedidoEntityMappingTest {

    @Autowired private TestEntityManager entityManager;

    @Test
    void deberiaPersistirYRecuperarUnPedidoConSusLineas() {
        PedidoEntity entity = new PedidoEntity("p-1", "c-1", EstadoPedidoEntity.BORRADOR, Instant.now());
        entityManager.persistAndFlush(entity);
        entityManager.clear(); // fuerza recarga desde BD, no desde el Persistence Context

        PedidoEntity recuperado = entityManager.find(PedidoEntity.class, "p-1");
        assertThat(recuperado.getClienteId()).isEqualTo("c-1");
    }
}
```

## Checklist final

- [ ] ¿Se diseñó primero el dominio, y la entidad JPA se derivó de él, no al revés?
- [ ] ¿Todos los enums usan `EnumType.STRING`?
- [ ] ¿Toda relación es `FetchType.LAZY`?
- [ ] ¿Existe `@Version` si la entidad tiene concurrencia de escritura relevante?
- [ ] ¿`equals`/`hashCode` están basados únicamente en el ID?
- [ ] ¿Existe migración de esquema versionada (Flyway/Liquibase)?
- [ ] Ejecutar [`../../checklists/architecture.md`](../checklists/architecture.md).
