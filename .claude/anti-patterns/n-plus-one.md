# Antipatrón: N+1 Query

## Descripción

Ocurre cuando el código ejecuta 1 query para obtener una lista de N registros, y después ejecuta N queries adicionales (una por cada registro) para obtener datos relacionados, en lugar de resolver toda la información necesaria en una cantidad constante y reducida de queries. El resultado son N+1 accesos a base de datos donde bastaría con 1 o 2, degradando el rendimiento de forma proporcional al tamaño de la lista.

## Cómo detectarlo

- Un bucle `for`/`forEach` sobre una colección de entidades que dentro realiza una llamada a base de datos por cada elemento.
- Logs de SQL (con el logging de queries activado en desarrollo) mostrando decenas o cientos de queries casi idénticas, solo con el identificador cambiando, en respuesta a una única petición HTTP.
- Latencia de un endpoint que crece de forma aproximadamente lineal con el tamaño de la colección devuelta, en lugar de mantenerse aproximadamente constante.
- Uso de relaciones `FetchType.LAZY` en JPA/Hibernate sin `JOIN FETCH`/`@EntityGraph`, accedidas dentro de un bucle sobre la colección padre.

## Ejemplo del antipatrón (Java/JPA)

```java
List<Pedido> pedidos = pedidoRepository.findAll(); // 1 query

for (Pedido pedido : pedidos) {
    Cliente cliente = pedido.getCliente(); // relación LAZY: dispara una query SEPARADA por cada pedido
    System.out.println(cliente.getNombre());
}
// Total: 1 + N queries, donde N es el número de pedidos
```

## Ejemplo del antipatrón (Node.js/TypeORM)

```typescript
const pedidos = await pedidoRepository.find(); // 1 query

for (const pedido of pedidos) {
  const cliente = await clienteRepository.findOneBy({ id: pedido.clienteId }); // N queries adicionales
  console.log(cliente.nombre);
}
```

## Cómo evitarlo

### JPA/Hibernate: `JOIN FETCH` o `@EntityGraph`

```java
// JOIN FETCH explícito en la query: 1 única query trae pedidos y sus clientes juntos
@Query("SELECT p FROM Pedido p JOIN FETCH p.cliente")
List<Pedido> findAllConCliente();
```

```java
// @EntityGraph declarativo, alternativa a escribir el JOIN FETCH a mano
@EntityGraph(attributePaths = {"cliente"})
List<Pedido> findAll();
```

### TypeORM: `relations` o `QueryBuilder` con `leftJoinAndSelect`

```typescript
const pedidos = await pedidoRepository.find({ relations: ['cliente'] }); // 1 query con JOIN
```

### GraphQL: `DataLoader` para batchear resoluciones de campos anidados

En GraphQL, el problema N+1 aparece de forma natural al resolver campos anidados de una lista (cada objeto de la lista dispara su propio resolver de campo relacionado). `DataLoader` agrupa (*batch*) todas las peticiones del mismo tick de ejecución en una única consulta:

```javascript
const clienteLoader = new DataLoader(async (clienteIds) => {
  const clientes = await clienteRepository.findByIds(clienteIds); // 1 query para TODOS los ids pedidos en este batch
  return clienteIds.map(id => clientes.find(c => c.id === id));
});

// El resolver de "cliente" en cada Pedido usa el loader en lugar de una query directa
const resolvers = {
  Pedido: {
    cliente: (pedido) => clienteLoader.load(pedido.clienteId),
  },
};
```

### SQL Server / SQL puro: cargar en dos pasos con `IN`

Cuando no se usa un ORM con soporte de `JOIN FETCH`, el patrón manual equivalente es: una query para los IDs padres, y una segunda query con `WHERE id IN (...)` para todos los relacionados de golpe, en lugar de una query por cada padre.

```sql
-- En vez de 1 query por pedido:
SELECT * FROM clientes WHERE id IN (SELECT DISTINCT cliente_id FROM pedidos WHERE ...);
```

## Cómo detectarlo en producción antes de que sea un incidente

- Activar logging de SQL en entornos de staging/desarrollo (nunca en producción por volumen y coste) y revisar el número de queries generadas por endpoint durante el desarrollo de cualquier feature que devuelva colecciones con relaciones.
- Usar herramientas de APM (Application Performance Monitoring) que cuentan el número de queries por petición HTTP y alertan sobre valores anómalos.
- Incluir en el checklist de revisión de PR: "¿esta feature que devuelve una lista con datos relacionados ha sido verificada contra N+1?".

## Checklist

- [ ] ¿Existe algún bucle sobre una colección que dispara una consulta a base de datos por cada elemento?
- [ ] ¿Las relaciones `LAZY` accedidas en un contexto de colección usan `JOIN FETCH`/`@EntityGraph` en lugar de cargarse una por una?
- [ ] ¿Los resolvers de GraphQL que resuelven campos relacionados de una lista usan `DataLoader` u otro mecanismo de batching?
- [ ] ¿Se ha verificado el número real de queries generadas por el endpoint con datos de volumen representativo, no solo con 2-3 registros de prueba?

Ver también: sección 4.4 y sección 6 de `CLAUDE.md` (Hibernate/JPA y Bases de Datos), [`../refactoring.md`](../knowledge/architecture/refactoring.md).
