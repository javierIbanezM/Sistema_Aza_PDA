# MapStruct

> Ver [`../../standards/dto.md`](../../standards/dto.md) para la norma de mapeo explícito entre capas que MapStruct automatiza en tiempo de compilación.

## Por qué MapStruct y no mapeo manual

El mapeo explícito entre DTO ↔ Dominio ↔ Entidad de persistencia (ver [`../../standards/dto.md`](../../standards/dto.md) y [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md)) es necesario para mantener las capas desacopladas, pero escribirlo a mano para cada campo es repetitivo y propenso a errores de omisión (un campo nuevo en la entidad que se olvida mapear al DTO). MapStruct genera el código de mapeo **en tiempo de compilación** (no por reflexión en runtime, sin coste de rendimiento), a partir de una interfaz anotada.

## Mapper básico

```java
@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "total", expression = "java(pedido.calcularTotal())")
    PedidoResponse aResponse(Pedido pedido);

    List<PedidoResponse> aResponseList(List<Pedido> pedidos);
}
```

MapStruct genera automáticamente una implementación (`PedidoMapperImpl`) que copia campos con el mismo nombre, y aplica las reglas explícitas (`@Mapping`) para los que no coinciden o requieren transformación.

## Mapeo con nombres de campo distintos

```java
@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(source = "nombreCompleto", target = "displayName")
    @Mapping(source = "fechaAlta", target = "memberSince")
    ClienteResponse aResponse(Cliente cliente);
}
```

## Mapeo entre Value Objects y tipos primitivos

```java
@Mapper(componentModel = "spring")
public interface DineroMapper {

    default BigDecimal aDecimal(Dinero dinero) {
        return dinero != null ? dinero.getMonto() : null;
    }

    default Dinero aDinero(BigDecimal monto, String moneda) {
        return monto != null ? new Dinero(monto, Moneda.valueOf(moneda)) : null;
    }
}
```

Para Value Objects (ver [`../architecture/ddd.md`](../architecture/ddd.md)) que envuelven invariantes propias, un método `default` explícito es preferible a intentar que MapStruct infiera automáticamente la construcción — la validación del Value Object debe ejecutarse igualmente al construirlo desde el mapper.

## Mappers compuestos (delegación)

```java
@Mapper(componentModel = "spring", uses = { DineroMapper.class, LineaPedidoMapper.class })
public interface PedidoMapper {
    PedidoResponse aResponse(Pedido pedido); // delega automáticamente en DineroMapper/LineaPedidoMapper para los campos correspondientes
}
```

## Mapeo entre Entidad de dominio y Entidad de persistencia

```java
@Mapper(componentModel = "spring")
public interface PedidoPersistenceMapper {

    @Mapping(target = "clienteId", source = "clienteId.valor")
    PedidoEntity aEntity(Pedido pedido);

    @Mapping(target = "clienteId", expression = "java(new ClienteId(entity.getClienteId()))")
    Pedido aDominio(PedidoEntity entity);
}
```

Un mapper por frontera de capa (`PedidoMapper` para DTO↔Dominio, `PedidoPersistenceMapper` para Dominio↔Entidad JPA) — no un único mapper "todo en uno" que mezcla ambas responsabilidades, para mantener cada capa desacoplada de forma independiente (ver [`../../knowledge/architecture/solid.md`](../architecture/solid.md), SRP).

## Manejo de valores nulos

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ActualizarClienteMapper {
    // en un mapeo de "update parcial", un campo null en el DTO NO sobrescribe el valor existente
    void actualizarDesdeRequest(ActualizarClienteRequest request, @MappingTarget Cliente cliente);
}
```

Relevante para PATCH parcial: sin esta estrategia, un campo omitido en el request (null) sobrescribiría silenciosamente el valor existente con `null`.

## Checklist

- [ ] ¿Existe un mapper explícito por cada frontera de capa (DTO↔Dominio, Dominio↔Persistencia), sin mezclarlos?
- [ ] ¿Los Value Objects se construyen a través de métodos `default` que preservan su validación de invariantes?
- [ ] ¿Los mapeos de actualización parcial (`PATCH`) ignoran explícitamente los campos nulos del request?
- [ ] ¿Se ha verificado que MapStruct no genera warnings de campos no mapeados (`unmapped target property`) sin una razón explícita?

Ver también: [`../../standards/dto.md`](../../standards/dto.md), [`../architecture/ddd.md`](../architecture/ddd.md), [`lombok.md`](../lombok/lombok.md).
