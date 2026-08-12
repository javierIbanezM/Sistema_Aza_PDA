# ADR-0002: Adoptar Repository como patrón de persistencia por defecto

- **Estado**: Aceptado
- **Fecha**: 2026-07-30
- **Decisores**: Equipo de diseño de `claude-code-premium`

## Contexto

Dada la adopción de Clean Architecture (ver [`ADR-0001-clean-architecture.md`](./ADR-0001-clean-architecture.md)), la capa de aplicación necesita un puerto de salida estándar para persistencia que sea consistente entre dominios (Java/JPA, SQL Server, Node/TypeORM, Android/Room...).

## Decisión

El framework adopta el patrón **Repository** (ver [`../../patterns/repository.md`](../../../patterns/repository.md)) como abstracción de persistencia por defecto — un Repository por Agregado (ver [`../ddd.md`](../ddd.md)), nunca por tabla, expuesto como interfaz en `application/ports/` e implementado en `infrastructure/`.

## Alternativas consideradas

### Active Record (la propia entidad sabe persistirse)
- Ventajas: menos código, muy directo para CRUDs simples (Rails, algunos ORMs ligeros de Node).
- Desventajas: acopla el dominio a la infraestructura de persistencia por diseño — viola la regla de dependencia de Clean Architecture (ver [`ADR-0001-clean-architecture.md`](./ADR-0001-clean-architecture.md)) de forma estructural, no evitable.
- Por qué se descartó: incompatible con la decisión ya tomada en ADR-0001.

### Acceso directo al ORM/driver desde los casos de uso, sin capa de abstracción
- Ventajas: cero indirección adicional.
- Desventajas: imposibilita testear casos de uso sin infraestructura real; acopla la capa de aplicación a la tecnología de persistencia concreta.
- Por qué se descartó: el framework prioriza la testabilidad de los casos de uso con Fakes en memoria (ver [`../testing.md`](../testing.md)) sobre la eliminación de esa capa de indirección.

## Consecuencias

### Positivas
- Los casos de uso de cualquier dominio se testean igual: Fake en memoria del Repository, sin base de datos real.
- Cambiar de tecnología de persistencia (o testear con una implementación distinta) no afecta a la capa de aplicación.

### Negativas / Trade-offs aceptados
- Código de mapeo adicional entre entidad de dominio y entidad de persistencia — aceptado como parte del mismo trade-off de ADR-0001.
- Riesgo de degenerar en un Repository "todo genérico" (`GenericRepository<T>`) si no se sigue la guía de [`../../patterns/repository.md`](../../../patterns/repository.md) — mitigado documentando explícitamente ese error común en el propio patrón.

## Referencias

- [`ADR-0001-clean-architecture.md`](./ADR-0001-clean-architecture.md)
- [`../../patterns/repository.md`](../../../patterns/repository.md)
- [`../../decisions/repository-vs-jdbc.md`](../../../decisions/repository-vs-jdbc.md)
