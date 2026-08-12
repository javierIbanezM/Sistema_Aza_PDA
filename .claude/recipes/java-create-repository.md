# Recipe: Crear un Repository (Java/Spring Data JPA)

Aplica: [`../../patterns/repository.md`](../patterns/repository.md), [`../../knowledge/hibernate/jpa.md`](../knowledge/hibernate/jpa.md), [`../../knowledge/hibernate/hibernate.md`](../knowledge/hibernate/hibernate.md).

## Pasos

### 1. Definir el puerto en `application/ports/` (interfaz de dominio, sin JPA)

```java
public interface PedidoRepository {
    void guardar(Pedido pedido);
    Optional<Pedido> buscarPorId(PedidoId id);
    List<Pedido> buscarPendientesDeEnvio();
}
```

### 2. Definir la entidad de persistencia en `infrastructure/persistence/`

```java
@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PedidoEntity {

    @Id
    private String id;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Enumerated(EnumType.STRING)
    private EstadoPedidoEntity estado;

    @Version
    private Long version;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LineaPedidoEntity> lineas = new ArrayList<>();

    public PedidoEntity(String id, String clienteId, EstadoPedidoEntity estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
```

Ver [`../../knowledge/lombok/lombok.md`](../knowledge/lombok/lombok.md) — nunca `@Data` en esta clase.

### 3. Definir la interfaz técnica de Spring Data (confinada a infrastructure/)

```java
interface PedidoJpaRepository extends JpaRepository<PedidoEntity, String> {

    @Query("SELECT p FROM PedidoEntity p JOIN FETCH p.lineas WHERE p.estado = :estado AND p.enviado = false")
    List<PedidoEntity> findPendientesDeEnvioConLineas(@Param("estado") EstadoPedidoEntity estado);
}
```

`JOIN FETCH` explícito para evitar N+1 al recorrer `lineas` de cada pedido (ver [`../../anti-patterns/n-plus-one.md`](../anti-patterns/n-plus-one.md)).

### 4. Definir el Mapper Dominio ↔ Persistencia

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

### 5. Implementar el adaptador

```java
@Repository
public class PedidoRepositoryJpa implements PedidoRepository {

    private final PedidoJpaRepository jpaRepository;
    private final PedidoPersistenceMapper mapper;

    public PedidoRepositoryJpa(PedidoJpaRepository jpaRepository, PedidoPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Pedido pedido) {
        jpaRepository.save(mapper.aEntity(pedido));
    }

    @Override
    public Optional<Pedido> buscarPorId(PedidoId id) {
        return jpaRepository.findById(id.valor()).map(mapper::aDominio);
    }

    @Override
    public List<Pedido> buscarPendientesDeEnvio() {
        return jpaRepository.findPendientesDeEnvioConLineas(EstadoPedidoEntity.CONFIRMADO)
            .stream().map(mapper::aDominio).toList();
    }
}
```

### 6. Migración de esquema (Flyway)

```sql
-- V5__crear_tabla_pedidos.sql
CREATE TABLE pedidos (
    id VARCHAR(36) PRIMARY KEY,
    cliente_id VARCHAR(36) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
```

Ver [`../../knowledge/hibernate/jpa.md`](../knowledge/hibernate/jpa.md) — nunca `ddl-auto: update` en producción.

### 7. Fake para tests unitarios de los casos de uso que usan este repositorio

```java
public class PedidoRepositoryEnMemoria implements PedidoRepository {
    private final Map<PedidoId, Pedido> pedidos = new HashMap<>();

    public void guardar(Pedido pedido) { pedidos.put(pedido.getId(), pedido); }
    public Optional<Pedido> buscarPorId(PedidoId id) { return Optional.ofNullable(pedidos.get(id)); }
    public List<Pedido> buscarPendientesDeEnvio() {
        return pedidos.values().stream().filter(Pedido::estaPendienteDeEnvio).toList();
    }
}
```

### 8. Integration test con Testcontainers

```java
@Testcontainers
@SpringBootTest
class PedidoRepositoryJpaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired private PedidoRepositoryJpa repository;

    @Test
    void deberiaGuardarYRecuperarUnPedidoConSusLineas() {
        Pedido pedido = new PedidoTestBuilder().conLineas(List.of(unaLinea())).build();

        repository.guardar(pedido);
        Optional<Pedido> recuperado = repository.buscarPorId(pedido.getId());

        assertThat(recuperado).isPresent();
    }
}
```

## Checklist final

- [ ] ¿La entidad JPA está confinada a `infrastructure/`, nunca expuesta al dominio ni a la API?
- [ ] ¿Toda relación es `LAZY` con `JOIN FETCH` explícito donde se necesita?
- [ ] ¿Existe un Fake en memoria para testear casos de uso que dependen de este repositorio sin base de datos real?
- [ ] Ejecutar [`../../checklists/architecture.md`](../checklists/architecture.md).
