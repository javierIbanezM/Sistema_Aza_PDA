# Bottleneck Analysis

> Para herramientas específicas de diagnóstico por stack, ver [`../java/performance.md`](../java/performance.md) (JFR, GC), [`../node/performance.md`](../node/performance.md) (inspector, Worker Threads), [`../sqlserver/performance.md`](../sqlserver/performance.md) (execution plans, wait stats), [`../android/performance.md`](../android/performance.md) (Profiler). Este documento cubre el **marco conceptual transversal** para localizar el cuello de botella antes de elegir qué herramienta específica usar.

## El principio: en cualquier sistema, un único recurso limita el throughput

Una cadena de procesamiento (petición → aplicación → base de datos → respuesta) tiene, en un momento dado, un único recurso que es el limitante real — CPU, memoria, I/O de disco, I/O de red, o contención de lock. Optimizar cualquier otro recurso que no sea el limitante no mejora el rendimiento global, por mucho esfuerzo que se invierta ahí — identificar **cuál** es el limitante real es el paso que precede a cualquier optimización.

## Método USE: Utilization, Saturation, Errors

```
Para cada recurso candidato (CPU, memoria, disco, red):
Utilization: ¿qué % del tiempo el recurso está ocupado?
Saturation: ¿hay trabajo en cola esperando ese recurso, más allá de lo que puede procesar?
Errors: ¿hay errores del propio recurso (timeouts de disco, paquetes de red perdidos)?
```

Recorrer sistemáticamente cada recurso con estas tres preguntas evita el sesgo de investigar solo el recurso que "parece" sospechoso por intuición — un recurso con alta Utilization pero sin Saturation puede no ser el cuello de botella real; uno con Saturation incluso con Utilization moderada sí lo es.

## Cuello de botella de CPU: síntomas y confirmación

```
Síntoma: CPU consistentemente >80-90%, tiempo de respuesta correlacionado con picos de CPU
Confirmación: profiling (ver knowledge/java/performance.md JFR, knowledge/node/performance.md --prof)
             identifica en qué método/función se consume el tiempo de CPU real
```

## Cuello de botella de memoria: presión de GC o fugas

```
Síntoma: pausas de GC frecuentes/largas (Java, ver ../java/performance.md), o crecimiento
         sostenido de memoria sin estabilizarse (fuga, ver ../../anti-patterns/memory-leak.md)
```

## Cuello de botella de I/O (disco/red): latencia de espera, no de cómputo

```
Síntoma: el proceso pasa la mayor parte del tiempo esperando (wait time), no calculando —
         CPU baja pero latencia alta simultáneamente es la señal característica de I/O-bound
Confirmación: wait stats (ver ../sqlserver/performance.md), latencia de red medida explícitamente
```

## Contención (locks/concurrencia): el cuello de botella menos visible

```
Síntoma: throughput no escala al añadir más hilos/instancias — o incluso EMPEORA
Confirmación: análisis de locks/deadlocks (ver ../sqlserver/deadlocks.md), profiling de contención de threads
```

Ver [`../../patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md) — la contención es engañosa porque no se manifiesta como alto uso de un recurso físico (CPU/memoria/disco parecen normales), sino como threads esperándose mutuamente; requiere herramientas específicas de análisis de concurrencia, no las métricas de recursos estándar.

## Ley de Amdahl: el límite teórico de paralelizar

```
Aceleración máxima = 1 / ((1 - P) + P/N)
donde P = fracción paralelizable del trabajo, N = número de unidades de paralelismo
```

Si el 20% del trabajo es inherentemente secuencial (no paralelizable), ninguna cantidad de hilos/instancias adicionales supera una aceleración de 5x — antes de invertir en paralelizar más, calcular si el límite teórico ya está cerca, evitando esfuerzo desproporcionado al beneficio posible.

## Checklist

- [ ] ¿Se aplicó el método USE sistemáticamente a cada recurso candidato, no solo al que "parecía" sospechoso?
- [ ] ¿El cuello de botella identificado está confirmado con una herramienta de diagnóstico específica del stack, no solo inferido?
- [ ] ¿Se ha descartado contención de locks/concurrencia, que no se manifiesta como alto uso de recursos físicos?
- [ ] ¿Se ha evaluado el límite teórico de paralelización (Amdahl) antes de invertir en más paralelismo?

Ver también: [`performance-engineering.md`](./performance-engineering.md), [`../java/performance.md`](../java/performance.md), [`../node/performance.md`](../node/performance.md), [`../sqlserver/performance.md`](../sqlserver/performance.md), [`../android/performance.md`](../android/performance.md).
