# Spring AOP

> Ver [`../../patterns/decorator.md`](../../patterns/decorator.md) para el patrón de diseño que AOP implementa de forma declarativa mediante proxies, evitando escribir manualmente las clases decoradoras.

## Cómo funciona (proxies, no bytecode weaving en el caso estándar)

Spring AOP crea, en tiempo de ejecución, un **proxy** alrededor del bean anotado (JDK dynamic proxy si implementa una interfaz, CGLIB si es una clase concreta). El proxy intercepta las llamadas al método y ejecuta el *advice* (código transversal) antes/después/alrededor de la llamada real. Implicación práctica importante: **las llamadas internas dentro de la misma clase no pasan por el proxy** — si un método anotado con `@Cacheable` se llama desde otro método de la misma clase (`this.metodo()`), el aspecto no se aplica, porque esa llamada nunca atraviesa el proxy externo.

```java
@Service
public class PedidoService {

    @Cacheable("pedidos")
    public Pedido buscar(String id) { /* ... */ }

    public void otroMetodo() {
        this.buscar("p-1"); // NO pasa por el proxy — @Cacheable no se aplica aquí
    }
}
```

Solución: inyectar el propio bean (auto-inyección) o extraer el método anotado a otro bean, para que la llamada sí pase por el proxy externo.

## Aspecto personalizado con `@Around`

```java
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(Loggeable)")
    public Object logEjecucion(ProceedingJoinPoint joinPoint) throws Throwable {
        String metodo = joinPoint.getSignature().toShortString();
        long inicio = System.currentTimeMillis();

        try {
            Object resultado = joinPoint.proceed(); // ejecuta el método real
            log.info("event=metodo.ejecutado metodo={} duracion_ms={}",
                metodo, System.currentTimeMillis() - inicio);
            return resultado;
        } catch (Exception e) {
            log.error("event=metodo.fallido metodo={}", metodo, e);
            throw e;
        }
    }
}
```

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggeable {}
```

```java
@Loggeable
public void procesarPedido(Pedido pedido) { /* ... */ }
```

Este patrón (aspecto + anotación propia) es la forma idiomática de añadir comportamiento transversal reutilizable (logging, medición de tiempo, reintentos) declarativamente, sin envolver manualmente cada clase con un Decorator escrito a mano (ver [`../../patterns/decorator.md`](../../patterns/decorator.md)) — Spring genera el proxy automáticamente a partir de la anotación.

## Tipos de advice

| Advice | Cuándo se ejecuta |
|---|---|
| `@Before` | Antes del método, no puede evitar su ejecución |
| `@After` | Después del método, tanto si retorna normalmente como si lanza excepción |
| `@AfterReturning` | Después del método, solo si retorna normalmente |
| `@AfterThrowing` | Después del método, solo si lanza una excepción |
| `@Around` | Envuelve completamente la ejecución — el único que puede evitar/modificar la llamada real o su resultado |

## Cuándo usar AOP propio vs. cuándo NO

- **Usar** para lógica transversal genuina que se repetiría idénticamente en muchos métodos no relacionados entre sí (logging estructurado, medición, auditoría, reintentos).
- **No usar** para lógica de negocio específica de un caso de uso — eso vive explícitamente en el caso de uso mismo, no oculto detrás de un proxy que dificulta seguir el flujo real al leer el código (ver [`../../anti-patterns/spaghetti.md`](../../anti-patterns/spaghetti.md) sobre flujo de control implícito difícil de razonar).

## Checklist

- [ ] ¿Se ha verificado que no se depende de un aspecto aplicándose a una llamada interna (`this.metodo()`) dentro de la misma clase?
- [ ] ¿El aspecto personalizado resuelve lógica genuinamente transversal, no lógica de negocio específica de un caso de uso?
- [ ] ¿Se ha preferido `@Around` solo cuando realmente se necesita envolver/modificar la ejecución, usando advices más específicos (`@AfterThrowing`, etc.) cuando bastan?

Ver también: [`../../patterns/decorator.md`](../../patterns/decorator.md), [`spring-cache.md`](./spring-cache.md), [`../../anti-patterns/spaghetti.md`](../../anti-patterns/spaghetti.md).
