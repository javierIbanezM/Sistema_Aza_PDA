# Prompt: Diagnóstico de Rendimiento

## Propósito

Guiar el análisis de un problema de rendimiento basándose en medición real, evitando optimizaciones especulativas. Ver sección 11 de `CLAUDE.md` (Rendimiento).

## Instrucciones para Claude Code

Al recibir este prompt junto con un síntoma de rendimiento (latencia alta, timeout, uso de CPU/memoria elevado):

1. **Exige o busca datos de medición real** antes de proponer cualquier cambio: profiling (JFR/Java Flight Recorder, `clinic.js`/`--prof` en Node, Android Profiler), logs de latencia por etapa, métricas de los cuatro golden signals (ver [`../logging.md`](../knowledge/architecture/logging.md)). No optimices basándote en intuición sobre qué "debería" ser lento.

2. **Identifica el tipo de cuello de botella**:
   - CPU-bound: cálculo intensivo, serialización/deserialización costosa, algoritmos ineficientes.
   - I/O-bound: llamadas de red, queries de base de datos, acceso a disco.
   - Contención: locks, pools de conexión agotados, colas saturadas.
   - Garbage Collection: pausas de GC frecuentes o largas por presión de memoria.

3. **Si el cuello de botella involucra base de datos**, verifica primero el antipatrón [N+1 Query](../anti-patterns/n-plus-one.md) y revisa el plan de ejecución real de las queries sospechosas (ver sección 6.2 de `CLAUDE.md` para SQL Server específicamente).

4. **Si el cuello de botella involucra memoria**, verifica el antipatrón [Memory Leak](../anti-patterns/memory-leak.md).

5. **Propón la optimización mínima que resuelve el cuello de botella medido**, no una reescritura amplia especulativa. Prioriza en este orden:
   - Eliminar trabajo innecesario (queries duplicadas, cálculos repetidos, payloads sobredimensionados).
   - Cachear resultados costosos y estables (ver sección 6.5 de `CLAUDE.md`, Redis).
   - Paralelizar trabajo independiente.
   - Solo como último recurso: cambiar de algoritmo/estructura de datos o escalar infraestructura horizontalmente.

6. **Verifica el impacto con la misma medición usada en el paso 1**, comparando antes/después con la misma metodología.

## Formato de salida esperado

```
## Síntoma reportado
[Descripción del problema de rendimiento]

## Medición realizada
[Herramienta usada y datos obtenidos: profiling, logs, métricas]

## Cuello de botella identificado
[CPU / I/O / Contención / GC, con la evidencia que lo confirma]

## Optimización propuesta
[Cambio mínimo propuesto, justificado por la medición]

## Verificación
[Comparación antes/después con la misma medición]
```

## Restricciones

- No propongas "optimizaciones" sin datos de medición que las respalden.
- No introduzcas caché, paralelismo o cambios estructurales complejos si el cuello de botella real medido no lo justifica (ver YAGNI en [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).

Ver también: sección 11 de `CLAUDE.md`, [`../anti-patterns/n-plus-one.md`](../anti-patterns/n-plus-one.md), [`../anti-patterns/memory-leak.md`](../anti-patterns/memory-leak.md).
