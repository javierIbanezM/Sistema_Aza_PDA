# Agents — Índice

Un archivo `.md` plano por agente. Cada agente tiene personalidad, tono y **metodología propia** — el valor real de un agente no es repetir el conocimiento de `knowledge/` (eso lo enlaza), sino el *proceso de razonamiento* que aplica antes, durante y después de usar ese conocimiento.

## Agentes de proceso (transversales a cualquier tecnología)

| Agente | Rol |
|---|---|
| [`senior-business-analyst.md`](./senior-business-analyst.md) | Evita construir la solución equivocada: traduce peticiones ambiguas en requisitos verificables |
| [`software-architect.md`](./software-architect.md) | Diseña la estructura del sistema con trade-offs explícitos, sin sobre-ingeniería |
| [`tech-lead.md`](./tech-lead.md) | Convierte el diseño en un plan de implementación secuenciado, con riesgos identificados a tiempo |
| [`senior-code-reviewer.md`](./senior-code-reviewer.md) | Revisa en capas (diseño → anti-patrones → seguridad → tests → estilo), con severidad explícita |
| [`senior-debugger.md`](./senior-debugger.md) | Depuración sistemática: reproducción, hipótesis falsable, causa raíz, test de regresión |

## Agentes técnicos senior (especialistas por tecnología)

| Agente | Rol |
|---|---|
| [`senior-java-engineer.md`](./senior-java-engineer.md) | Implementación Java/Spring/Hibernate con 15-20 años de criterio profesional |
| [`senior-sqlserver-dba.md`](./senior-sqlserver-dba.md) | Diseño de esquema, índices, backup/restore y seguridad a nivel de motor SQL Server |
| [`senior-nodejs-engineer.md`](./senior-nodejs-engineer.md) | Implementación Node.js/TypeScript con dominio del Event Loop y la concurrencia asíncrona |
| [`senior-javascript-engineer.md`](./senior-javascript-engineer.md) | Lenguaje JavaScript y ecosistema navegador: DOM, rendimiento percibido, memoria |
| [`senior-android-engineer.md`](./senior-android-engineer.md) | Kotlin + Compose + Room + Retrofit + Hilt, ciclo de vida y gestión de memoria Android |
| [`senior-integration-engineer.md`](./senior-integration-engineer.md) | Comunicación entre sistemas: APIs externas, ERPs, WMS, CRMs, colas, resiliencia |
| [`senior-aws-architect.md`](./senior-aws-architect.md) | Decisiones de infraestructura AWS (EC2 vs. Lambda, RDS vs. DynamoDB...), no descripción de servicios |
| [`senior-devops-engineer.md`](./senior-devops-engineer.md) | Pipelines, estrategia de despliegue/rollback, IaC, observabilidad de plataforma |
| [`senior-security-engineer.md`](./senior-security-engineer.md) | Threat Model → Riesgo → Mitigación → Implementación → Verificación, Incident Commander |
| [`senior-performance-engineer.md`](./senior-performance-engineer.md) | Medir → Localizar → Datos → Hipótesis → Experimento → Optimización → Validación, nunca por intuición |

## Flujo típico entre agentes

```
senior-business-analyst.md
        ↓ (problema entendido y verificable)
software-architect.md
        ↓ (diseño con trade-offs explícitos)
tech-lead.md
        ↓ (plan de implementación por pasos)
Agente técnico senior (java / sqlserver / nodejs / ...)
        ↓ (código implementado, autorevisado)
senior-code-reviewer.md
        ↓ (aprobado o con hallazgos)
[si hay un bug] senior-debugger.md
```

Ver [`../playbooks/`](../playbooks/) para cómo este flujo se adapta a cada tipo de proceso concreto (nueva funcionalidad, hotfix, cambio de base de datos...).

## Agentes técnicos: bloque completo

Los 9 agentes técnicos senior planificados están construidos. Próximos agentes serán de proceso (`qa`, `project-manager`, `product-owner`, `documentador`) — ver sección siguiente.

Nota: no se creará `senior-kotlin-engineer.md` — Kotlin queda orquestado por `senior-android-engineer.md` hasta que el framework se amplíe a Kotlin Multiplatform/Ktor/no-Android. `senior-integration-engineer.md` se adelantó respecto al orden original por prioridad de uso real.

## Regla de construcción de agentes técnicos

Antes de crear o ampliar un agente técnico, comprobar que existe el dominio correspondiente en `knowledge/`. Si no existe, construirlo primero (y sus `recipes/`/`snippets/` asociadas si aplica) y **después** el agente, reutilizando esa documentación en lugar de duplicarla. Nunca al revés — ver la jerarquía de dependencia completa en `CLAUDE.md` sección 0.1.

## Otros agentes de proceso pendientes

```
qa.md
project-manager.md
product-owner.md
documentador.md
```

