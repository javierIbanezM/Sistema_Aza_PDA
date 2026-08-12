# Performance (Java / JVM)

> Ver [`../../prompts/performance.md`](../../prompts/performance.md) para la metodología general de diagnóstico (medir antes de optimizar) y `CLAUDE.md` sección 11. Este documento cubre las herramientas y causas de degradación **específicas de la JVM**.

## Profiling con JFR (Java Flight Recorder)

```bash
# Grabar un perfil de 60 segundos de la aplicación en ejecución, sin reiniciarla
java -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=perfil.jfr -jar app.jar

# O adjuntar a un proceso ya en ejecución
jcmd <pid> JFR.start duration=60s filename=perfil.jfr
```

Analizar el `.jfr` resultante con JDK Mission Control (JMC) — identifica hot methods (dónde pasa más tiempo la CPU), asignación de objetos (presión sobre el GC) y pausas de Garbage Collector, sin necesidad de instrumentar el código manualmente ni degradar el rendimiento de forma significativa (overhead bajo, apto para producción).

## Garbage Collection: síntomas y diagnóstico

- **Pausas de GC frecuentes o largas** son la causa más común de latencia intermitente ("todo va bien salvo picos ocasionales") en aplicaciones Java.
- Habilitar logging de GC para diagnóstico: `-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=50M`.
- Un patrón de *sawtooth* (memoria sube, cae bruscamente en cada GC, sube de nuevo) es normal. Un patrón de crecimiento sostenido **sin caídas correspondientes** tras el GC indica una fuga de memoria (ver [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md)) — el GC no puede liberar objetos que siguen referenciados.
- Elegir el recolector según el perfil de la aplicación: G1GC (por defecto en JDK moderno, buen equilibrio general), ZGC/Shenandoah (pausas ultra-bajas, para aplicaciones de latencia crítica), Parallel GC (máximo throughput, aceptando pausas más largas, para batch jobs sin requisito de latencia).

## N+1 Query es la causa más común de latencia en apps Spring/JPA

Ver [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md) para el diagnóstico y solución completos. En Java/JPA específicamente: activar `spring.jpa.show-sql=true` + `logging.level.org.hibernate.SQL=debug` en desarrollo/staging para contar queries generadas por endpoint antes de optimizar nada.

## Pool de conexiones (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # NO "cuanto más grande mejor" — dimensionar según núcleos de la BD, no arbitrariamente alto
      connection-timeout: 3000
      idle-timeout: 600000
```

Un pool sobredimensionado no mejora el throughput más allá de un punto — cada conexión activa compite por los mismos recursos de la base de datos (CPU, I/O), y un pool excesivo puede degradar el rendimiento general de la base de datos al saturarla con más trabajo concurrente del que puede procesar eficientemente. Fórmula de referencia de HikariCP: `conexiones = núcleos_disponibles * 2 + discos_efectivos`, ajustada empíricamente con medición real.

## Caché en memoria vs. distribuida

```java
@Cacheable(value = "productos", key = "#id")
public Producto buscarProducto(String id) { /* ... */ }
```

`@Cacheable` con un backend en memoria (Caffeine) es apropiado para una única instancia; en despliegues con múltiples réplicas, considerar Redis (ver `CLAUDE.md` sección 6.5) para que la caché sea coherente entre instancias, evitando que cada réplica sirva datos potencialmente distintos tras una invalidación.

## Serialización JSON: coste no siempre despreciable

En APIs de alto volumen, la serialización/deserialización de Jackson puede ser un cuello de botella medible. Antes de optimizar:

- Verificar con profiling (JFR) si Jackson aparece efectivamente como hot path, no asumirlo.
- Evitar anotar DTOs con configuraciones de Jackson costosas innecesarias (reflexión excesiva vía `@JsonAnyGetter` dinámico) en el camino caliente de la API si el perfil lo confirma como relevante.

## Warm-up y compilación JIT

La JVM interpreta el bytecode inicialmente y compila a código nativo (JIT) los métodos ejecutados con frecuencia (*hot methods*), tras un número de invocaciones. Esto implica que las primeras peticiones tras un despliegue son más lentas que las siguientes ("cold start" de la JVM, distinto del cold start de un contenedor). En despliegues con autoescalado agresivo o serverless (Lambda con Java), este efecto es más relevante — considerar estrategias de warm-up explícito o CDS/AppCDS (Class Data Sharing) para reducir el tiempo de arranque.

## Checklist

- [ ] ¿Se ha perfilado con JFR/JMC antes de proponer una optimización, en lugar de optimizar por intuición?
- [ ] ¿Se ha descartado una fuga de memoria (crecimiento sostenido sin caída tras GC) antes de atribuir la latencia a otra causa?
- [ ] ¿Se ha verificado el número real de queries por endpoint (N+1) antes de optimizar código de aplicación?
- [ ] ¿El pool de conexiones está dimensionado según núcleos/capacidad de la base de datos, no arbitrariamente?
- [ ] ¿La caché es coherente entre réplicas si el despliegue tiene múltiples instancias?

Ver también: [`../../prompts/performance.md`](../../prompts/performance.md), [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md), [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md), [`hibernate.md`](../hibernate/hibernate.md).
