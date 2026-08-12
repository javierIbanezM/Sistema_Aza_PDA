# /analyze

## Qué hace

Ejecuta el Project Analyzer Nivel 1 sobre el proyecto actual y resume qué Skills/agentes son relevantes — sin implicar ningún cambio de código.

## Invoca

- Lógica: [`../docs/orchestrator.md`](../docs/orchestrator.md) Paso 1-2 (misma detección que ejecuta [`../../scripts/claude-init`](../../scripts/claude-init) al instalar, aplicada aquí bajo demanda sobre el estado actual del proyecto).
- Sin playbook ni agente técnico invocado todavía — este comando solo informa, no actúa.

## Cuándo usarlo

Al empezar a trabajar en un proyecto que ya tiene el framework instalado pero cuyo stack pudo cambiar desde la instalación (nuevas dependencias, nueva infraestructura) — o simplemente para confirmar qué agentes recomienda el framework antes de pedir una tarea concreta.

## Salida esperada

El mismo formato "Proyecto detectado" (Lenguajes / Frameworks / Base de datos / Infraestructura / Herramientas / Agentes recomendados) que `claude-init` genera en la instalación.

## Equivalente manual

Leer [`../docs/orchestrator.md`](../docs/orchestrator.md) Paso 2 y aplicar la tabla de señales manualmente.
