# Domain-Driven Design (DDD)

> Documento único de referencia sobre DDD estratégico y táctico. Los patrones tácticos concretos (Repository, etc.) tienen su propio archivo en `patterns/` y se enlazan desde aquí en lugar de reexplicarse.

## Introducción

DDD (Eric Evans, 2003) es un enfoque para diseñar software complejo centrado en modelar el dominio de negocio de forma explícita en el código, en lugar de dejar que la lógica de negocio se disuelva en controladores, scripts SQL o servicios anémicos. Se divide en dos partes: **DDD estratégico** (cómo dividir un sistema grande en partes manejables) y **DDD táctico** (patrones concretos para modelar cada parte).

DDD no es gratuito: introduce una curva de aprendizaje y overhead de modelado. Se justifica en dominios con lógica de negocio genuinamente compleja, no en CRUDs simples (ver [`dry-kiss-yagni.md`](./dry-kiss-yagni.md) — YAGNI).

---

## DDD Estratégico

### Bounded Context (Contexto Delimitado)

Un Bounded Context es una frontera explícita (normalmente un servicio, módulo o subsistema) dentro de la cual un modelo de dominio concreto es válido y consistente. El mismo término de negocio puede significar cosas distintas en contextos distintos:

- En el contexto de **Ventas**, un `Cliente` tiene historial de compras, preferencias y un límite de crédito.
- En el contexto de **Soporte**, ese mismo `Cliente` tiene tickets abiertos, SLA y un historial de conversaciones.

Modelar un único `Cliente` "universal" que sirva a ambos contextos produce una clase con demasiadas responsabilidades y acoplamiento entre equipos que deberían poder evolucionar de forma independiente. Cada Bounded Context tiene su propio modelo, aunque compartan el mismo identificador de negocio.

### Lenguaje Ubicuo (Ubiquitous Language)

El vocabulario usado en el código (nombres de clases, métodos, eventos) debe ser **exactamente** el que usa el negocio dentro de ese Bounded Context, sin traducciones libres. Si el negocio dice "Liquidar una comisión", el código dice `liquidarComision()`, no `processCommissionData()`. Esto:

- Reduce la fricción de traducción entre negocio y desarrollo.
- Hace que el código sirva como documentación viva del dominio.
- Facilita detectar cuándo un mismo término tiene significados distintos en distintos contextos (señal de que hace falta separar Bounded Contexts).

### Context Mapping

Cuando existen varios Bounded Contexts que necesitan comunicarse, el *Context Map* documenta la relación entre ellos. Patrones de relación más comunes:

- **Shared Kernel**: dos contextos comparten un subconjunto de modelo (usar con mucha cautela, genera acoplamiento fuerte).
- **Customer/Supplier**: un contexto (proveedor) provee datos/funcionalidad a otro (cliente), con negociación de contrato entre equipos.
- **Conformist**: el contexto cliente acepta el modelo del proveedor tal cual, sin traducirlo (típico al integrar con un sistema externo que no se puede negociar).
- **Anti-Corruption Layer (ACL)**: el contexto cliente traduce explícitamente el modelo externo a su propio lenguaje ubicuo mediante una capa de adaptación, evitando que conceptos ajenos "contaminen" el dominio propio. Ver ejemplo abajo.

#### Ejemplo de Anti-Corruption Layer

```typescript
// Modelo del sistema legado externo (fuera de nuestro control)
interface LegacyCustomerRecord {
  cust_id: string;
  cust_nm: string;
  cust_stat_cd: '0' | '1' | '2'; // 0=inactivo, 1=activo, 2=suspendido
}

// Modelo de nuestro dominio, con lenguaje ubicuo propio
class Cliente {
  constructor(
    public readonly id: string,
    public readonly nombre: string,
    public readonly estado: EstadoCliente
  ) {}
}

enum EstadoCliente { INACTIVO, ACTIVO, SUSPENDIDO }

// Anti-Corruption Layer: traduce el modelo externo al modelo propio
class LegacyCustomerAdapter {
  static aCliente(record: LegacyCustomerRecord): Cliente {
    const estadoMap: Record<string, EstadoCliente> = {
      '0': EstadoCliente.INACTIVO,
      '1': EstadoCliente.ACTIVO,
      '2': EstadoCliente.SUSPENDIDO,
    };
    return new Cliente(record.cust_id, record.cust_nm, estadoMap[record.cust_stat_cd]);
  }
}
```

El resto del dominio nunca ve `LegacyCustomerRecord` ni sus códigos crípticos; solo interactúa con `Cliente` y `EstadoCliente`, expresados en el lenguaje ubicuo propio.

---

## DDD Táctico

### Entidad (Entity)

Objeto definido por su **identidad**, no por sus atributos. Dos entidades con los mismos atributos pero distinto identificador son objetos distintos; una entidad con el mismo identificador sigue siendo "la misma" aunque cambien sus atributos a lo largo del tiempo.

```java
public class Pedido {
    private final PedidoId id; // identidad
    private EstadoPedido estado;
    private List<LineaPedido> lineas;

    public boolean equals(Object o) {
        if (!(o instanceof Pedido)) return false;
        return this.id.equals(((Pedido) o).id); // igualdad por identidad, no por atributos
    }
}
```

### Value Object (Objeto de Valor)

Objeto definido por sus **atributos**, no por identidad. Es inmutable; dos Value Objects con los mismos valores son intercambiables y se comparan por valor, no por referencia.

```java
public final class Dinero {
    private final BigDecimal monto;
    private final Moneda moneda;

    public Dinero(BigDecimal monto, Moneda moneda) {
        if (monto.signum() < 0) throw new IllegalArgumentException("El monto no puede ser negativo");
        this.monto = monto;
        this.moneda = moneda;
    }

    public Dinero sumar(Dinero otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("No se pueden sumar montos de monedas distintas");
        }
        return new Dinero(this.monto.add(otro.monto), this.moneda); // devuelve una nueva instancia, no muta
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Dinero)) return false;
        Dinero d = (Dinero) o;
        return monto.equals(d.monto) && moneda.equals(d.moneda); // igualdad por valor
    }
}
```

Los Value Objects encapsulan invariantes (aquí: el monto nunca es negativo, no se pueden sumar monedas distintas) que de otro modo tendrían que validarse dispersas por todo el código que manipula un `BigDecimal` desnudo. Este es uno de los mayores beneficios prácticos de DDD táctico: eliminar "primitivos con significado implícito" (`BigDecimal`, `String` usados para representar dinero, email, NIF...) sustituyéndolos por tipos que se autovalidan.

### Agregado (Aggregate) y Raíz de Agregado (Aggregate Root)

Un Agregado es un clúster de Entidades y Value Objects tratado como una unidad de consistencia transaccional. Tiene una única **Raíz de Agregado**, que es la única Entidad accesible directamente desde fuera del agregado; el resto de objetos internos solo se acceden a través de ella.

```java
public class Pedido { // Raíz de Agregado
    private final PedidoId id;
    private final List<LineaPedido> lineas = new ArrayList<>();
    private EstadoPedido estado;

    public void agregarLinea(Producto producto, int cantidad) {
        if (estado != EstadoPedido.BORRADOR) {
            throw new IllegalStateException("No se pueden agregar líneas a un pedido confirmado");
        }
        lineas.add(new LineaPedido(producto, cantidad)); // invariante controlada por la raíz
    }

    public void confirmar() {
        if (lineas.isEmpty()) {
            throw new IllegalStateException("Un pedido no puede confirmarse sin líneas");
        }
        this.estado = EstadoPedido.CONFIRMADO;
    }

    // No hay setter público de `lineas`: se modifica únicamente vía métodos de la raíz
}

class LineaPedido { // Entidad interna del agregado, no accesible directamente desde fuera
    // ...
}
```

**Reglas de diseño de agregados:**

1. El agregado es la unidad de transacción: una operación de escritura modifica, como máximo, un agregado completo.
2. Referencias entre agregados distintos se hacen por **identificador**, nunca por referencia de objeto directa (evita que un cambio en un agregado modifique implícitamente otro).
3. Agregados pequeños por defecto; agregados grandes generan contención de concurrencia y cargan datos innecesarios en memoria.

### Repositorio (Repository)

Abstracción que da la ilusión de una colección en memoria de Agregados, ocultando los detalles de persistencia. Un Repositorio existe **por Agregado**, no por tabla. Ver [`patterns/Repository.md`](../../patterns/repository.md) para la implementación detallada del patrón.

### Domain Service

Cuando una operación de negocio no pertenece naturalmente a ninguna Entidad ni Value Object concreto (por ejemplo, porque involucra a varios agregados), se modela como un Domain Service: una operación de dominio sin estado propio.

```java
public class ServicioDeTransferencia {
    public void transferir(CuentaBancaria origen, CuentaBancaria destino, Dinero monto) {
        origen.retirar(monto); // invariantes propias de CuentaBancaria
        destino.depositar(monto);
    }
}
```

No confundir con un Application Service (caso de uso de la capa de aplicación, ver [`clean-architecture.md`](./clean-architecture.md)): el Domain Service contiene lógica de **dominio** pura, sin orquestar transacciones, seguridad ni infraestructura.

### Domain Event

Representa algo relevante que ha ocurrido en el dominio, expresado en pasado, que otros agregados o Bounded Contexts pueden necesitar conocer.

```java
public final class PedidoConfirmadoEvent {
    public final PedidoId pedidoId;
    public final Instant ocurridoEn;

    public PedidoConfirmadoEvent(PedidoId pedidoId, Instant ocurridoEn) {
        this.pedidoId = pedidoId;
        this.ocurridoEn = ocurridoEn;
    }
}
```

Los Domain Events se publican típicamente tras confirmar la transacción del agregado emisor, y se consumen de forma asíncrona por otros contextos (ver `kafka.md` / `rabbitmq.md` en la Skill de mensajería correspondiente para el mecanismo de transporte).

---

## Errores comunes al aplicar DDD

1. **Modelo Anémico**: entidades que son solo bolsas de getters/setters sin comportamiento, con toda la lógica en "servicios" externos. Esto no es DDD, es programación procedural con nombres de clase de dominio. Ver [`../anti-patterns/anemic-domain-model.md`](../../anti-patterns/anemic-domain-model.md).
2. **Un único Bounded Context gigante**: intentar modelar toda la empresa en un solo modelo produce el "modelo de dios", con las mismas patologías que un [God Object](../../anti-patterns/god-object.md) a nivel de sistema.
3. **Agregados demasiado grandes**: cargar y bloquear más datos de los necesarios en cada transacción, generando problemas de concurrencia y rendimiento.
4. **Aplicar DDD táctico completo a un CRUD simple**: si el dominio no tiene reglas de negocio complejas, DDD añade complejidad sin beneficio (ver YAGNI en [`dry-kiss-yagni.md`](./dry-kiss-yagni.md)).

## Checklist de modelado DDD

- [ ] ¿Se han identificado los Bounded Contexts del sistema y sus fronteras?
- [ ] ¿El lenguaje usado en el código coincide con el lenguaje del negocio en ese contexto?
- [ ] ¿Los "primitivos con significado" (dinero, email, NIF, rangos de fechas) están modelados como Value Objects, no como `String`/`BigDecimal` desnudos?
- [ ] ¿Cada Agregado tiene una única Raíz que controla todas sus invariantes?
- [ ] ¿Las referencias entre Agregados distintos son por identificador, no por objeto?
- [ ] ¿Existe un Anti-Corruption Layer al integrar con sistemas externos o legados?

Ver también: [`clean-architecture.md`](./clean-architecture.md), [`patterns/Repository.md`](../../patterns/repository.md), [`../anti-patterns/anemic-domain-model.md`](../../anti-patterns/anemic-domain-model.md).
