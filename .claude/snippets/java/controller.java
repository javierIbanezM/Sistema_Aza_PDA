// snippets/java/controller.java
// REST Controller — ver knowledge/spring/spring-web.md y standards/api.md.

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
        return ResponseEntity
            .created(URI.create("/api/v1/pedidos/" + pedido.getId()))
            .body(pedidoMapper.aResponse(pedido));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(pedidoMapper.aResponse(buscarPedidoUseCase.ejecutar(id)));
    }
}

// Manejo global de errores, formato consistente con standards/api.md
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PedidoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handlePedidoNoEncontrado(PedidoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("PEDIDO_NOT_FOUND", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .toList();
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", "Datos de entrada inválidos", detalles));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("INTERNAL_ERROR", "Ha ocurrido un error inesperado", List.of()));
    }
}
