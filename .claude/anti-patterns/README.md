# Anti-patterns — Índice

Anti-patrones **globales**, aplicables a cualquier dominio tecnológico. Cada documento explica cómo detectarlo, cómo evitarlo y cómo solucionarlo si ya existe en el código.

| Anti-patrón | Descripción corta |
|---|---|
| [`god-object.md`](./god-object.md) | Clase que acumula demasiadas responsabilidades no relacionadas |
| [`spaghetti.md`](./spaghetti.md) | Flujo de control enrevesado, difícil de seguir |
| [`anemic-domain-model.md`](./anemic-domain-model.md) | Entidades sin comportamiento propio, lógica dispersa en servicios externos |
| [`callback-hell.md`](./callback-hell.md) | Anidamiento profundo de callbacks asíncronos |
| [`n-plus-one.md`](./n-plus-one.md) | 1 query + N queries adicionales en lugar de una cantidad constante |
| [`sql-injection.md`](./sql-injection.md) | Concatenación de input de usuario en sentencias SQL |
| [`memory-leak.md`](./memory-leak.md) | Retención de referencias que impide liberar memoria |

Ver también [`../patterns/`](../patterns/) (qué aplicar en su lugar) y [`../standards/`](../standards/) (normas que previenen varios de estos anti-patrones por diseño).
