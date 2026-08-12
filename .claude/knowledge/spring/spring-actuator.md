# Spring Actuator

> Ver [`../architecture/logging.md`](../architecture/logging.md) para el marco general de observabilidad (logs, métricas, trazas) que Actuator implementa parcialmente en el ecosistema Spring.

## Qué provee

Actuator expone endpoints HTTP de gestión y monitorización de la aplicación en ejecución: health checks, métricas, información de build, variables de entorno activas — sin necesidad de instrumentación manual para lo básico.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## Health checks: base de Kubernetes probes

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true # expone /actuator/health/liveness y /actuator/health/readiness por separado
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
```

```java
@Component
public class ServicioPagosHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return procesadorPagos.estaDisponible()
            ? Health.up().build()
            : Health.down().withDetail("motivo", "Servicio de pagos no responde").build();
    }
}
```

`liveness` (¿debe reiniciarse el proceso?) y `readiness` (¿debe recibir tráfico ahora mismo?) son conceptos distintos — un servicio puede estar "vivo" pero no "listo" temporalmente (calentando caché al arrancar). Ver la Skill de Docker/Kubernetes (milestone futuro) para el mapeo directo a `livenessProbe`/`readinessProbe`.

## Métricas con Micrometer (integrado en Actuator)

```java
@Component
public class MetricasPedidos {
    private final Counter pedidosCreados;
    private final Timer duracionProcesamiento;

    public MetricasPedidos(MeterRegistry registry) {
        this.pedidosCreados = Counter.builder("pedidos.creados").register(registry);
        this.duracionProcesamiento = Timer.builder("pedidos.procesamiento.duracion").register(registry);
    }

    public void registrarPedidoCreado() { pedidosCreados.increment(); }
}
```

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true # expone /actuator/prometheus en formato scrapeable
```

Micrometer actúa como fachada neutral de métricas (ver [`../../patterns/facade.md`](../../patterns/facade.md)), exportable a Prometheus, Datadog, CloudWatch u otros backends sin cambiar el código de instrumentación — solo la configuración de exportación. Ver [`../architecture/logging.md`](../architecture/logging.md) para el razonamiento de qué métricas capturar (los cuatro golden signals).

## Seguridad de los endpoints de Actuator

**Nunca exponer `/actuator/**` sin autenticación en producción** — endpoints como `/actuator/env` pueden filtrar variables de entorno sensibles si no se excluyen explícitamente:

```yaml
management:
  endpoint:
    env:
      show-values: when-authorized # nunca "always" en producción
  endpoints:
    web:
      exposure:
        include: health, info, prometheus # whitelist explícita, no "*"
```

```java
@Bean
public SecurityFilterChain actuatorSecurityChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/actuator/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**").permitAll() // necesario para probes de Kubernetes
            .anyRequest().hasRole("ADMIN")
        )
        .build();
}
```

Ver [`../spring/spring-security.md`](./spring-security.md) y `CLAUDE.md` sección 12 (Security Misconfiguration).

## Checklist

- [ ] ¿Solo los endpoints necesarios de Actuator están expuestos (whitelist, no `include: "*"`)?
- [ ] ¿Los endpoints sensibles (`/actuator/env`, `/actuator/beans`) requieren autenticación?
- [ ] ¿`liveness`/`readiness` están configurados por separado para integrarse correctamente con probes de orquestación?
- [ ] ¿Las métricas de negocio relevantes están instrumentadas con Micrometer, no solo las técnicas por defecto?

Ver también: [`../architecture/logging.md`](../architecture/logging.md), [`spring-security.md`](./spring-security.md), `CLAUDE.md` sección 12.
