# Spring Core

> Ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md) para el razonamiento de capas que este documento aplica concretamente a Spring. Ver [`spring-boot.md`](./spring-boot.md) para auto-configuración y arranque de aplicación, [`spring-web.md`](./spring-web.md) para controllers y manejo de errores HTTP.

## Inyección de dependencias

**Siempre por constructor**, nunca por campo (`@Autowired` en un field está prohibido salvo en tests):

```java
@Service
public class CrearPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final NotificadorEmail notificadorEmail;

    // Constructor único → Spring inyecta automáticamente sin necesidad de @Autowired explícito
    public CrearPedidoUseCase(PedidoRepository pedidoRepository, NotificadorEmail notificadorEmail) {
        this.pedidoRepository = pedidoRepository;
        this.notificadorEmail = notificadorEmail;
    }

    public void ejecutar(CrearPedidoCommand command) {
        Pedido pedido = Pedido.crear(command.clienteId());
        command.lineas().forEach(pedido::agregarLinea);
        pedido.confirmar();
        pedidoRepository.guardar(pedido);
        notificadorEmail.notificar(pedido);
    }
}
```

La inyección por constructor hace las dependencias explícitas, permite marcarlas `final` (inmutabilidad), y facilita el testing sin necesidad de un contenedor Spring (`new CrearPedidoUseCase(fakeRepo, fakeNotificador)`).

## Estereotipos y su responsabilidad

| Anotación | Capa (ver [`clean-architecture.md`](../architecture/clean-architecture.md)) | Responsabilidad |
|---|---|---|
| `@RestController` | Interface/Presentation | Traduce HTTP ↔ casos de uso, nunca contiene lógica de negocio (ver [`spring-web.md`](./spring-web.md)) |
| `@Service` | Application | Casos de uso / orquestación |
| `@Repository` | Infrastructure | Implementación concreta de un puerto de persistencia (ver [`spring-data-jpa.md`](./spring-data-jpa.md)) |
| `@Component` | Infrastructure (genérico) | Utilidades que no encajan en las categorías anteriores |
| `@Configuration` | Composición | Ensamblado de beans, punto único de instanciación de implementaciones concretas |

Las clases de `domain/` (entidades, value objects) **no llevan anotaciones de Spring** — el dominio no depende del framework (ver [`clean-architecture.md`](../architecture/clean-architecture.md)).

## Configuración externa

`application.yml` + `@ConfigurationProperties`, nunca `@Value` disperso para configuración compleja:

```yaml
# application.yml
app:
  pagos:
    proveedor: stripe
    timeout-ms: 3000
    reintentos-maximos: 3
```

```java
@ConfigurationProperties(prefix = "app.pagos")
public record PagosProperties(String proveedor, int timeoutMs, int reintentosMaximos) {}
```

```java
@Configuration
@EnableConfigurationProperties(PagosProperties.class)
public class PagosConfig {

    @Bean
    public ProcesadorPagos procesadorPagos(PagosProperties props, StripeSdkClient client) {
        return new StripeProcesadorPagosAdapter(client); // ver ../../patterns/adapter.md
    }
}
```

## Perfiles (`dev`, `test`, `prod`)

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pedidos_dev

# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}  # inyectado por el entorno, nunca hardcodeado
```

Regla: la lógica de negocio **nunca** se condiciona por `@Profile` — los perfiles configuran infraestructura (qué base de datos, qué proveedor externo mockeado), no reglas de negocio distintas por entorno.

## Ciclo de vida de un bean

```java
@Component
public class ConexionExternaManager {

    @PostConstruct
    void inicializar() { /* abrir conexión, precalentar caché */ }

    @PreDestroy
    void limpiar() { /* cerrar conexión limpiamente al apagar la aplicación */ }
}
```

Usar `@PostConstruct`/`@PreDestroy` con moderación — solo para inicialización/limpieza que realmente depende del ciclo de vida del contenedor Spring, no como sustituto del constructor para lógica que podría vivir ahí.

## Checklist rápido

- [ ] ¿Toda inyección de dependencias es por constructor?
- [ ] ¿Las clases de `domain/` están libres de anotaciones de Spring?
- [ ] ¿La configuración compleja usa `@ConfigurationProperties`, no `@Value` disperso?
- [ ] ¿La lógica de negocio evita condicionarse por `@Profile`?

Ver también: [`spring-boot.md`](./spring-boot.md), [`spring-web.md`](./spring-web.md), [`spring-data-jpa.md`](./spring-data-jpa.md), [`spring-security.md`](./spring-security.md), [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md), `CLAUDE.md` sección 4.2.
