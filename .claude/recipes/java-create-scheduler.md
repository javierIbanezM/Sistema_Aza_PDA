# Recipe: Crear un Scheduler / Tarea Programada (Java/Spring)

Aplica: [`../../standards/logging-standard.md`](../standards/logging-standard.md), [`../../standards/exceptions.md`](../standards/exceptions.md).

## Pasos

### 1. Habilitar scheduling (una vez por proyecto)

```java
@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4); // dimensionar según número de jobs concurrentes reales, no arbitrario
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setErrorHandler(t -> log.error("Error no capturado en tarea programada", t));
        return scheduler;
    }
}
```

Un `errorHandler` explícito evita que una excepción no capturada dentro de un `@Scheduled` mate silenciosamente el hilo del scheduler sin dejar rastro (ver [`../../standards/logging-standard.md`](../standards/logging-standard.md)).

### 2. Escribir el job, con manejo de errores propio

```java
@Component
public class LimpiezaPedidosExpiradosJob {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaPedidosExpiradosJob.class);

    private final PedidoRepository pedidoRepository;
    private final MetricsRegistry metrics; // opcional: métricas de duración/resultado del job

    public LimpiezaPedidosExpiradosJob(PedidoRepository pedidoRepository, MetricsRegistry metrics) {
        this.pedidoRepository = pedidoRepository;
        this.metrics = metrics;
    }

    @Scheduled(cron = "${app.jobs.limpieza-pedidos.cron:0 0 3 * * *}") // configurable externamente
    @SchedulerLock(name = "limpiezaPedidosExpirados", lockAtMostFor = "10m") // ver paso 4, evita ejecución duplicada
    public void ejecutar() {
        Instant inicio = Instant.now();
        log.info("event=job.limpieza_pedidos.iniciado");

        try {
            int cantidad = pedidoRepository.cancelarPedidosExpirados();
            Duration duracion = Duration.between(inicio, Instant.now());

            log.info("event=job.limpieza_pedidos.completado cantidad={} duracion_ms={}",
                cantidad, duracion.toMillis());
            metrics.recordJobSuccess("limpieza_pedidos", duracion);
        } catch (Exception e) {
            log.error("event=job.limpieza_pedidos.fallido", e);
            metrics.recordJobFailure("limpieza_pedidos");
            // decidir explícitamente: ¿el job debe reintentarse en la siguiente ejecución programada
            // (comportamiento por defecto) o requiere alertar inmediatamente? Ver logging-standard.md.
        }
    }
}
```

### 3. Cron expression: usar configuración externa, no hardcodear

```yaml
app:
  jobs:
    limpieza-pedidos:
      cron: "0 0 3 * * *"  # distinto por entorno si es necesario (ej. cada 5 min en dev para testing manual)
```

### 4. Evitar ejecución duplicada en despliegues con múltiples réplicas

Con más de una instancia de la aplicación desplegada, `@Scheduled` se ejecuta **en cada réplica** de forma independiente, salvo que se use un mecanismo de lock distribuido:

```java
// ShedLock (librería): garantiza que solo una réplica ejecuta el job en cada disparo programado
@SchedulerLock(name = "limpiezaPedidosExpirados", lockAtMostFor = "10m", lockAtLeastFor = "1m")
```

```yaml
# Configuración de ShedLock con almacenamiento en base de datos, coordinando el lock entre réplicas
```

Sin esto, un job de "enviar recordatorio de pago" ejecutándose en 3 réplicas enviaría 3 recordatorios duplicados a cada cliente — error frecuente y silencioso en despliegues escalados horizontalmente.

### 5. Tests

```java
@Test
void deberiaCancelarLosPedidosExpiradosYRegistrarLaCantidad() {
    when(pedidoRepository.cancelarPedidosExpirados()).thenReturn(5);

    job.ejecutar();

    verify(metrics).recordJobSuccess(eq("limpieza_pedidos"), any());
}

@Test
void deberiaRegistrarElFalloSinPropagarLaExcepcion() {
    when(pedidoRepository.cancelarPedidosExpirados()).thenThrow(new RuntimeException("timeout BD"));

    assertThatCode(() -> job.ejecutar()).doesNotThrowAnyException(); // el job no debe matar el scheduler

    verify(metrics).recordJobFailure("limpieza_pedidos");
}
```

## Checklist final

- [ ] ¿El cron/frecuencia es configurable externamente, no hardcodeado?
- [ ] ¿El job captura sus propias excepciones y las loguea, sin dejar que maten el scheduler silenciosamente?
- [ ] ¿Existe protección contra ejecución duplicada si el despliegue tiene múltiples réplicas?
- [ ] ¿Se registran métricas de duración/éxito/fallo del job para observabilidad?
- [ ] Ejecutar [`../../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`java-create-worker.md`](./java-create-worker.md), [`../../standards/logging-standard.md`](../standards/logging-standard.md).
