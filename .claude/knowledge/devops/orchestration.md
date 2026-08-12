# Orchestration

> Ver `CLAUDE.md` sección 7.2 para las reglas base de Kubernetes ya fijadas a nivel de framework, [`../aws-compute/ecs.md`](../aws-compute/ecs.md)/[`eks.md`](../aws-compute/eks.md) para las opciones gestionadas en AWS, y [`../../decisions/ecs-vs-eks.md`](../../decisions/ecs-vs-eks.md) para esa comparativa específica. Ver [`../kubernetes/kubernetes.md`](../kubernetes/kubernetes.md) para detalle técnico de manifiestos.

## Por qué orquestar: gestión declarativa del estado deseado

Un orquestador de contenedores mantiene el número deseado de instancias de cada servicio en ejecución, las reemplaza automáticamente ante fallo, y gestiona el ciclo de vida de despliegues — la alternativa (gestionar contenedores individualmente con scripts) no escala más allá de un puñado de servicios sin reinventar, de forma menos robusta, lo que un orquestador ya resuelve.

## Requests/Limits: nunca sin límites en producción

```
requests: lo que el contenedor necesita garantizado (usado para planificar en qué nodo colocarlo)
limits: el máximo que puede consumir antes de ser restringido/terminado
```

Ver `CLAUDE.md` sección 7.2 — un contenedor sin límites puede consumir recursos de forma descontrolada, afectando a otros contenedores en el mismo nodo (*noisy neighbor*); sin `requests`, el orquestador no puede planificar la colocación de forma eficiente.

## Readiness vs. Liveness: propósitos distintos, nunca confundir

```
Readiness: ¿debe este contenedor recibir tráfico AHORA? (puede estar vivo pero no listo, ej. calentando caché)
Liveness: ¿está el proceso genuinamente colgado y necesita reiniciarse?
```

Ver `CLAUDE.md` sección 7.2 — usar el mismo check para ambos propósitos es un error común: un contenedor lento pero funcionando correctamente podría reiniciarse innecesariamente (si Liveness es demasiado estricto) o seguir recibiendo tráfico mientras no está listo (si Readiness no está bien definido).

## Autoescalado basado en métricas reales, no réplicas fijas

```
HorizontalPodAutoscaler: escala entre 2-10 réplicas según CPU/memoria/métrica custom de negocio
```

Ver `CLAUDE.md` sección 7.2 — fijar un número estático de réplicas en un sistema con carga variable desperdicia capacidad en horas valle o resulta insuficiente en picos; el autoescalado basado en métricas reales (incluidas métricas de negocio custom, no solo CPU) ajusta la capacidad al patrón de uso real.

## Rolling updates como estrategia por defecto del orquestador

Ver [`deployment-strategies.md`](./deployment-strategies.md) — la mayoría de orquestadores (Kubernetes, ECS) implementan Rolling Update de forma nativa; Blue/Green y Canary requieren configuración adicional (o herramientas complementarias como Argo Rollouts/Flagger) sobre la base del orquestador.

## Namespaces/clústeres: aislamiento por entorno y por equipo

```
Namespace "pedidos-prod", Namespace "pedidos-staging" — mismo clúster, aislamiento lógico
NetworkPolicy restringiendo tráfico entre namespaces no relacionados
```

Ver `CLAUDE.md` sección 7.2 — el aislamiento lógico (namespaces con NetworkPolicy) es más económico que clústeres físicos separados por entorno, apropiado cuando el requisito de aislamiento no exige separación física completa (ver [`environments.md`](./environments.md) para cuándo sí se justifica esa separación más estricta).

## Checklist

- [ ] ¿Todo contenedor tiene `requests`/`limits` definidos explícitamente?
- [ ] ¿Readiness y Liveness están diferenciados, no usando el mismo check para ambos propósitos?
- [ ] ¿El autoescalado se basa en métricas reales de carga, no en un número fijo de réplicas?
- [ ] ¿El aislamiento entre entornos/equipos usa namespaces con NetworkPolicy cuando la separación física completa no es necesaria?

Ver también: [`../kubernetes/kubernetes.md`](../kubernetes/kubernetes.md), [`../../decisions/ecs-vs-eks.md`](../../decisions/ecs-vs-eks.md), `CLAUDE.md` sección 7.2.
