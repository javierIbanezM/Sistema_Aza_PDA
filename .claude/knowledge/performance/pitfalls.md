# Pitfalls (Performance) — Errores frecuentes

## Optimizar por intuición, sin medir primero

```
"Esto parece lento, vamos a optimizarlo" — sin profiling ni datos que confirmen que es el cuello de botella real
```

Ver [`performance-engineering.md`](./performance-engineering.md).

## Reportar solo el promedio, ocultando la cola de peores casos

```
"Latencia media: 100ms" — mientras el p99 es 3 segundos para el 1% de usuarios
```

Ver [`latency-and-throughput.md`](./latency-and-throughput.md).

## Benchmark de una única ejecución, sin warm-up ni repeticiones

```
Un único run del benchmark, incluyendo las primeras peticiones "en frío" — resultado no representativo
```

Ver [`benchmarking.md`](./benchmarking.md).

## Cambiar varias variables a la vez en un experimento de optimización

```
Cambiar el índice + aumentar memoria + ajustar timeout simultáneamente —
si mejora, no se sabe cuál de los tres cambios fue responsable
```

Ver [`performance-engineering.md`](./performance-engineering.md).

## Servicio con estado local que impide escalado horizontal real

```
Caché en memoria local de la instancia, sin externalizar — al añadir instancias, cada una
tiene su propia caché desincronizada
```

Ver [`scalability.md`](./scalability.md).

## Sobre-aprovisionar capacidad sin revisar nunca contra el uso real

```
"Por si acaso" mantenido indefinidamente, sin revisión periódica del coste que genera
```

Ver [`cost-vs-performance.md`](./cost-vs-performance.md).

## Checklist de revisión rápida

- [ ] ¿Se ha propuesto alguna optimización sin datos de profiling/medición que la respalden?
- [ ] ¿Algún reporte de rendimiento usa solo el promedio, sin percentiles?
- [ ] ¿Algún benchmark se ejecutó una única vez sin repeticiones ni warm-up?
- [ ] ¿Existe capacidad sobreaprovisionada nunca revisada contra el uso real?

Ver también: [`best-practices.md`](./best-practices.md).
