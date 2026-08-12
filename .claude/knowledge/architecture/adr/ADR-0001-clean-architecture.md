# ADR-0001: Adoptar Clean Architecture como organización en capas por defecto

- **Estado**: Aceptado
- **Fecha**: 2026-07-30
- **Decisores**: Equipo de diseño de `claude-code-premium`

## Contexto

El framework necesita una única forma recomendada de organizar el código en capas para todos los dominios (Java, SQL Server, Node, Android...), de modo que las recipes, snippets y ejemplos de cada tecnología sean consistentes entre sí y con las reglas de `CLAUDE.md`. Sin un estándar único, cada dominio tendería a proponer su propia organización, dificultando aplicar los mismos patrones (`Repository`, `Strategy`...) de forma homogénea.

## Decisión

El framework adopta **Clean Architecture / Arquitectura Hexagonal** (ver [`../clean-architecture.md`](../clean-architecture.md)) como organización en capas por defecto: `domain/` → `application/` → `infrastructure/` → `interface/`, con la regla de dependencia apuntando siempre hacia adentro.

## Alternativas consideradas

### Arquitectura en capas tradicional (Controller → Service → DAO, sin regla de dependencia explícita)
- Ventajas: más simple de explicar, ampliamente conocida.
- Desventajas: no fuerza el desacoplo del dominio respecto a la infraestructura; es habitual que la lógica de negocio termine filtrándose a los DAOs o a los Services acoplados al framework.
- Por qué se descartó: no ofrece ninguna garantía estructural de que el dominio permanezca libre de dependencias externas — depende enteramente de la disciplina del equipo, que es precisamente lo que este framework busca reforzar con reglas verificables.

### Arquitectura vertical por feature sin capas explícitas
- Ventajas: alta cohesión por feature, fácil de navegar en proyectos pequeños.
- Desventajas: sin una regla de dependencia explícita entre capas, tiende a degenerar en acoplamiento ad-hoc a medida que el proyecto crece.
- Por qué se descartó: el framework prioriza la enseñabilidad y verificabilidad de la regla de dependencia sobre la conveniencia de navegación en proyectos pequeños.

## Consecuencias

### Positivas
- Todas las recipes de todos los dominios pueden asumir la misma organización de carpetas, haciendo el conocimiento transferible entre tecnologías (ver [`../../java/README.md`](../../java/README.md) y el futuro `knowledge/sqlserver/README.md` de infraestructura de acceso a datos).
- El dominio permanece testeable sin infraestructura real en cualquier lenguaje del framework.

### Negativas / Trade-offs aceptados
- Mayor cantidad de código de mapeo entre capas (DTO ↔ Dominio ↔ Persistencia) que un enfoque más directo — aceptado conscientemente, ver [`../clean-architecture.md`](../clean-architecture.md) sección "Cuándo NO aplicar Clean Architecture completa" para los casos donde este framework reconoce que la organización completa es sobre-ingeniería.

## Referencias

- [`../clean-architecture.md`](../clean-architecture.md)
- [`../ddd.md`](../ddd.md)
