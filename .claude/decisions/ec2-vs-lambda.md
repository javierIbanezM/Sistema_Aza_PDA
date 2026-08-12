# EC2 vs. Lambda

## Contexto

Ambos ejecutan código de aplicación en AWS, con modelos operativos y de coste radicalmente distintos.

## Comparativa

| Criterio | EC2 | Lambda |
|---|---|---|
| Gestión de infraestructura | El equipo gestiona SO, parcheo, escalado | Ninguna — AWS gestiona todo el runtime |
| Modelo de coste | Por tiempo de instancia activa, independiente del uso real | Por invocación + tiempo de ejecución real, cero coste si no se ejecuta |
| Duración máxima de ejecución | Sin límite | 15 minutos por invocación |
| Latencia de arranque | Ninguna una vez la instancia está corriendo | Cold start en la primera invocación tras inactividad (ver [`../knowledge/aws-compute/lambda.md`](../knowledge/aws-compute/lambda.md)) |
| Control del entorno | Total (kernel, drivers, dependencias del sistema) | Limitado al runtime soportado (o contenedor custom, con restricciones) |
| Apropiado para carga constante de alto volumen | Sí, más económico con Reserved/Savings Plans | Menos económico a volumen sostenido muy alto |
| Apropiado para carga esporádica/impredecible | Desperdicia capacidad reservada sin uso | Ideal — coste proporcional al uso real |

## Cuándo elegir EC2

- Procesos de larga duración (más de 15 minutos) o con estado en memoria significativo que se beneficia de persistir entre peticiones.
- Carga constante y predecible de alto volumen, donde Reserved Instances/Savings Plans son más económicos que el coste por invocación de Lambda a esa escala.
- Necesidad de control total del entorno (kernel específico, binarios/drivers no soportados en el runtime de Lambda).

## Cuándo elegir Lambda

- Cargas esporádicas o de tráfico muy variable, donde pagar por capacidad reservada constante sería ineficiente.
- Procesamiento orientado a eventos (una petición HTTP, un mensaje de cola, un cambio en S3) de corta duración.
- Se prioriza velocidad de desarrollo y cero gestión operativa sobre control granular del entorno de ejecución.

## Recomendación por defecto de este framework

**Lambda por defecto** para nuevos servicios orientados a eventos de corta duración, dada la menor carga operativa; **EC2** (normalmente vía ECS/EKS sobre EC2, ver [`ecs-vs-eks.md`](./ecs-vs-eks.md)) cuando el proceso excede los límites de Lambda o el volumen sostenido hace que el coste por invocación deje de ser eficiente frente a capacidad reservada.

Ver también: [`../knowledge/aws-compute/ec2.md`](../knowledge/aws-compute/ec2.md), [`../knowledge/aws-compute/lambda.md`](../knowledge/aws-compute/lambda.md), [`ecs-vs-eks.md`](./ecs-vs-eks.md).
