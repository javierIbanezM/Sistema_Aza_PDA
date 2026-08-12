# Playbook: Refactor

## Cuándo usar este playbook

Un cambio estructural del código sin alterar su comportamiento observable — desde extraer un método hasta dividir un [God Object](../anti-patterns/god-object.md) detectado en producción. Para refactors triviales de bajo riesgo (renombrar, extraer un método simple), el flujo completo puede ser desproporcionado — ver criterio de proporcionalidad en [`../agents/software-architect.md`](../agents/software-architect.md).

## Fases

### 1. Software Architect (o Senior Code Reviewer, según el origen)

- **Hace**: identifica y justifica el anti-patrón o problema estructural (ver [`../anti-patterns/`](../anti-patterns/)) que motiva el refactor — nunca se refactoriza "porque sí" o por preferencia de estilo sin un problema real de mantenibilidad, testabilidad o extensibilidad.
- **Produce**: justificación explícita del refactor, con el patrón/estructura objetivo (ver [`../patterns/`](../patterns/)).
- **Entrega a**: Tech Lead.
- **Terminada cuando**: el problema estructural está identificado con evidencia (no solo intuición) y hay una estructura objetivo clara.

### 2. Tech Lead

- **Hace**: verifica que existe red de seguridad de tests suficiente (ver [`../knowledge/architecture/refactoring.md`](../knowledge/architecture/refactoring.md)); si no, planifica primero la escritura de tests de caracterización como paso previo obligatorio. Divide el refactor en pasos pequeños e independientes.
- **Produce**: plan de refactor por pasos, cada uno verificable con los tests existentes en verde.
- **Entrega a**: el agente técnico correspondiente a la tecnología del código afectado (ver `docs/orchestrator.md` Paso 2b).
- **Terminada cuando**: cada paso del plan es lo suficientemente pequeño para revertirse de forma aislada si algo falla (ver [`../prompts/refactor.md`](../prompts/refactor.md)).

### 3. Agente técnico (ejecución)

- **Hace**: ejecuta cada paso del plan, verificando que los tests permanecen en verde tras cada uno — **nunca** mezcla el refactor con funcionalidad nueva en el mismo paso/commit.
- **Produce**: código refactorizado, mismo comportamiento observable, tests en verde en todo momento.
- **Entrega a**: Senior Code Reviewer.
- **Terminada cuando**: todos los pasos están completos y el comportamiento observable es idéntico al inicial (verificado, no asumido).

### 4. Senior Code Reviewer

- **Hace**: confirma que el refactor no introdujo cambios funcionales encubiertos, que efectivamente resuelve el anti-patrón identificado en la fase 1, y que no introdujo uno nuevo.
- **Produce**: aprobación o hallazgos.
- **Entrega a**: despliegue si aprobado.
- **Terminada cuando**: no quedan hallazgos bloqueantes.

## Diagrama de flujo

```
Software Architect (justifica el refactor)
        ↓
Tech Lead (verifica red de seguridad + divide en pasos)
        ↓
Agente técnico (ejecuta paso a paso, tests en verde siempre)
        ↓
Senior Code Reviewer (confirma comportamiento idéntico + anti-patrón resuelto)
        ↓
   [Producción]
```

## Regla de oro de este playbook

Un refactor sin tests previos no es un refactor — es una reescritura sin red de seguridad. Si la fase 2 detecta que no hay tests suficientes, escribirlos es un paso obligatorio, no opcional, antes de tocar el código de producción.
