# Recipe: Crear un REST Controller (Java/Spring)

Aplica: [`../../knowledge/spring/spring-core.md`](../knowledge/spring/spring-core.md), [`../../standards/api.md`](../standards/api.md), [`../../standards/dto.md`](../standards/dto.md), [`../../standards/validation.md`](../standards/validation.md).

## Pasos

### 1. Definir el Request DTO con validación

```java
public record CrearPedidoRequest(
    @NotNull(message = "El clienteId es obligatorio")
    UUID clienteId,

    @NotEmpty(message = "El pedido debe tener al menos una línea")
    List<@Valid LineaPedidoRequest> lineas
) {}

public record LineaPedidoRequest(
    @NotNull UUID productoId,
    @Min(1) int cantidad
) {}
```

### 2. Definir el Response DTO (nunca la entidad de dominio directamente)

```java
public record PedidoResponse(
    String id,
    String estado,
    BigDecimal total,
    String fechaCreacion
) {}
```

### 3. Definir el caso de uso en `application/` (si no existe ya)

```java
@Service
public class CrearPedidoUseCase {
    private final PedidoRepository pedidoRepository;

    public CrearPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido ejecutar(UUID clienteId, List<LineaPedido> lineas) {
        Pedido pedido = Pedido.crear(new ClienteId(clienteId));
        lineas.forEach(pedido::agregarLinea);
        pedido.confirmar();
        pedidoRepository.guardar(pedido);
        return pedido;
    }
}
```

### 4. Definir el Mapper (MapStruct)

```java
@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "total", expression = "java(pedido.calcularTotal())")
    @Mapping(target = "fechaCreacion", expression = "java(pedido.getFechaCreacion().toString())")
    PedidoResponse aResponse(Pedido pedido);

    default LineaPedido aLineaPedido(LineaPedidoRequest request) {
        return new LineaPedido(new ProductoId(request.productoId()), request.cantidad());
    }
}
```

### 5. Escribir el Controller

```java
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final CrearPedidoUseCase crearPedidoUseCase;
    private final PedidoMapper pedidoMapper;

    public PedidoController(CrearPedidoUseCase crearPedidoUseCase, PedidoMapper pedidoMapper) {
        this.crearPedidoUseCase = crearPedidoUseCase;
        this.pedidoMapper = pedidoMapper;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody CrearPedidoRequest request) {
        List<LineaPedido> lineas = request.lineas().stream().map(pedidoMapper::aLineaPedido).toList();
        Pedido pedido = crearPedidoUseCase.ejecutar(request.clienteId(), lineas);

        PedidoResponse response = pedidoMapper.aResponse(pedido);
        return ResponseEntity
            .created(URI.create("/api/v1/pedidos/" + pedido.getId()))
            .body(response);
    }
}
```

### 6. Manejo de errores (si no existe ya un `@RestControllerAdvice` global)

Ver [`../../knowledge/spring/spring-core.md`](../knowledge/spring/spring-core.md) sección "Manejo global de errores" — añadir el `@ExceptionHandler` correspondiente si el caso de uso lanza una excepción de dominio nueva.

### 7. Tests

```java
@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock private CrearPedidoUseCase crearPedidoUseCase;
    @Mock private PedidoMapper pedidoMapper;
    @InjectMocks private PedidoController controller;

    @Test
    void deberiaCrearElPedidoYDevolver201() {
        // arrange, act, assert — ver knowledge/java/junit.md y mockito.md
    }
}
```

Añadir además un test de integración del endpoint completo con `@SpringBootTest` + `MockMvc` o `WebTestClient` si el proyecto lo tiene configurado, y un integration test del repositorio con Testcontainers (ver [`../../knowledge/junit/junit.md`](../knowledge/junit/junit.md)).

## Checklist final

Ejecutar [`../../checklists/architecture.md`](../checklists/architecture.md) completo antes de dar la tarea por terminada.
