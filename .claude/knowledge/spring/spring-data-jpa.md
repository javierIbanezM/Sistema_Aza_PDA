# Spring Data JPA

> Este documento cubre la **capa de integración de Spring** sobre JPA/Hibernate (generación de repositorios, query methods). Para el mapeo de entidades y comportamiento del ORM en sí, ver [`../hibernate/jpa.md`](../hibernate/jpa.md) y [`../hibernate/hibernate.md`](../hibernate/hibernate.md) — no se duplica aquí.

## Spring Data JPA como implementación de un puerto propio

```java
// Puerto de dominio/aplicación (sin ninguna dependencia de Spring ni JPA)
public interface PedidoRepository {
    void guardar(Pedido pedido);
    Optional<Pedido> buscarPorId(PedidoId id);
}

// Interfaz técnica de Spring Data, en infraestructura, NUNCA expuesta fuera de infrastructure/
interface PedidoJpaRepository extends JpaRepository<PedidoEntity, String> {}

// Adaptador: implementa el puerto de dominio usando Spring Data por debajo
@Repository
public class PedidoRepositoryJpa implements PedidoRepository {

    private final PedidoJpaRepository jpaRepository;

    public PedidoRepositoryJpa(PedidoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void guardar(Pedido pedido) {
        jpaRepository.save(PedidoMapper.aEntity(pedido));
    }

    @Override
    public Optional<Pedido> buscarPorId(PedidoId id) {
        return jpaRepository.findById(id.valor()).map(PedidoMapper::aDominio);
    }
}
```

Ver [`../../patterns/repository.md`](../../patterns/repository.md) para el razonamiento completo del patrón.

## Query methods derivados del nombre

```java
interface PedidoJpaRepository extends JpaRepository<PedidoEntity, String> {
    List<PedidoEntity> findByClienteIdAndEstado(String clienteId, EstadoPedidoEntity estado);
    boolean existsByClienteIdAndEstado(String clienteId, EstadoPedidoEntity estado);
    long countByEstado(EstadoPedidoEntity estado);
}
```

Spring Data genera la implementación a partir del nombre del método siguiendo una convención (`findBy`, `existsBy`, `countBy` + nombres de campo + operadores como `And`/`Or`/`GreaterThan`). Apropiado para queries simples de 1-3 condiciones; queries más complejas se expresan mejor con `@Query` explícito (más legible que un nombre de método de 8 cláusulas encadenadas).

## `@Query` con JOIN FETCH (evitar N+1)

```java
@Query("SELECT p FROM PedidoEntity p JOIN FETCH p.lineas WHERE p.estado = :estado")
List<PedidoEntity> findByEstadoConLineas(@Param("estado") EstadoPedidoEntity estado);
```

Ver [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md) y [`../hibernate/jpa.md`](../hibernate/jpa.md) para el detalle completo del problema y la solución.

## Paginación con `Pageable`

```java
interface PedidoJpaRepository extends JpaRepository<PedidoEntity, String> {
    Page<PedidoEntity> findByClienteId(String clienteId, Pageable pageable);
}
```

```java
@GetMapping
public Page<PedidoResponse> listar(
    @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
    return pedidoJpaRepository.findByClienteId(clienteId, pageable).map(pedidoMapper::aResponse);
}
```

Paginación obligatoria en cualquier endpoint que devuelva una colección potencialmente grande (ver [`../../standards/api.md`](../../standards/api.md)).

## Specifications para queries dinámicas

```java
public class PedidoSpecifications {
    public static Specification<PedidoEntity> conEstado(EstadoPedidoEntity estado) {
        return (root, query, cb) -> estado == null ? null : cb.equal(root.get("estado"), estado);
    }
    public static Specification<PedidoEntity> conClienteId(String clienteId) {
        return (root, query, cb) -> clienteId == null ? null : cb.equal(root.get("clienteId"), clienteId);
    }
}

// Uso: combina filtros opcionales sin construir SQL condicional a mano
Specification<PedidoEntity> spec = Specification
    .where(PedidoSpecifications.conEstado(estado))
    .and(PedidoSpecifications.conClienteId(clienteId));
List<PedidoEntity> resultado = jpaRepository.findAll(spec);
```

Apropiado para "búsqueda avanzada" con múltiples filtros opcionales combinables — evita construir JPQL/SQL condicional a mano con concatenación (ver [`../../anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md)) manteniendo cada filtro parametrizado de forma segura.

## Checklist

- [ ] ¿La interfaz `JpaRepository` está confinada a `infrastructure/`, sin filtrarse a `application/` ni `domain/`?
- [ ] ¿Las queries complejas usan `@Query` explícito en lugar de nombres de método excesivamente largos?
- [ ] ¿Los endpoints de colección usan `Pageable`, no devuelven listas completas sin paginar?
- [ ] ¿Las búsquedas con filtros opcionales combinables usan `Specification` en lugar de SQL condicional concatenado?

Ver también: [`../hibernate/jpa.md`](../hibernate/jpa.md), [`../hibernate/hibernate.md`](../hibernate/hibernate.md), [`../../patterns/repository.md`](../../patterns/repository.md), [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md).
