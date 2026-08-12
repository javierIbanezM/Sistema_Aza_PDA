# Estrategia de Testing

> Documento único de referencia sobre la pirámide de testing, tipos de test y buenas prácticas. Las herramientas específicas (JUnit/Mockito, Jest, Playwright, Espresso) se documentan en la Skill de cada tecnología, enlazando a este documento para la estrategia general.

## La pirámide de testing

```
        /\
       /E2E\          Pocos, lentos, frágiles, alta confianza end-to-end
      /------\
     /Integr. \       Moderados, verifican integración real con infraestructura
    /----------\
   /   Unit     \     Muchos, rápidos, aislados, verifican lógica de negocio
  /--------------\
```

La proporción no es una regla rígida, pero la intención es clara: la mayoría de la cobertura debe venir de tests rápidos y aislados (unitarios), con una capa intermedia de integración que verifica que las piezas encajan con infraestructura real, y una capa fina de E2E que verifica los flujos de negocio críticos de principio a fin.

### Por qué esta forma y no la inversa

Un test E2E es lento (segundos/minutos), frágil (falla por motivos ajenos al código, como timing o datos de entorno) y difícil de depurar (un fallo no indica dónde está el problema). Un test unitario es rápido (milisegundos), estable y señala el problema con precisión. Invertir la pirámide ("cono de helado": muchos E2E, pocos unitarios) produce suites de test lentas, inestables (*flaky*) y costosas de mantener.

## Tipos de test

### Unit Tests

Verifican una unidad de comportamiento (una función, un caso de uso, un método de una entidad de dominio) de forma aislada, sin I/O real (sin red, sin base de datos, sin sistema de archivos).

```typescript
describe('CrearPedidoUseCase', () => {
  it('debe lanzar error si se confirma un pedido sin líneas', async () => {
    const repositorioFalso: PedidoRepository = { guardar: jest.fn(), buscarPorId: jest.fn() };
    const useCase = new CrearPedidoUseCase(repositorioFalso);

    await expect(useCase.ejecutar('pedido-1', [])).rejects.toThrow(
      'Un pedido no puede confirmarse sin líneas'
    );
    expect(repositorioFalso.guardar).not.toHaveBeenCalled();
  });
});
```

Las dependencias externas (repositorios, clientes HTTP) se sustituyen por dobles de test. Ver la distinción entre tipos de doble más abajo.

### Integration Tests

Verifican que un componente funciona correctamente contra una dependencia de infraestructura **real** (o una réplica fiel de ella), típicamente usando contenedores efímeros (Testcontainers en Java, contenedores Docker levantados en el pipeline de CI para Node).

```java
@Testcontainers
class PedidoRepositoryJpaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Test
    void debeGuardarYRecuperarUnPedido() {
        PedidoRepositoryJpa repository = new PedidoRepositoryJpa(entityManager);
        Pedido pedido = Pedido.crear(new PedidoId("p-1"));

        repository.guardar(pedido);
        Optional<Pedido> recuperado = repository.buscarPorId(new PedidoId("p-1"));

        assertThat(recuperado).isPresent();
    }
}
```

Los integration tests son la única forma fiable de verificar mapeos ORM, queries SQL reales y comportamiento de transacciones — un mock de repositorio nunca detecta un error de mapeo JPA.

### Contract Tests

Verifican que el contrato entre dos servicios (típicamente un consumidor y un proveedor en una arquitectura de microservicios, ver `docs` de la Skill de microservicios) se mantiene, sin necesidad de desplegar ambos servicios juntos. Herramienta de referencia: Pact.

- El consumidor define las expectativas sobre las respuestas del proveedor.
- El proveedor verifica, en su propio pipeline, que sigue cumpliendo esas expectativas.
- Evita el escenario "ambos equipos pasan sus tests por separado pero la integración real falla".

### E2E Tests

Verifican un flujo de negocio completo a través de la interfaz real del sistema (navegador, app móvil), tocando el sistema desplegado o un entorno muy similar a producción.

```typescript
test('un usuario puede completar una compra de principio a fin', async ({ page }) => {
  await page.goto('/productos/zapatillas-running');
  await page.click('text=Añadir al carrito');
  await page.click('text=Ir a pagar');
  await page.fill('#numero-tarjeta', '4242424242424242');
  await page.click('text=Confirmar pago');

  await expect(page.locator('text=Pedido confirmado')).toBeVisible();
});
```

Reservar los E2E para los flujos verdaderamente críticos de negocio (checkout, login, alta de usuario), no para cada variante de cada formulario — eso corresponde a unit/integration tests de los componentes individuales.

## Dobles de test: Mock vs Stub vs Fake vs Spy

| Tipo | Qué hace | Cuándo usarlo |
|---|---|---|
| **Dummy** | Objeto pasado solo para satisfacer una firma, nunca usado realmente | Parámetros requeridos irrelevantes para el test |
| **Stub** | Devuelve respuestas predefinidas a las llamadas que recibe | Cuando solo importa el valor de retorno, no la interacción |
| **Fake** | Implementación funcional simplificada (ej. un repositorio en memoria) | Cuando se necesita comportamiento real pero sin infraestructura pesada |
| **Mock** | Verifica que se realizaron llamadas concretas con argumentos concretos | Cuando el comportamiento a verificar ES la interacción (ej. "se envió el email") |
| **Spy** | Envuelve un objeto real y registra las llamadas realizadas sobre él | Cuando se necesita comportamiento real y verificar interacción a la vez |

Sobre-usar mocks (verificar interacciones internas en vez de comportamiento observable) acopla los tests a la implementación y los hace frágiles ante refactors legítimos. Preferir Fakes o Stubs cuando el test solo necesita un resultado, reservando Mocks para verificar efectos secundarios explícitos que son el propósito real del test (ej. "se llamó al servicio de notificación exactamente una vez").

## Nomenclatura de tests

Formato recomendado, consistente en todo el proyecto:

```
should_<resultado_esperado>_when_<condición>
```

o el equivalente `given/when/then` en frameworks que lo soportan de forma nativa (Kotest, Jest con `describe`/`it` anidados):

```typescript
describe('CalculadoraDescuento', () => {
  describe('cuando el cliente es VIP', () => {
    it('aplica un 15% de descuento', () => { /* ... */ });
  });
  describe('cuando el pedido está vacío', () => {
    it('lanza un error de validación', () => { /* ... */ });
  });
});
```

Un nombre de test debe permitir entender qué falló **sin leer el cuerpo del test**, solo leyendo el resultado del test runner.

## Qué testear y qué no

- **Testear**: comportamiento observable público (entradas → salidas/efectos), reglas de negocio, casos límite, casos de error.
- **No testear**: detalles de implementación privados (no testear que un método privado se llama internamente; eso es un test frágil que se rompe con cualquier refactor válido).
- **No testear**: getters/setters triviales sin lógica.
- **No testear** exhaustivamente configuración de frameworks (confiar en que Spring/NestJS/Room funcionan; testear la configuración propia solo si tiene lógica condicional relevante).

## Determinismo

Un test debe producir el mismo resultado siempre que se ejecute, independientemente del orden, del entorno o del momento del día.

- Nunca usar `Thread.sleep()`/`setTimeout` arbitrarios para "esperar" una operación asíncrona; usar mecanismos de espera activa con timeout (`waitFor`, `await`) o control explícito del scheduler.
- Fijar la fuente de tiempo (`Clock` inyectado en Java, `jest.useFakeTimers()` en Node) en vez de depender de `new Date()`/`Instant.now()` reales dentro de la lógica bajo test.
- Aislar el estado entre tests: cada test debe poder ejecutarse solo o en cualquier orden sin depender de efectos secundarios de otro test.

## Checklist

- [ ] ¿Todo caso de uso nuevo tiene al menos un test de camino feliz, uno de validación fallida y uno de caso límite?
- [ ] ¿Los tests unitarios no dependen de I/O real (red, base de datos, filesystem)?
- [ ] ¿Los tests de integración usan infraestructura real o una réplica fiel (Testcontainers), no mocks de la capa de persistencia?
- [ ] ¿El nombre de cada test describe el resultado esperado y la condición, sin necesidad de leer el cuerpo?
- [ ] ¿Los tests son deterministas y pueden ejecutarse en cualquier orden?
- [ ] ¿Se testea comportamiento observable, no detalles de implementación privados?

Ver también: [`refactoring.md`](./refactoring.md), [`debugging.md`](./debugging.md).
