# Reference: Spring Boot Versions

Consulta rápida de compatibilidad. Sin razonamiento — ver [`../knowledge/spring/spring-core.md`](../knowledge/spring/spring-core.md) para explicaciones. **Verificar siempre la matriz oficial de Spring antes de fijar versiones en un proyecto nuevo**, ya que este framework puede quedar desactualizado respecto a nuevos releases.

| Spring Boot | Java mínimo | Spring Framework | Estado típico |
|---|---|---|---|
| 3.3.x | 17 (recomendado 21) | 6.1.x | Activo |
| 3.2.x | 17 | 6.1.x | Mantenimiento |
| 3.1.x | 17 | 6.0.x | Mantenimiento extendido |
| 2.7.x | 8 | 5.3.x | Solo mantenimiento (rama final de la serie 2.x) |

## Dependencias asociadas de uso frecuente (verificar versión exacta compatible en el BOM de Spring Boot)

- Hibernate ORM (gestionado por el starter `spring-boot-starter-data-jpa`)
- Spring Security 6.x (para Spring Boot 3.x)
- MapStruct: compatible con cualquier versión de Spring Boot, gestionar su versión de forma independiente (no viene en el BOM de Spring)
- Testcontainers: compatible con cualquier versión de Spring Boot 2.x/3.x, ver `spring-boot-starter-testcontainers` para integración automática en 3.1+

Ver [`java-versions.md`](./java-versions.md) para la versión de Java requerida por cada línea de Spring Boot.
