# Patterns — Índice

Patrones de diseño **globales**, reutilizables por cualquier dominio tecnológico (Java, Node, Android, .NET...). Nunca se crean versiones específicas por tecnología de un mismo patrón — un ejemplo de implementación en un lenguaje concreto vive dentro del propio documento (sección "Ejemplo"), pero el documento en sí es único.

| Patrón | Descripción corta |
|---|---|
| [`repository.md`](./repository.md) | Abstracción de persistencia como colección en memoria de Agregados |
| [`factory.md`](./factory.md) | Encapsula la creación de objetos complejos o con variantes |
| [`builder.md`](./builder.md) | Construcción paso a paso de objetos con muchos campos/opcionales |
| [`strategy.md`](./strategy.md) | Familia de algoritmos intercambiables en tiempo de ejecución |
| [`observer.md`](./observer.md) | Notificación uno-a-muchos ante cambios de estado |
| [`adapter.md`](./adapter.md) | Traduce una interfaz incompatible a la que el cliente espera |
| [`facade.md`](./facade.md) | Interfaz simple sobre un subsistema complejo |
| [`decorator.md`](./decorator.md) | Añade responsabilidades a un objeto dinámicamente, de forma combinable |
| [`cqrs.md`](./cqrs.md) | Separación de responsabilidades entre comandos (escritura) y queries (lectura) |
| [`circuit-breaker.md`](./circuit-breaker.md) | Protege ante fallos repetidos de un servicio externo |

Ver también [`../anti-patterns/`](../anti-patterns/) (qué evitar) y [`../decisions/`](../decisions/) (comparativas entre enfoques alternativos).
