# Recipe: Crear un Cliente Resiliente para un Sistema Externo (Integration)

Aplica: [`../knowledge/integration/retries-and-backoff.md`](../knowledge/integration/retries-and-backoff.md), [`../knowledge/integration/circuit-breaker.md`](../knowledge/integration/circuit-breaker.md), [`../knowledge/integration/schema-mapping.md`](../knowledge/integration/schema-mapping.md), [`../knowledge/integration/error-handling.md`](../knowledge/integration/error-handling.md).

## Pasos

### 1. Definir el puerto propio (nunca el modelo del sistema externo directamente)

```java
public interface ErpClient {
    ResultadoCreacionPedido crearPedido(Pedido pedido);
}
```

### 2. Definir el Anti-Corruption Layer

```java
public class SapPedidoAdapter {
    public SapPedidoRequest aFormatoSap(Pedido pedido) { /* mapeo explícito, ver schema-mapping.md */ }
    public ResultadoCreacionPedido aResultadoDominio(SapPedidoResponse response) { /* ... */ }
}
```

### 3. Implementar el adaptador con Circuit Breaker + reintentos

```java
@Component
public class SapErpClient implements ErpClient {

    private final SapHttpClient httpClient;
    private final SapPedidoAdapter adapter;
    private final CircuitBreaker circuitBreaker;

    public SapErpClient(SapHttpClient httpClient, SapPedidoAdapter adapter) {
        this.httpClient = httpClient;
        this.adapter = adapter;
        this.circuitBreaker = CircuitBreaker.of("erp-sap", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(20)
            .build());
    }

    @Override
    @Retry(name = "erp-sap", fallbackMethod = "crearPedidoFallback")
    public ResultadoCreacionPedido crearPedido(Pedido pedido) {
        return circuitBreaker.executeSupplier(() -> {
            SapPedidoRequest request = adapter.aFormatoSap(pedido);
            SapPedidoResponse response = httpClient.crearPedido(request);
            return adapter.aResultadoDominio(response);
        });
    }

    private ResultadoCreacionPedido crearPedidoFallback(Pedido pedido, Exception ex) {
        colaReintentoDiferido.encolar(pedido); // ver saga.md para compensaciones si aplica
        return ResultadoCreacionPedido.pendiente();
    }
}
```

### 4. Traducir errores del sistema externo

```java
try {
    return httpClient.crearPedido(request);
} catch (SapHttpException e) {
    throw errorTranslator.traducir(e.getRespuestaError()); // ver error-handling.md
}
```

### 5. Instrumentar observabilidad

```java
circuitBreaker.getEventPublisher().onStateTransition(evento ->
    logger.warn("event=circuit_breaker.transicion sistema=erp-sap estado_nuevo={}",
        evento.getStateTransition().getToState()));
```

Ver [`../knowledge/integration/observability.md`](../knowledge/integration/observability.md) para las métricas completas a instrumentar.

### 6. Test con el sistema externo simulado (WireMock/similar)

```java
@Test
void debeReintentarAnteUn503YTenerExitoEnElSegundoIntento() {
    wireMock.stubFor(post("/erp/pedidos")
        .willReturn(serverError()).inScenario("reintento").whenScenarioStateIs(STARTED)
        .willSetStateTo("segundo-intento"));
    wireMock.stubFor(post("/erp/pedidos")
        .willReturn(okJson(respuestaExitosa())).inScenario("reintento").whenScenarioStateIs("segundo-intento"));

    ResultadoCreacionPedido resultado = sapErpClient.crearPedido(pedido);

    assertThat(resultado.esExitoso()).isTrue();
}
```

## Checklist final

- [ ] ¿El cliente expone un puerto propio, sin filtrar el modelo del sistema externo?
- [ ] ¿Existe Circuit Breaker específico para este sistema, no compartido con otras integraciones?
- [ ] ¿Los errores del sistema externo se traducen a excepciones de dominio propio?
- [ ] ¿Existe fallback apropiado (caché para lectura, cola diferida para escritura crítica)?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/integration/resilient-client.java`](../snippets/integration/resilient-client.java), [`integration-add-idempotency.md`](./integration-add-idempotency.md).
