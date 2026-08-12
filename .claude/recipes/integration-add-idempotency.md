# Recipe: Añadir Idempotencia a un Endpoint (Integration)

Aplica: [`../knowledge/integration/idempotency.md`](../knowledge/integration/idempotency.md), [`../standards/api.md`](../standards/api.md).

## Pasos

### 1. Definir el almacén de claves de idempotencia

```java
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {
    @Id
    private String clave;
    @Column(columnDefinition = "TEXT")
    private String respuestaSerializada;
    private Instant creadaEn;
}
```

### 2. Interceptar la petición antes del caso de uso

```java
@PostMapping("/pagos")
public ResponseEntity<PagoResponse> procesarPago(
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @RequestBody PagoRequest request) {

    if (idempotencyKey != null) {
        Optional<PagoResponse> existente = idempotencyStore.buscar(idempotencyKey);
        if (existente.isPresent()) return ResponseEntity.ok(existente.get());
    }

    PagoResponse resultado = procesarPagoUseCase.ejecutar(request);

    if (idempotencyKey != null) {
        idempotencyStore.guardar(idempotencyKey, resultado, Duration.ofHours(24));
    }
    return ResponseEntity.ok(resultado);
}
```

### 3. Job de limpieza de claves expiradas

```java
@Scheduled(cron = "0 0 4 * * *")
public void limpiarClavesExpiradas() {
    idempotencyStore.eliminarAnterioresA(Instant.now().minus(Duration.ofHours(24)));
}
```

Ver [`java-create-scheduler.md`](./java-create-scheduler.md) para el patrón completo de tarea programada.

### 4. Documentar el soporte en el contrato de API

```yaml
parameters:
  - name: Idempotency-Key
    in: header
    required: false
    schema: { type: string, format: uuid }
    description: Clave única por intento lógico. Reintentar con la misma clave devuelve la respuesta original sin reejecutar.
```

### 5. Test

```java
@Test
void debeDevolverLaMismaRespuestaAnteLaMismaIdempotencyKey() {
    String key = UUID.randomUUID().toString();

    PagoResponse primera = controller.procesarPago(key, request).getBody();
    PagoResponse segunda = controller.procesarPago(key, request).getBody();

    assertThat(segunda).isEqualTo(primera);
    verify(procesarPagoUseCase, times(1)).ejecutar(request); // solo se ejecutó una vez
}
```

## Checklist final

- [ ] ¿El almacén de idempotencia tiene TTL y limpieza periódica?
- [ ] ¿El contrato de API documenta explícitamente el soporte de `Idempotency-Key`?
- [ ] ¿El test confirma que el caso de uso se ejecuta una única vez ante repetición de la misma clave?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/integration/idempotency-filter.java`](../snippets/integration/idempotency-filter.java).
