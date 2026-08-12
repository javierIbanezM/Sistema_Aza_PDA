# Cost vs. Performance

> Ver [`capacity-planning.md`](./capacity-planning.md) para la planificación de capacidad de la que este trade-off forma parte, y [`../aws-compute/ec2.md`](../aws-compute/ec2.md) para los modelos de precio (On-Demand/Reserved/Spot) que son la palanca técnica principal de este trade-off en AWS.

## El rendimiento adicional tiene coste marginal decreciente

```
De p95=1000ms a p95=500ms: puede lograrse con una optimización estructural (índice faltante) — coste bajo
De p95=500ms a p95=300ms: requiere más capacidad/optimización — coste medio
De p95=300ms a p95=200ms: puede requerir rediseño significativo (caching agresivo, edge computing) — coste alto
De p95=200ms a p95=150ms: coste desproporcionado para una mejora marginal — rendimientos decrecientes
```

Cada mejora incremental de rendimiento, pasado cierto punto, cuesta más que la anterior para un beneficio marginal cada vez menor — la pregunta relevante no es "¿podemos ser más rápidos?" (casi siempre sí, con suficiente inversión) sino "¿el coste adicional de esta mejora está justificado por su impacto real en el negocio?".

## Conectar rendimiento con impacto de negocio, no optimizar en abstracto

```
"p95 de 300ms a 200ms" sin contexto de negocio: mejora técnica sin justificación clara de prioridad
"Cada 100ms de latencia adicional en checkout reduce la conversión en X%" (dato real del negocio):
ahora la inversión en reducir latencia tiene un retorno cuantificable, comparable con otras prioridades
```

Ver [`../../agents/senior-business-analyst.md`](../../agents/senior-business-analyst.md) — sin esta traducción a impacto de negocio, la priorización de trabajo de rendimiento compite en desventaja frente a funcionalidades con impacto de negocio más directamente visible, aunque el rendimiento pueda tener un retorno real igual o mayor.

## El coste de sobre-aprovisionar "por seguridad"

```
Capacidad dimensionada con margen excesivo, "por si acaso", nunca revisada contra el uso real:
coste continuo mensual pagando capacidad que nunca se utiliza
```

Ver [`capacity-planning.md`](./capacity-planning.md) — el margen de seguridad es necesario, pero un margen excesivo y nunca cuestionado es tan problemático como uno insuficiente, solo que el coste se paga de forma silenciosa y continua en lugar de manifestarse como un incidente visible.

## Optimizaciones "gratis": las que reducen coste y mejoran rendimiento simultáneamente

```
Eliminar una query N+1: mejora latencia Y reduce carga de base de datos (menor coste de capacidad)
Cachear un dato de alta lectura: mejora latencia Y reduce llamadas al origen (menor coste)
```

Antes de asumir que rendimiento y coste están en tensión, identificar las optimizaciones que mejoran ambos simultáneamente (típicamente, eliminar trabajo innecesario) — estas se priorizan siempre, ya que no requieren justificar un trade-off, a diferencia de "más capacidad = más coste, más rendimiento".

## Decidir el punto de equilibrio explícitamente, con datos

```
Coste de +30 instancias adicionales para bajar p95 de 300ms a 200ms: $X/mes
Beneficio estimado de negocio de esa mejora (conversión, retención): $Y/mes
Decisión: proceder si Y > X con margen razonable, documentada explícitamente, no asumida
```

## Checklist

- [ ] ¿La mejora de rendimiento propuesta está conectada a un impacto de negocio cuantificable, no justificada solo técnicamente?
- [ ] ¿Se ha evaluado si existen optimizaciones que reducen coste y mejoran rendimiento simultáneamente antes de asumir el trade-off?
- [ ] ¿La capacidad actual se revisa periódicamente contra el uso real, evitando sobre-aprovisionamiento silencioso?

Ver también: [`capacity-planning.md`](./capacity-planning.md), [`../aws-compute/ec2.md`](../aws-compute/ec2.md), [`performance-budget.md`](./performance-budget.md).
