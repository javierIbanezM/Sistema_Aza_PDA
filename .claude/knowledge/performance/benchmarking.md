# Benchmarking

> Ver [`performance-engineering.md`](./performance-engineering.md) para el método general de medición — este documento cubre el rigor estadístico específico que un benchmark válido requiere, distinto del profiling puntual ya documentado por stack (`knowledge/java/performance.md`, `knowledge/node/performance.md`).

## Benchmarking vs. profiling: propósitos distintos

```
Profiling (ver knowledge/java/performance.md, etc.): ¿DÓNDE se consume el tiempo/recursos dentro del sistema?
Benchmarking: ¿CUÁNTA capacidad/rendimiento tiene el sistema en su conjunto, de forma comparable y repetible?
```

Un benchmark responde "¿cuántas peticiones por segundo soporta este servicio con esta configuración?" — un profiler responde "¿por qué esta petición concreta tardó 500ms?". Ambos son necesarios, en momentos distintos del proceso de [`performance-engineering.md`](./performance-engineering.md).

## Rigor estadístico: un benchmark no es una única ejecución

```
Antipatrón: ejecutar el benchmark una vez, tomar el número como definitivo
Correcto: múltiples ejecuciones, reportar percentiles (no solo media), y desviación entre ejecuciones
```

La variabilidad del entorno (otros procesos, red, garbage collection en momentos aleatorios) hace que una única ejecución pueda no ser representativa — reportar un rango (p50/p95/p99) y la variabilidad entre repeticiones da una imagen honesta de la incertidumbre real, no una falsa precisión de un único número.

## Warm-up: excluir el estado transitorio de arranque

```
Primeras N peticiones descartadas del resultado — JIT compilation (JVM/V8), pools de
conexión inicializándose, cachés vacías — el sistema no está en su estado estable todavía
```

Medir desde la primera petición mezcla el coste de "arrancar en frío" con el rendimiento real en estado estable — un benchmark válido excluye explícitamente el periodo de warm-up del resultado reportado, salvo que el objetivo específico sea medir precisamente ese comportamiento de arranque.

## Reproducibilidad: mismo entorno, mismas condiciones, comparables entre ejecuciones

```
Un benchmark ejecutado en un entorno distinto (hardware diferente, con o sin otros procesos
compitiendo por recursos) NO es comparable con uno anterior, aunque el número parezca similar
```

Para que un benchmark sirva para comparar "antes" y "después" de un cambio (ver [`performance-engineering.md`](./performance-engineering.md), validación), debe ejecutarse en condiciones lo más idénticas posible — mismo hardware/instancia, mismo estado de carga de fondo, mismos parámetros — de lo contrario, la comparación no aísla el efecto del cambio evaluado.

## Benchmarking sintético vs. representativo de carga real

```
Sintético: patrón de carga artificial diseñado para estresar un componente específico
Representativo: patrón de carga que imita el tráfico real observado en producción (distribución de endpoints, tamaño de payloads)
```

Un benchmark puramente sintético puede no reflejar el comportamiento real bajo el patrón de uso genuino del sistema — complementar con benchmarks basados en tráfico real capturado (o su distribución estadística) cuando la decisión que depende del benchmark es de alto impacto.

## Checklist

- [ ] ¿El benchmark se ejecutó múltiples veces, reportando percentiles y variabilidad, no un único número?
- [ ] ¿Se excluyó explícitamente el periodo de warm-up del resultado?
- [ ] ¿Las condiciones del entorno son reproducibles y comparables entre ejecuciones "antes/después"?
- [ ] ¿El patrón de carga usado refleja razonablemente el uso real, no solo un escenario sintético arbitrario?

Ver también: [`performance-engineering.md`](./performance-engineering.md), [`capacity-planning.md`](./capacity-planning.md), [`load-testing-types.md`](./load-testing-types.md).
