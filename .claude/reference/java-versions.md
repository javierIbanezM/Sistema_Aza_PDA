# Reference: Java Versions

Consulta rápida de versiones LTS y su soporte. Sin razonamiento — ver [`../knowledge/java/`](../knowledge/java/) para explicaciones.

| Versión | Tipo | Fin de soporte (Oracle Premier) | Características clave introducidas |
|---|---|---|---|
| 8 | LTS | Vencido (soporte extendido de pago) | Lambdas, Streams, `Optional` |
| 11 | LTS | Vencido (soporte extendido de pago) | `var`, HTTP Client estándar |
| 17 | LTS | Vencido (soporte extendido de pago) | Sealed classes, pattern matching en `instanceof` |
| 21 | LTS | 2028 (ampliable) | Virtual Threads (Project Loom), pattern matching en `switch`, record patterns |
| 25 | LTS | 2031 (ampliable) | Última LTS — verificar release notes oficiales para novedades exactas |

**Recomendación por defecto de este framework**: Java 21 LTS para proyectos nuevos (Virtual Threads mejora significativamente el throughput en cargas I/O-bound como típicas APIs REST, sin cambiar el modelo de programación).

## Compatibilidad rápida con Spring Boot

| Spring Boot | Java mínimo requerido |
|---|---|
| 3.x | 17 |
| 2.7.x (mantenimiento) | 8 |

Ver [`spring-boot-versions.md`](./spring-boot-versions.md) para la matriz completa.
