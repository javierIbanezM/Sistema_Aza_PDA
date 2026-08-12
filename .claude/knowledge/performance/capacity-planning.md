# Capacity Planning

> Ver [`../aws-compute/ec2.md`](../aws-compute/ec2.md) y [`../devops/orchestration.md`](../devops/orchestration.md) para el autoescalado que responde a la demanda en tiempo real — este documento cubre la planificación **anticipada** de capacidad, antes de que la demanda real la exija.

## Por qué el autoescalado no sustituye a la planificación

El autoescalado (ver [`../devops/orchestration.md`](../devops/orchestration.md)) responde reactivamente a la demanda ya presente — capacity planning anticipa la demanda **futura** conocida (un evento de marketing, temporada alta, lanzamiento de una funcionalidad con adopción esperada) para que la capacidad esté disponible **antes** de que la necesidad se materialice, ya que el autoescalado tiene límites (tiempo de arranque de nuevas instancias, límites de cuota de AWS) que pueden no ser suficientes ante un pico repentino y masivo.

## Modelar la demanda, no adivinarla

```
Demanda histórica (tráfico de los últimos 12 meses, con estacionalidad identificada)
+ Crecimiento proyectado (tasa de crecimiento de usuarios/transacciones)
+ Eventos conocidos (campaña de marketing, lanzamiento de producto)
= Proyección de demanda futura, con intervalo de confianza, no un único número
```

Una proyección sin intervalo de confianza ("necesitaremos X capacidad") es menos útil que una con rango ("necesitaremos entre X e Y, con Y como escenario conservador") — permite planificar tanto el caso esperado como el margen de seguridad ante incertidumbre.

## De demanda a capacidad: la conversión requiere benchmarking previo

```
Demanda proyectada: 5000 peticiones/segundo en el pico
Capacidad por instancia (medida con benchmarking, ver benchmarking.md): 200 req/s sostenidas
Capacidad necesaria: 25 instancias, + margen de seguridad (ver abajo)
```

Esta conversión depende directamente de haber medido (no asumido) cuánta carga soporta realmente una unidad de capacidad — sin ese dato medido, cualquier plan de capacidad es una suposición, no una planificación.

## Margen de seguridad: cuánto y por qué

```
Capacidad calculada para el pico esperado: 25 instancias
Margen de seguridad (20-30% típico, ajustado según criticidad): 30-33 instancias
```

El margen de seguridad absorbe: error en la proyección de demanda, variabilidad no capturada en el benchmark, y tiempo de reacción del autoescalado ante picos por encima de lo proyectado — el porcentaje exacto depende de la criticidad del sistema y la confianza en la proyección (ver [`../security/risk-assessment.md`](../security/risk-assessment.md) para el mismo principio de margen aplicado a riesgo).

## Capacity Planning y coste: la tensión explícita

Ver [`cost-vs-performance.md`](./cost-vs-performance.md) — sobredimensionar la capacidad "por seguridad" sin límite tiene coste real continuo; capacity planning es, en esencia, encontrar el punto donde el margen de seguridad es suficiente sin ser derrochador, una decisión de negocio tanto como técnica.

## Revisión periódica, no un cálculo de una única vez

La demanda real, el patrón de uso, y el rendimiento por instancia cambian con el tiempo (nuevas funcionalidades, cambios de comportamiento de usuarios) — un plan de capacidad calculado una vez y nunca revisado queda obsoleto; revisar contra la demanda real observada periódicamente, ajustando la proyección futura.

## Checklist

- [ ] ¿La proyección de demanda se basa en datos históricos + crecimiento + eventos conocidos, no en una estimación sin fundamento?
- [ ] ¿La capacidad por unidad se obtuvo de un benchmark real, no de una suposición?
- [ ] ¿Existe un margen de seguridad explícito y justificado, ni ausente ni desproporcionado?
- [ ] ¿El plan se revisa periódicamente contra la demanda real observada?

Ver también: [`benchmarking.md`](./benchmarking.md), [`cost-vs-performance.md`](./cost-vs-performance.md), [`scalability.md`](./scalability.md).
