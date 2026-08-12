// snippets/java/repository.java
// Repository pattern con Spring Data JPA — ver patterns/repository.md y knowledge/spring/spring-data-jpa.md.

// Puerto de dominio/aplicación (sin dependencias de Spring/JPA)
public interface PedidoRepository {
    void guardar(Pedido pedido);
    Optional<Pedido> buscarPorId(PedidoId id);
    List<Pedido> buscarPendientesDeEnvio();
}

// Interfaz técnica de Spring Data, confinada a infrastructure/
interface PedidoJpaRepository extends JpaRepository<PedidoEntity, String> {

    @Query("SELECT p FROM PedidoEntity p JOIN FETCH p.lineas WHERE p.estado = :estado AND p.enviado = false")
    List<PedidoEntity> findPendientesDeEnvioConLineas(@Param("estado") EstadoPedidoEntity estado);
}

// Adaptador
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

// Fake en memoria para tests unitarios de casos de uso
public class PedidoRepositoryEnMemoria implements PedidoRepository {
    private final Map<PedidoId, Pedido> pedidos = new HashMap<>();

    public void guardar(Pedido pedido) { pedidos.put(pedido.getId(), pedido); }
    public Optional<Pedido> buscarPorId(PedidoId id) { return Optional.ofNullable(pedidos.get(id)); }
    public List<Pedido> buscarPendientesDeEnvio() {
        return pedidos.values().stream().filter(Pedido::estaPendienteDeEnvio).toList();
    }
}
