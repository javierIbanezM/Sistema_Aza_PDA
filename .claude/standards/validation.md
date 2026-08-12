# Standard: Validation

> Norma prescriptiva de validación de datos de entrada, transversal a todos los lenguajes del framework.

## Principio: defensa en dos capas

1. **Validación de forma/sintaxis en el borde** (Controller/DTO): tipo de dato correcto, campos requeridos presentes, formato (email, UUID, fecha), rangos numéricos básicos. Falla rápido, antes de que el dato llegue a la lógica de negocio.
2. **Validación de invariantes de negocio en el dominio** (Entidad/Value Object, ver [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md)): reglas que dependen del estado del sistema o de combinaciones de campos (`fechaFin` debe ser posterior a `fechaInicio`; un pedido no puede confirmarse sin líneas).

Ambas capas son necesarias — la validación de borde no sustituye a la del dominio, porque el dominio puede invocarse desde otros puntos de entrada (jobs internos, eventos, otro servicio) que no pasan por el DTO validado del Controller.

## Reglas obligatorias

1. **Nunca confiar en la validación del cliente/frontend** — toda validación del lado del cliente es una mejora de UX, no una medida de seguridad ni de integridad de datos. El backend valida siempre, independientemente de lo que ya haya validado el frontend.
2. **Whitelisting sobre blacklisting**: definir explícitamente qué formato/valores son válidos, no intentar enumerar qué está prohibido (ver `CLAUDE.md` sección 12, Injection).
3. **Validar en el borde con las herramientas del framework**, no a mano dispersas por el código: `class-validator` + `ValidationPipe` en NestJS, Bean Validation (`@Valid`, `@NotNull`, `@Size`) en Spring, `zod`/`joi` en Express/Fastify.
4. **Mensajes de error de validación específicos por campo**, no un mensaje genérico único — el consumidor de la API necesita saber exactamente qué campo falló y por qué (ver formato de error en `CLAUDE.md` sección 15.1).
5. **Las invariantes de negocio viven en el dominio, no solo en el DTO** — ver [`../anti-patterns/anemic-domain-model.md`](../anti-patterns/anemic-domain-model.md): si la única validación de negocio vive en el Controller/Service externo y la entidad no se protege a sí misma, es una señal de modelo anémico.

## Qué validar según el tipo de dato

| Tipo | Validación mínima |
|---|---|
| Email | Formato RFC válido; considerar verificación de dominio/MX solo si el caso de uso lo justifica |
| Fechas | Formato ISO-8601; rango lógico (no fechas de nacimiento futuras, `fechaFin` > `fechaInicio`) |
| Identificadores (UUID/ID) | Formato correcto; existencia verificada en el dominio, no solo en el DTO |
| Cadenas de texto | Longitud mínima/máxima; whitelisting de caracteres si el campo tiene un formato restringido (código postal, teléfono) |
| Números | Rango válido; signo esperado (cantidades nunca negativas salvo que el dominio lo permita explícitamente) |
| Enums/valores cerrados | Verificar pertenencia al conjunto válido; rechazar valores fuera del enum, no asignar un valor por defecto silenciosamente |

## Sanitización vs. validación

- **Validar** rechaza el input inválido con un error explícito.
- **Sanitizar** modifica el input para hacerlo seguro (escapar HTML, recortar espacios).
- Ambas son necesarias en contextos distintos: sanitizar contenido que se va a renderizar como HTML (prevención de XSS), validar datos que van a interpretarse como comandos, cantidades o identificadores. Nunca sanitizar como sustituto de validar cuando el dato debe cumplir una regla de negocio exacta.

## Checklist

- [ ] ¿Existe validación de forma en el borde (DTO) para todo campo de entrada?
- [ ] ¿Las invariantes de negocio están garantizadas por el dominio, no solo por el DTO?
- [ ] ¿Los mensajes de error de validación son específicos por campo?
- [ ] ¿Se usa whitelisting de formato/valores en lugar de blacklisting?
- [ ] ¿Se ha verificado que el dominio se protege igual si se invoca desde un punto de entrada distinto al Controller validado (job, evento)?

Ver también: [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md), [`../anti-patterns/anemic-domain-model.md`](../anti-patterns/anemic-domain-model.md), [`../anti-patterns/sql-injection.md`](../anti-patterns/sql-injection.md), [`dto.md`](./dto.md).
