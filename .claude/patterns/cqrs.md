# Patrón: CQRS (Command Query Responsibility Segregation)

## Descripción

CQRS separa explícitamente las operaciones que **modifican** estado (Commands) de las que **leen** estado (Queries), usando modelos (y, opcionalmente, almacenamiento) distintos para cada una. En su forma más simple, es solo una separación de clases/métodos dentro del mismo servicio y base de datos. En su forma más avanzada, implica bases de datos de lectura y escritura físicamente separadas, sincronizadas de forma asíncrona (a menudo combinado con Event Sourcing).

## Cuándo usarlo

- Cuando los requisitos de lectura y escritura son muy distintos en volumen, forma o escalabilidad (ej. un sistema con pocas escrituras complejas pero millones de lecturas de un modelo desnormalizado optimizado para dashboards).
- Cuando el modelo de dominio rico necesario para validar escrituras (con invariantes, Agregados — ver [`../ddd.md`](../knowledge/architecture/ddd.md)) no es el modelo óptimo para servir lecturas (que suelen necesitar datos desnormalizados, agregados de varias fuentes, proyecciones específicas por pantalla).
- En sistemas con Event Sourcing, donde el modelo de escritura es el log de eventos y los modelos de lectura son proyecciones materializadas a partir de ese log.

## Cuándo NO usarlo

- En un CRUD estándar donde lecturas y escrituras comparten naturalmente el mismo modelo sin fricción — introducir CQRS aquí es sobre-ingeniería que viola YAGNI (ver [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).
- Cuando el equipo no tiene experiencia previa gestionando la complejidad de consistencia eventual entre el modelo de escritura y el de lectura — el coste operativo y cognitivo es alto y debe justificarse con un problema real medido, no anticipado.

## Ventajas

- Permite optimizar independientemente el modelo de escritura (consistencia fuerte, validación de invariantes) y el de lectura (desnormalizado, rápido, adaptado a cada vista).
- Escala lecturas y escrituras de forma independiente (réplicas de lectura, cachés, bases de datos distintas por cada lado).
- Simplifica los modelos de lectura: en lugar de un ORM complejo con joins costosos, las proyecciones de lectura pueden ser tablas planas ya preparadas para la consulta exacta que necesita cada pantalla.

## Desventajas

- Introduce **consistencia eventual** entre el modelo de escritura y el de lectura: tras un `Command`, una `Query` inmediatamente posterior puede no reflejar aún el cambio, lo que exige diseñar la UI/UX y las expectativas del negocio para tolerarlo.
- Duplica la complejidad de infraestructura: dos modelos (o dos bases de datos) que mantener, sincronizar y monitorizar.
- Aumenta la curva de aprendizaje del equipo y el coste de depuración (rastrear un dato desde el Command que lo originó hasta la proyección de lectura final requiere entender todo el pipeline de sincronización).

## Ejemplo — CQRS simple (mismo almacenamiento, separación solo de responsabilidades)

```typescript
// Lado de escritura (Command): pasa por el dominio completo, valida invariantes
class ConfirmarPedidoCommand {
  constructor(public readonly pedidoId: string) {}
}

class ConfirmarPedidoCommandHandler {
  constructor(private readonly pedidoRepository: PedidoRepository) {}

  async ejecutar(command: ConfirmarPedidoCommand): Promise<void> {
    const pedido = await this.pedidoRepository.buscarPorId(command.pedidoId);
    if (!pedido) throw new Error('Pedido no encontrado');
    pedido.confirmar(); // invariantes de dominio aplicadas aquí
    await this.pedidoRepository.guardar(pedido);
  }
}

// Lado de lectura (Query): bypassa el modelo de dominio, lee directamente una proyección optimizada
class ObtenerResumenPedidosQuery {
  constructor(private readonly db: DatabaseClient) {}

  async ejecutar(clienteId: string): Promise<ResumenPedidoDto[]> {
    // Query SQL directa a una vista/tabla desnormalizada, sin pasar por entidades de dominio
    return this.db.query(
      `SELECT id, total, estado, fecha FROM vista_resumen_pedidos WHERE cliente_id = $1`,
      [clienteId]
    );
  }
}
```

Incluso en esta forma simple (sin bases de datos separadas), el beneficio es real: la Query no carga ni reconstruye Agregados completos con todas sus invariantes solo para leer datos, lo cual sería un coste innecesario para una operación de solo lectura.

## Errores comunes

- Introducir bases de datos de lectura y escritura físicamente separadas sin un problema de escalabilidad real medido, asumiendo el coste operativo de sincronización sin necesidad.
- No comunicar al negocio/UX la consistencia eventual introducida, generando reportes de "bug" cuando en realidad es el comportamiento esperado del sistema (un dato tarda un instante en propagarse del lado de escritura al de lectura).
- Duplicar reglas de validación entre el lado de Command y el lado de Query cuando la Query nunca debería validar nada — las Queries son de solo lectura y no deben mutar estado ni aplicar reglas de negocio.

Ver también: [`../ddd.md`](../knowledge/architecture/ddd.md), [`Observer.md`](./observer.md), sección 17 de `CLAUDE.md` (Microservicios).
