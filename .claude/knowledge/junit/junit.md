# JUnit 5

> Ver [`../architecture/testing.md`](../architecture/testing.md) para la estrategia general de testing (pirámide, tipos de test, nomenclatura) que este documento aplica concretamente con JUnit 5. Ver [`mockito.md`](../mockito/mockito.md) para el uso combinado con dobles de test.

## Anatomía básica

```java
class CalculadoraDescuentoTest {

    private final CalculadoraDescuento calculadora = new CalculadoraDescuento();

    @Test
    void deberiaAplicarDescuentoDelCincoPorcientoParaClienteRegular() {
        Pedido pedido = new PedidoTestBuilder().conTotal(100).build();

        BigDecimal resultado = calculadora.calcular(pedido, TipoCliente.REGULAR);

        assertThat(resultado).isEqualByComparingTo("95.00");
    }
}
```

Nomenclatura de métodos de test siguiendo [`../architecture/testing.md`](../architecture/testing.md): `deberia<ResultadoEsperado>Cuando<Condicion>` o el equivalente en inglés `should<...>When<...>` según la convención del proyecto.

## `@Nested` para agrupar escenarios (equivalente a `describe` anidado)

```java
class ConfirmarPedidoUseCaseTest {

    @Nested
    class CuandoElPedidoTieneLineas {
        @Test
        void deberiaConfirmarCorrectamente() { /* ... */ }
    }

    @Nested
    class CuandoElPedidoNoTieneLineas {
        @Test
        void deberiaLanzarExcepcion() { /* ... */ }
    }
}
```

Agrupar por escenario/condición (no solo por método bajo test) hace que el reporte de fallos del test runner comunique directamente qué condición de negocio falló, sin leer el cuerpo del test.

## `@ParameterizedTest` para cubrir múltiples casos sin duplicar el test

```java
@ParameterizedTest
@CsvSource({
    "REGULAR, 95.00",
    "VIP, 85.00",
    "EMPLEADO, 70.00"
})
void deberiaAplicarElDescuentoCorrectoPorTipoDeCliente(TipoCliente tipo, BigDecimal esperado) {
    Pedido pedido = new PedidoTestBuilder().conTotal(100).build();

    BigDecimal resultado = calculadora.calcular(pedido, tipo);

    assertThat(resultado).isEqualByComparingTo(esperado);
}
```

Evita tres métodos de test casi idénticos que solo difieren en el dato de entrada — aplica DRY (ver [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)) al propio código de test.

## `@BeforeEach`/`@AfterEach` para setup compartido

```java
class PedidoRepositoryTest {

    private PedidoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PedidoRepositoryEnMemoria(); // instancia limpia en cada test, sin estado compartido
    }
}
```

Cada test debe empezar con estado limpio — nunca compartir una instancia mutable entre tests sin reiniciarla, para garantizar que los tests son deterministas y ejecutables en cualquier orden (ver [`../architecture/testing.md`](../architecture/testing.md)).

## Fijar el reloj para tests deterministas

```java
@Test
void deberiaMarcarComoVencidoTrasElPlazo() {
    Clock clockFijo = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC);
    ServicioVencimiento servicio = new ServicioVencimiento(clockFijo); // reloj inyectado, no Instant.now() directo

    boolean vencido = servicio.estaVencido(pedidoCreadoHace31Dias);

    assertThat(vencido).isTrue();
}
```

Nunca depender de `Instant.now()`/`LocalDate.now()` directamente dentro de la lógica bajo test — inyectar un `Clock` (o equivalente) permite fijar el tiempo en el test, evitando tests no deterministas (ver [`../architecture/testing.md`](../architecture/testing.md)).

## AssertJ para aserciones fluidas y legibles

```java
assertThat(pedido.getLineas())
    .hasSize(2)
    .extracting(LineaPedido::getProductoId)
    .containsExactly("p-1", "p-2");

assertThatThrownBy(() -> pedido.confirmar())
    .isInstanceOf(PedidoSinLineasException.class)
    .hasMessage("Un pedido no puede confirmarse sin líneas");
```

AssertJ (no parte de JUnit, pero de uso estándar junto a él) produce mensajes de fallo más informativos que las aserciones básicas de JUnT, y permite encadenar múltiples verificaciones sobre el mismo resultado de forma legible.

## Testcontainers para integration tests

```java
@Testcontainers
class PedidoRepositoryJpaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("pedidos_test");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Test
    void deberiaGuardarYRecuperarUnPedido() { /* ... */ }
}
```

Ver [`../architecture/testing.md`](../architecture/testing.md) para el razonamiento de por qué los integration tests deben usar infraestructura real (o una réplica fiel vía Testcontainers) en lugar de mocks de la capa de persistencia.

## Checklist

- [ ] ¿Cada test tiene un nombre que comunica el resultado esperado y la condición, sin leer el cuerpo?
- [ ] ¿Se usa `@ParameterizedTest` en lugar de duplicar tests casi idénticos con distinto dato de entrada?
- [ ] ¿Cada test parte de estado limpio (`@BeforeEach`), sin depender de la ejecución de otro test?
- [ ] ¿El tiempo está fijado explícitamente (`Clock` inyectado) en tests que dependen de fechas/horas?
- [ ] ¿Los integration tests usan Testcontainers (infraestructura real), no mocks del repositorio?

Ver también: [`mockito.md`](../mockito/mockito.md), [`../architecture/testing.md`](../architecture/testing.md).
