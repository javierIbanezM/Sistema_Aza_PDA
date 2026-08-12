// snippets/integration/resilient-client.java
// Cliente resiliente para sistema externo: Circuit Breaker + reintentos + traducción de errores.
// Ver recipes/integration-create-resilient-client.md.

@Component
public class SapErpClient implements ErpClient {

    private final SapHttpClient httpClient;
    private final SapPedidoAdapter adapter;
    private final ErpErrorTranslator errorTranslator;
    private final CircuitBreaker circuitBreaker;

    public SapErpClient(SapHttpClient httpClient, SapPedidoAdapter adapter, ErpErrorTranslator errorTranslator) {
        this.httpClient = httpClient;
        this.adapter = adapter;
        this.errorTranslator = errorTranslator;
        this.circuitBreaker = CircuitBreaker.of("erp-sap", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(20)
            .build());

        this.circuitBreaker.getEventPublisher().onStateTransition(evento ->
            log.warn("event=circuit_breaker.transicion sistema=erp-sap estado_nuevo={}",
                evento.getStateTransition().getToState()));
    }

    @Override
    public ResultadoCreacionPedido crearPedido(Pedido pedido) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                SapPedidoRequest request = adapter.aFormatoSap(pedido);
                SapPedidoResponse response = ejecutarConReintentos(() -> httpClient.crearPedido(request));
                return adapter.aResultadoDominio(response);
            });
        } catch (CallNotPermittedException e) {
            return ResultadoCreacionPedido.pendiente(); // circuito abierto: fallback sin intentar la llamada real
        } catch (SapHttpException e) {
            throw errorTranslator.traducir(e.getRespuestaError());
        }
    }

    private SapPedidoResponse ejecutarConReintentos(Supplier<SapPedidoResponse> llamada) {
        int intento = 0;
        while (true) {
            try {
                return llamada.get();
            } catch (SapTimeoutException | SapServiceUnavailableException e) {
                intento++;
                if (intento >= 3) throw e;
                sleepConBackoff(intento);
            }
        }
    }

    private void sleepConBackoff(int intento) {
        long delay = (long) (100 * Math.pow(2, intento)) + ThreadLocalRandom.current().nextLong(0, 100);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
