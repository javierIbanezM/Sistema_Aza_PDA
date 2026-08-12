# Standard: Naming

> Norma prescriptiva. Para el razonamiento y los ejemplos extendidos detrás de estas reglas, ver [`../knowledge/architecture/solid.md`](../knowledge/architecture/solid.md) y [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md). Este documento define la regla a aplicar, no la explica en profundidad.

## Regla general

Los nombres deben ser **descriptivos, específicos y en el idioma consistente con el resto del proyecto** (por defecto, inglés para código; español permitido en comentarios de negocio si el equipo ya lo usa así — ver `CLAUDE.md`, sección 20).

## Clases y tipos

- `PascalCase`.
- Sustantivos o frases nominales: `PedidoRepository`, `CalculadoraDescuento`, `EstadoPedido`.
- Prohibido: nombres genéricos sin calificar (`Manager`, `Helper`, `Utils`, `Processor`) — ver [`../anti-patterns/god-object.md`](../anti-patterns/god-object.md).
- Interfaces: sin prefijo `I` (`PedidoRepository`, no `IPedidoRepository`), salvo que la convención del lenguaje/framework lo exija explícitamente (algunos entornos .NET).
- Clases abstractas: sufijo `Base` solo si aporta claridad real (`ControllerBase`), no por defecto.

## Métodos y funciones

- `camelCase`.
- Verbo o frase verbal que describe la acción: `calcularTotal()`, `confirmarPedido()`, `buscarPorId()`.
- Getters de boolean: prefijo `is`/`has`/`can` (`isConfirmado()`, `hasLineas()`), no `getConfirmado()`.
- Prohibido: parámetros booleanos "flag" sin nombre explícito en la llamada — ver `CLAUDE.md` sección 20.

## Variables

- `camelCase`.
- Nombre que refleja el contenido, no el tipo (`pedidosPendientes`, no `listaPedidos` ni `pedidosArray`).
- Constantes: `UPPER_SNAKE_CASE` (`EDAD_MINIMA_LEGAL`).
- Evitar abreviaturas no estándar (`cli` por `cliente` está bien si es una convención establecida del equipo; `pdo` por `pedido` no).

## Paquetes / módulos / carpetas

- `kebab-case` para carpetas y archivos en proyectos Node/TypeScript/frontend.
- `lowercase` sin guiones para paquetes Java (`com.empresa.pedidos.dominio`), siguiendo la convención del lenguaje.
- Organización por capa o por feature según lo definido en [`../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md).

## Bases de datos

- Tablas: `snake_case`, plural (`pedidos`, `lineas_pedido`).
- Columnas: `snake_case`, singular (`cliente_id`, `fecha_creacion`).
- Claves foráneas: `<tabla_singular>_id` (`cliente_id` en la tabla `pedidos`).
- Índices: `ix_<tabla>_<columnas>` (`ix_pedidos_cliente_id`).
- Procedimientos almacenados (SQL Server): `sp_<Accion><Entidad>` (`sp_CrearPedido`), evitando el prefijo `sp_` reservado para procedimientos del sistema si la convención del equipo así lo exige — usar un prefijo propio (`usp_`) en ese caso.

## Endpoints REST

- Recursos en plural, sustantivos, `kebab-case` en la URL: `/pedidos`, `/lineas-pedido`.
- Sin verbos en la URL (`/pedidos/crear` es incorrecto; `POST /pedidos` es correcto). Ver [`../knowledge/architecture/README.md`](../knowledge/architecture/README.md) y `CLAUDE.md` sección 15.

## Ramas de Git y commits

Ver [`git.md`](./git.md), [`commits.md`](./commits.md) y [`branching.md`](./branching.md) — normas específicas, no duplicadas aquí.

## Checklist

- [ ] ¿Los nombres describen qué es/hace el elemento sin necesitar comentario adicional?
- [ ] ¿Se ha evitado un nombre genérico (`Manager`, `Helper`, `data`, `temp`)?
- [ ] ¿La convención de casing es consistente con el lenguaje/framework del proyecto?
- [ ] ¿Las tablas/columnas de base de datos siguen `snake_case` y las convenciones de FK/índice?
