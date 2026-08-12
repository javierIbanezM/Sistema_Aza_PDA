# Spring Web (MVC)

> Ver [`../../standards/api.md`](../../standards/api.md) para las convenciones REST que este documento aplica en Spring, y [`../../standards/dto.md`](../../standards/dto.md) para el diseño de los DTOs usados en los controllers.

## Controllers: solo traducción, sin lógica de negocio

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
        Pedido pedido = crearPedidoUseCase.ejecutar(request.clienteId(), request.lineas());
        return ResponseEntity.created(URI.create("/api/v1/pedidos/" + pedido.getId()))
            .body(pedidoMapper.aResponse(pedido));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable String id) {
        return pedidoMapper.aResponse(buscarPedidoUseCase.ejecutar(id));
    }
}
```

El controller **delega** siempre en un caso de uso — nunca contiene reglas de negocio, cálculos ni validación de invariantes propias del dominio (ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md)).

## Manejo global de errores

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PedidoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handlePedidoNoEncontrado(PedidoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("PEDIDO_NOT_FOUND", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(PedidoYaConfirmadoException.class)
    public ResponseEntity<ErrorResponse> handlePedidoYaConfirmado(PedidoYaConfirmadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("PEDIDO_ALREADY_CONFIRMED", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .toList();
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", "Datos de entrada inválidos", detalles));
    }

    @ExceptionHandler(Exception.class) // única captura genérica permitida: el borde de la aplicación
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex) {
        log.error("Error no controlado", ex); // se loguea completo, nunca se expone al cliente
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("INTERNAL_ERROR", "Ha ocurrido un error inesperado", List.of()));
    }
}
```

Formato de error consistente con [`../../standards/api.md`](../../standards/api.md). Ver [`../../standards/exceptions.md`](../../standards/exceptions.md) para la jerarquía de excepciones de dominio que este handler mapea, y [`../java/exceptions.md`](../../standards/exceptions.md) para la mecánica de excepciones en Java.

## Content negotiation y versionado

```java
@RestController
@RequestMapping("/api/v1/pedidos") // versión en la URL, ver standards/api.md
public class PedidoController { /* ... */ }
```

## Interceptores para lógica transversal (correlación de peticiones)

```java
@Component
public class CorrelationIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = Optional.ofNullable(request.getHeader("X-Correlation-Id"))
            .orElse(UUID.randomUUID().toString());
        MDC.put("traceId", correlationId); // ver ../../standards/logging-standard.md
        response.setHeader("X-Correlation-Id", correlationId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        MDC.clear();
    }
}
```

Ver [`../../standards/logging-standard.md`](../../standards/logging-standard.md) para el razonamiento completo de correlación de peticiones.

## Checklist

- [ ] ¿Los controllers delegan siempre en un caso de uso, sin lógica de negocio propia?
- [ ] ¿Existe un `@RestControllerAdvice` que mapea cada excepción de dominio a un código HTTP y formato de error consistente?
- [ ] ¿Las URLs siguen las convenciones de [`../../standards/api.md`](../../standards/api.md) (recursos en plural, versión explícita)?
- [ ] ¿Existe propagación de `trace-id`/`correlation-id` para observabilidad?

Ver también: [`spring-core.md`](./spring-core.md), [`spring-validation.md`](./spring-validation.md), [`../../standards/api.md`](../../standards/api.md).
