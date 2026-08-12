# Documentación Técnica

> Documento único de referencia sobre cómo y cuándo documentar. Las plantillas concretas viven en `.claude/templates/` (ADR.md, RFC.md, README.md) y se enlazan desde aquí, no se duplican.

## Principio general

La documentación técnica más valiosa es la que **vive junto al código** y se actualiza en el mismo PR que el cambio que documenta. La documentación externa (wikis, documentos compartidos separados del repositorio) tiende a desincronizarse rápidamente y se convierte en una fuente de desinformación en lugar de información.

## Tipos de documentación y cuándo usar cada uno

### README.md

Punto de entrada de cualquier repositorio. Debe responder, en este orden, a las preguntas que se hace alguien que ve el proyecto por primera vez:

1. ¿Qué hace este proyecto? (una frase, no un ensayo)
2. ¿Cómo lo levanto en mi máquina en los próximos cinco minutos?
3. ¿Cómo ejecuto los tests?
4. ¿Cómo despliego / qué pipeline lo despliega?
5. ¿Dónde está la documentación más profunda (arquitectura, ADRs) si necesito más detalle?

No debe intentar explicar el dominio de negocio completo ni la arquitectura interna en detalle — eso corresponde a documentación específica enlazada desde aquí (ver plantilla en `.claude/templates/README.md`).

### ADR — Architecture Decision Record

Documenta **una decisión arquitectónica concreta**, el contexto que la motivó, las alternativas consideradas y las consecuencias aceptadas. Se escribe **en el momento de tomar la decisión**, no después como justificación retroactiva.

**Cuándo escribir un ADR:**
- Elegir una tecnología nueva (base de datos, framework, librería con impacto arquitectónico).
- Cambiar un patrón arquitectónico establecido (pasar de monolito a microservicios, introducir CQRS).
- Decisiones con alto coste de reversión (formato de almacenamiento de datos, protocolo de comunicación entre servicios).

**Cuándo NO es necesario:**
- Decisiones de implementación local sin impacto en otras partes del sistema (nombrar una variable, elegir entre dos formas equivalentes de escribir un bucle).
- Decisiones ya cubiertas por una regla general de `CLAUDE.md` que no se está cuestionando.

Los ADRs son **inmutables una vez aceptados**: si la decisión cambia más adelante, se escribe un ADR nuevo que referencia y supersede al anterior, en lugar de editar el original. Esto preserva el historial de *por qué* se tomó cada decisión en su momento, con el contexto que existía entonces.

Plantilla: [`.claude/templates/ADR.md`](../../templates/ADR.md).

### RFC — Request for Comments

Documenta una **propuesta de cambio significativo** antes de implementarla, para recoger feedback de otros equipos o stakeholders cuando el cambio afecta a más de un equipo/servicio. A diferencia del ADR (que documenta una decisión ya tomada), el RFC es un documento de **discusión abierta**.

**Cuándo escribir un RFC:**
- Un cambio de contrato de API consumido por otros equipos.
- Una migración de datos o de infraestructura que afecta a varios servicios.
- Un cambio de proceso (nuevo flujo de CI/CD, nueva política de branching) que afecta a todo el equipo de ingeniería.

El RFC pasa por un periodo de comentarios explícito antes de aprobarse; una vez aprobado e implementado, puede resumirse en un ADR que documente la decisión final tomada.

Plantilla: [`.claude/templates/RFC.md`](../../templates/RFC.md).

### Comentarios en código

Los comentarios explican el **por qué**, no el **qué**. Si el código necesita un comentario para explicar qué hace, normalmente es señal de que el código podría escribirse de forma más clara (nombres mejores, extracción de método — ver [`refactoring.md`](./refactoring.md)) en lugar de compensar la falta de claridad con un comentario.

```java
// Mal: el comentario repite lo que el código ya dice
// Incrementa el contador en 1
contador++;

// Bien: el comentario explica una decisión no obvia
// Se usa un contador manual en vez de stream().count() porque el stream
// consume el iterador y necesitamos reutilizarlo más abajo en la línea 58.
contador++;
```

Comentarios que documentan una decisión de negocio no obvia, una limitación conocida, o el motivo de una solución que a simple vista parece innecesariamente compleja, sí aportan valor.

### Documentación de API

Ver [`api-documentation-openapi-swagger.md`](./openapi.md) — la documentación de contratos de API se trata como un documento independiente por su extensión y especificidad.

## Documentación que envejece mal (evitar)

- Diagramas de arquitectura en herramientas externas desconectadas del repositorio, que nadie actualiza tras el primer mes.
- Documentos de "cómo funciona el sistema" que reexplican en prosa lo que el código ya expresa con claridad — se desincronizan del código en la primera refactorización.
- Wikis genéricas sin dueño claro ni proceso de revisión, que acumulan información contradictoria de distintas épocas del proyecto.

La alternativa preferida: diagramas versionados como código cuando sea posible (Mermaid, PlantUML dentro de Markdown en el propio repositorio), y documentación de referencia (`docs/`) que se actualiza como parte del *Definition of Done* de cualquier tarea que la afecte (ver `CLAUDE.md`, sección 21).

## Checklist

- [ ] ¿El README permite a alguien nuevo levantar el proyecto en minutos, sin depender de conocimiento tribal?
- [ ] ¿Existe un ADR para cada decisión arquitectónica relevante tomada en este cambio?
- [ ] ¿Los comentarios en el código explican el "por qué", no repiten el "qué"?
- [ ] ¿La documentación de API está sincronizada con el contrato real (generada o verificada automáticamente, no mantenida a mano de forma desconectada)?
- [ ] ¿Se ha actualizado la documentación relevante como parte de este mismo cambio, no como una tarea "para después"?

Ver también: [`api-documentation-openapi-swagger.md`](./openapi.md), plantillas en `.claude/templates/`.
