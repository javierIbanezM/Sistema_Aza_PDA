# Performance Engineering

> Ver [`../../prompts/performance.md`](../../prompts/performance.md) para el prompt operativo de "medir antes de optimizar" ya establecido en el framework — este documento desarrolla la metodología completa detrás de ese prompt, transversal a cualquier tecnología.

## El método: nunca optimizar por intuición

```
Medir → Localizar el cuello de botella → Empezar con datos → Hipótesis →
Experimento → Medición → Optimización → Validación
```

Cada paso existe para eliminar una fuente de error común: sin **medir** primero, no hay línea base contra la que comparar; sin **localizar** el cuello de botella real, se optimiza lo que "parece" lento en lugar de lo que genuinamente lo es; sin **hipótesis** explícita, el "experimento" es solo cambiar cosas al azar hasta que algo mejora, sin entender por qué; sin **validación** final, no hay garantía de que la mejora sea real y no una variación estadística del entorno de medición.

## Por qué la intuición falla sistemáticamente en rendimiento

El cuello de botella real de un sistema rara vez es intuitivo — el código que "parece" ineficiente a simple vista con frecuencia no es el que más tiempo consume en la práctica (ver [`bottleneck-analysis.md`](./bottleneck-analysis.md)); optimizar por intuición desperdicia esfuerzo en partes del sistema que no mueven la aguja, mientras el cuello de botella real permanece sin tocar.

## Formular una hipótesis falsable, no una vaga sospecha

```
Hipótesis vaga (rechazada): "Puede que la base de datos esté lenta"
Hipótesis falsable (aceptada): "El 80% de la latencia p95 del endpoint /pedidos proviene de
una query N+1 en la carga de líneas de pedido — confirmable/refutable con el plan de ejecución"
```

Mismo principio que [`../../agents/senior-debugger.md`](../../agents/senior-debugger.md) exige para depuración de bugs — aplicado aquí a rendimiento: una hipótesis debe poder confirmarse o refutarse con un experimento concreto, no ser una sospecha general sin forma de verificarse.

## El experimento mínimo: cambiar una variable a la vez

```
Antipatrón: cambiar el índice, aumentar la memoria del pool de conexiones, y ajustar
el timeout simultáneamente — si mejora, no se sabe cuál de los tres cambios fue responsable

Correcto: cambiar una única variable, medir, confirmar o refutar la hipótesis específica
de esa variable, antes de pasar a la siguiente
```

## Performance como requisito no funcional explícito, no una aspiración vaga

```
No: "el sistema debe ser rápido"
Sí: "p95 de /api/pedidos < 300ms con 500 usuarios concurrentes" — ver performance-budget.md
```

Un requisito de rendimiento no cuantificado no puede validarse — mismo principio que [`../../agents/senior-business-analyst.md`](../../agents/senior-business-analyst.md) exige para cualquier requisito verificable.

## Checklist

- [ ] ¿Existe una medición de línea base antes de proponer cualquier cambio?
- [ ] ¿El cuello de botella se localizó con datos (profiling, plan de ejecución, wait stats), no por intuición?
- [ ] ¿La hipótesis es falsable, con un experimento concreto que la confirme o refute?
- [ ] ¿Se cambió una única variable por experimento, no varias simultáneamente?
- [ ] ¿La mejora se validó con la misma metodología de medición que la línea base?

Ver también: [`bottleneck-analysis.md`](./bottleneck-analysis.md), [`benchmarking.md`](./benchmarking.md), [`../../prompts/performance.md`](../../prompts/performance.md), [`../../playbooks/performance.md`](../../playbooks/performance.md).
