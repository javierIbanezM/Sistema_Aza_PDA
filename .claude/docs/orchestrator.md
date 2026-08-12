# Orchestrator

> Este documento **operacionaliza** el flujo autoorquestado ya descrito en `CLAUDE.md` sección 0.1 — no introduce una regla nueva, la convierte en un árbol de decisión concreto y accionable, con las señales exactas que detectan tecnología, tipo de tarea, y qué recursos activar. Léase junto a `CLAUDE.md` sección 0.1, que sigue siendo la fuente de verdad de las reglas; este documento es la referencia operativa de cómo aplicarlas paso a paso.

## Por qué existe este documento

Sin un punto de entrada explícito, cada tarea nueva obliga a decidir desde cero qué Skill, qué documento de `knowledge/`, qué agente y qué playbook son relevantes — con el framework ya en 69 carpetas de conocimiento, eso es un coste real de cada interacción. Este documento fija el **orden exacto** en que esa decisión se toma, para que sea consistente entre tareas y entre sesiones.

## Flujo completo

```
1. LEER EL PROYECTO
        ↓
2. DETECTAR TECNOLOGÍA(S)
        ↓
3. DETECTAR TIPO DE TAREA
        ↓
4. SELECCIONAR PLAYBOOK (si la tarea es un proceso completo) o SKILL(S) DIRECTAMENTE (si es una tarea acotada)
        ↓
5. EL PLAYBOOK SELECCIONA LOS AGENTES EN ORDEN
        ↓
6. CADA AGENTE CONSULTA SU KNOWLEDGE/PATTERNS/STANDARDS/RECIPES ESPECÍFICOS
        ↓
7. GENERAR RESPUESTA, EJECUTAR CHECKLIST DE CIERRE
```

## Paso 1 — Leer el proyecto

Antes de decidir nada, inspeccionar la raíz del proyecto real (no asumir) — archivos de manifiesto de dependencias, estructura de carpetas, `README.md` propio del proyecto si existe. Esto es lo que informa el Paso 2.

## Paso 2 — Detectar tecnología(s): señales concretas

| Señal encontrada en el proyecto | Tecnología detectada | Dominio(s) de `knowledge/` a activar |
|---|---|---|
| `pom.xml` / `build.gradle` | Java | `knowledge/java/`, `knowledge/spring/` si hay `spring-boot-starter-*` |
| `package.json` con `"express"`/`"@nestjs/core"`/`"fastify"` | Node.js | `knowledge/node/` + `knowledge/express/`/`nestjs/`/`fastify/` según dependencia |
| `package.json` sin framework backend, con `"react"`/HTML/DOM | JavaScript navegador | `knowledge/javascript/` |
| `build.gradle.kts` con `com.android.application` | Android | `knowledge/kotlin/`, `knowledge/android/`, `knowledge/compose/` si hay `androidx.compose` |
| `*.sql`, cadena de conexión SQL Server, `.tSQLt` | SQL Server | `knowledge/sqlserver/` |
| `*.tf` | Terraform | `knowledge/terraform/`, `knowledge/devops/infrastructure-as-code.md` |
| `Dockerfile`/`docker-compose.yml` | Contenerización | `knowledge/docker/`, `knowledge/devops/containerization.md` |
| `*.yaml` con `apiVersion: apps/v1` o similar | Kubernetes | `knowledge/kubernetes/`, `knowledge/devops/orchestration.md` |
| `.github/workflows/*.yml` | GitHub Actions | `knowledge/github-actions/`, `knowledge/devops/ci-cd.md` |
| SDKs de AWS, `aws-sdk`/`boto3`/`software.amazon.awssdk` | AWS | dominio `aws-*` correspondiente al servicio mencionado en la tarea |
| Cliente HTTP hacia un sistema externo con nombre reconocible (SAP, WMS, CRM) | Integración | `knowledge/integration/` |

Cuando hay múltiples señales (ej. Java + Terraform + GitHub Actions), se activan todos los dominios relevantes — una tarea real rara vez toca una única tecnología aislada.

> **Project Analyzer Nivel 1**: esta tabla de señales está implementada de forma ejecutable en [`../../scripts/claude-init`](../../scripts/claude-init) — al instalar el framework en un proyecto, `claude-init` la aplica automáticamente sobre `pom.xml`/`package.json`/`build.gradle`/`Dockerfile`/`*.tf`/`.github/workflows/`, extrae versiones donde es posible (ej. `java.version` de `pom.xml`, versión de `spring-boot-starter-parent`), y genera el informe "Proyecto detectado" con los agentes recomendados. Nivel 1 se limita a manifiestos y configuración, sin analizar el código fuente — detectar arquitectura real (Hexagonal/CQRS/Event-Driven) o riesgos de código (God Objects, acoplamiento) es Nivel 2, no implementado todavía.

## Paso 2b — De tecnología a agente técnico (la pieza que cierra el círculo)

Detectar el dominio de `knowledge/` no es suficiente por sí solo — cada dominio tiene un agente técnico correspondiente que es quien realmente ejecuta el trabajo dentro de un playbook. Esta tabla es la que faltaba entre "qué tecnología es" y "quién la implementa":

| Dominio detectado en el Paso 2 | Agente técnico |
|---|---|
| `knowledge/java/`, `knowledge/spring/` | [`../agents/senior-java-engineer.md`](../agents/senior-java-engineer.md) |
| `knowledge/node/`, `knowledge/express/`, `knowledge/nestjs/`, `knowledge/fastify/` | [`../agents/senior-nodejs-engineer.md`](../agents/senior-nodejs-engineer.md) |
| `knowledge/javascript/` (navegador, sin backend) | [`../agents/senior-javascript-engineer.md`](../agents/senior-javascript-engineer.md) |
| `knowledge/kotlin/`, `knowledge/android/`, `knowledge/compose/` y afines | [`../agents/senior-android-engineer.md`](../agents/senior-android-engineer.md) |
| `knowledge/sqlserver/` | [`../agents/senior-sqlserver-dba.md`](../agents/senior-sqlserver-dba.md) |
| `knowledge/aws-*/` (cualquier dominio AWS) | [`../agents/senior-aws-architect.md`](../agents/senior-aws-architect.md) |
| `knowledge/devops/`, `knowledge/docker/`, `knowledge/kubernetes/`, `knowledge/terraform/`, `knowledge/github-actions/` | [`../agents/senior-devops-engineer.md`](../agents/senior-devops-engineer.md) |
| `knowledge/integration/` | [`../agents/senior-integration-engineer.md`](../agents/senior-integration-engineer.md) |
| `knowledge/security/` | [`../agents/senior-security-engineer.md`](../agents/senior-security-engineer.md) |
| `knowledge/performance/` | [`../agents/senior-performance-engineer.md`](../agents/senior-performance-engineer.md) |

Si el Paso 2 detectó más de un dominio (caso habitual), esta tabla se aplica a **cada uno** — el resultado es más de un agente técnico en la cadena, no una elección única forzada.

## Paso 3 — Detectar tipo de tarea: palabras clave y contexto

| Señal en la petición | Tipo de tarea | Playbook candidato |
|---|---|---|
| "añadir", "crear funcionalidad", "implementar" | Funcionalidad nueva | [`../playbooks/new-feature.md`](../playbooks/new-feature.md) |
| "arreglar", "corregir", "no funciona" (sin urgencia explícita) | Bug | [`../playbooks/bug-fix.md`](../playbooks/bug-fix.md) |
| "producción caída", "urgente", "los usuarios no pueden..." **y la causa raíz ya se sospecha** (ej. "el último despliegue rompió X") | Incidente con causa conocida | [`../playbooks/hotfix.md`](../playbooks/hotfix.md) |
| "producción caída", "urgente" **y la causa raíz es desconocida**, o el incidente requiere coordinar más de un equipo/sistema | Incidente con causa desconocida o alcance amplio | [`../playbooks/incident-response.md`](../playbooks/incident-response.md) |
| "limpiar", "reestructurar", "sin cambiar comportamiento" | Refactor | [`../playbooks/refactor.md`](../playbooks/refactor.md) |
| "lento", "optimizar", "tarda mucho" | Rendimiento | [`../playbooks/performance.md`](../playbooks/performance.md) |
| "vulnerabilidad", "brecha", "acceso no autorizado" | Seguridad | [`../playbooks/security-issue.md`](../playbooks/security-issue.md) |
| "exponer API", "nuevo endpoint público", "contrato" | Nueva API | [`../playbooks/new-api.md`](../playbooks/new-api.md) |
| "cambiar esquema", "nueva tabla", "migración" | Cambio de BD | [`../playbooks/database-change.md`](../playbooks/database-change.md) |
| "desplegar", "release", "subir a producción" | Despliegue | [`../playbooks/deploy-production.md`](../playbooks/deploy-production.md) |
| "revertir", "volver a la versión anterior" | Rollback | [`../playbooks/rollback-production.md`](../playbooks/rollback-production.md) |
| "región caída", "pérdida de datos", "desastre" | Recuperación ante desastres | [`../playbooks/disaster-recovery.md`](../playbooks/disaster-recovery.md) |
| Tarea puntual, pequeña, sin ambigüedad de diseño | Tarea acotada | Sin playbook — ir directo a la Skill técnica correspondiente |

Si ninguna coincide con claridad, o la tarea es genuinamente pequeña y sin ambigüedad, **no forzar un playbook** — usar la Skill técnica directamente (ver `CLAUDE.md` sección 0.1, principio de proporcionalidad ya establecido en varios agentes).

## Paso 4-5 — Generar la cadena de agentes (no solo "el playbook decide")

Cada playbook en [`../playbooks/`](../playbooks/) define sus fases de proceso (Business Analyst → Architect → Tech Lead → ejecución → Code Reviewer → Debugger si hace falta, ver [`../agents/README.md`](../agents/README.md)) — pero la fase de "ejecución" ya **no** se deja como "Skill técnica" genérica: se resuelve sustituyendo esa fase por el/los agente(s) técnico(s) obtenidos en el Paso 2b, en el mismo orden que el playbook ya especifica para el resto de fases.

### Ejemplo completo: "Crear un endpoint" en un proyecto Spring Boot

```
Entrada: "Crear endpoint de confirmación de pedidos"
        ↓
Paso 2: se detecta pom.xml + spring-boot-starter-web + spring-boot-starter-data-jpa
        → dominios: knowledge/java/, knowledge/spring/, knowledge/sqlserver/ (por la cadena de conexión JDBC detectada)
        ↓
Paso 2b: knowledge/java/ → senior-java-engineer.md
         knowledge/sqlserver/ → senior-sqlserver-dba.md
        ↓
Paso 3: "crear" + "endpoint" → tipo de tarea: Funcionalidad nueva → playbooks/new-feature.md
        ↓
Cadena de agentes generada (fases del playbook + agentes del Paso 2b, en orden):

Senior Business Analyst
        ↓
Software Architect
        ↓
Tech Lead
        ↓
Senior Java Engineer          ← agente técnico (Paso 2b, dominio java/spring)
        ↓
Senior SQL Server DBA         ← agente técnico (Paso 2b, dominio sqlserver — solo si el cambio toca esquema/queries)
        ↓
Senior Code Reviewer
        ↓
Senior Debugger (solo si hay incidencias)
```

### Regla de inserción de agentes técnicos en la cadena

- Cuando el Paso 2b devuelve **un único** agente técnico, ese agente sustituye directamente la fase de ejecución del playbook.
- Cuando devuelve **varios** (caso Java + SQL Server, o Java + AWS + DevOps), se insertan **en el orden de las capas que tocan**: primero el agente del dominio de aplicación (Java/Node/Android/JS), después el de persistencia (SQL Server) si el cambio toca esquema/queries, después infraestructura (AWS/DevOps) si el cambio requiere despliegue nuevo, después integración (si el cambio conecta con un sistema externo) — esta secuencia refleja de dentro hacia afuera la misma disciplina de capas que `knowledge/architecture/clean-architecture.md` ya aplica al propio código.
- `senior-security-engineer.md` y `senior-performance-engineer.md` **no se insertan por defecto** en la cadena — se añaden solo cuando el tipo de tarea (Paso 3) es específicamente de seguridad o rendimiento, o cuando otro agente de la cadena escala explícitamente hacia ellos (ver la sección "Cuándo escala" de cada agente).

Cada agente en [`../agents/`](../agents/) ya documenta en su propia sección "Qué knowledge/patterns/standards/recipes consulta" sus fuentes específicas — el orquestador no las repite aquí, solo garantiza que el agente correcto llega a la fase correcta.

## Paso 6 — Cada agente consulta su propia sección "Qué knowledge/patterns/standards/recipes consulta"

Todo agente en [`../agents/`](../agents/) tiene esa información ya documentada explícitamente en su propio archivo — el orquestador no la duplica aquí, delega en cada agente consultar sus fuentes propias según su especialización.

## Paso 7 — Checklist de cierre

Toda tarea, siga o no un playbook completo, cierra con [`../checklists/architecture.md`](../checklists/architecture.md) antes de considerarse terminada — ver la Skill técnica correspondiente para checklists adicionales específicos del dominio.

## Casos ambiguos: cuándo NO forzar este flujo

- **Preguntas de consulta pura** ("¿qué hace `@Transactional`?"): responder directamente desde `knowledge/`, sin activar ningún playbook ni agente.
- **Tarea que cruza dominios sin un playbook exacto**: usar el playbook más cercano como esqueleto, adaptando conscientemente — nunca forzar un playbook que no encaja solo por seguir el proceso.
- **Ambigüedad genuina de qué tipo de tarea es**: preguntar antes de asumir, en lugar de adivinar y aplicar el playbook equivocado.

Ver también: `CLAUDE.md` sección 0.1, [`../agents/README.md`](../agents/README.md), [`../playbooks/README.md`](../playbooks/README.md), [`../skills/README.md`](../skills/README.md).
