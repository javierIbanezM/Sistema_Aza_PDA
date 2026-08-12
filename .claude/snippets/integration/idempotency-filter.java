// snippets/integration/idempotency-filter.java
// Verificación de Idempotency-Key reutilizable — ver recipes/integration-add-idempotency.md.

@Component
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    public IdempotencyService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    public <T> T ejecutarConIdempotencia(String idempotencyKey, Class<T> tipoRespuesta, Supplier<T> operacion) {
        if (idempotencyKey == null) {
            return operacion.get(); // sin clave, se ejecuta sin garantía de idempotencia
        }

        Optional<String> respuestaExistente = repository.buscarRespuesta(idempotencyKey);
        if (respuestaExistente.isPresent()) {
            return deserializar(respuestaExistente.get(), tipoRespuesta);
        }

        T resultado = operacion.get();
        repository.guardar(idempotencyKey, serializar(resultado), Duration.ofHours(24));
        return resultado;
    }
}

// Uso en un controller
@PostMapping("/pagos")
public ResponseEntity<PagoResponse> procesarPago(
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @RequestBody PagoRequest request) {

    PagoResponse resultado = idempotencyService.ejecutarConIdempotencia(
        idempotencyKey, PagoResponse.class, () -> procesarPagoUseCase.ejecutar(request));

    return ResponseEntity.ok(resultado);
}
