# Mockito

> Ver [`../architecture/testing.md`](../architecture/testing.md) para la distinción conceptual entre Mock/Stub/Fake/Spy y cuándo usar cada uno — este documento cubre la sintaxis concreta de Mockito para implementarlos.

## Configuración base con JUnit 5

```java
@ExtendWith(MockitoExtension.class) // JUnit 5, no MockitoJUnitRunner (JUnit 4)
class CrearPedidoUseCaseTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private NotificadorEmail notificadorEmail;

    @InjectMocks
    private CrearPedidoUseCase useCase; // Mockito inyecta los @Mock anteriores por constructor automáticamente

    @Test
    void deberiaGuardarElPedidoYNotificarAlConfirmar() {
        // ...
    }
}
```

## `when/thenReturn` para stubbing de comportamiento

```java
@Test
void deberiaLanzarExcepcionSiElPedidoNoExiste() {
    when(pedidoRepository.buscarPorId("p-999")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.confirmar("p-999"))
        .isInstanceOf(PedidoNoEncontradoException.class);

    verify(notificadorEmail, never()).notificar(any()); // efecto secundario NO debe ocurrir
}
```

## `verify` para comprobar interacciones (usar con criterio)

```java
@Test
void deberiaNotificarAlClienteTrasConfirmarElPedido() {
    Pedido pedido = new PedidoTestBuilder().build();
    when(pedidoRepository.buscarPorId("p-1")).thenReturn(Optional.of(pedido));

    useCase.confirmar("p-1");

    verify(notificadorEmail, times(1)).notificar(pedido); // el propósito del test ES verificar esta interacción
    verify(pedidoRepository).guardar(pedido);
}
```

Reservar `verify()` para los casos donde el **propósito explícito del test** es comprobar que ocurrió un efecto secundario (se envió un email, se guardó el pedido) — no usarlo para verificar detalles de implementación irrelevantes al comportamiento observable, lo que acopla el test a la implementación (ver [`../architecture/testing.md`](../architecture/testing.md)).

## `ArgumentCaptor` para inspeccionar el argumento pasado a un mock

```java
@Test
void deberiaGuardarElPedidoConEstadoConfirmado() {
    ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);

    useCase.confirmar("p-1");

    verify(pedidoRepository).guardar(captor.capture());
    assertThat(captor.getValue().getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
}
```

Útil cuando lo relevante no es solo *que* se llamó al mock, sino *con qué estado exacto* del objeto.

## Mockear excepciones

```java
@Test
void deberiaPropagar429SiElServicioDePagosEstaSaturado() {
    when(procesadorPagos.cobrar(any())).thenThrow(new ServicioPagosSaturadoException());

    assertThatThrownBy(() -> useCase.confirmar("p-1"))
        .isInstanceOf(ServicioPagosSaturadoException.class);
}
```

## `@Spy` cuando se necesita comportamiento real parcial

```java
@Test
void deberiaUsarElComportamientoRealSalvoElMetodoEspiado() {
    ServicioDescuento spy = spy(new ServicioDescuentoImpl());
    doReturn(BigDecimal.ZERO).when(spy).calcularDescuentoPromocional(any()); // solo este método se sobreescribe

    // el resto de métodos de ServicioDescuentoImpl ejecutan su implementación real
}
```

Usar `@Spy` con moderación — si se necesita sobreescribir varios métodos del mismo objeto, probablemente el diseño se beneficiaría de extraer esos métodos a una dependencia inyectable y mockeable en lugar de espiar la clase bajo test.

## Errores comunes con Mockito

- **Mockear la clase bajo test en lugar de sus dependencias**: si `CrearPedidoUseCaseTest` mockea `CrearPedidoUseCase`, el test no verifica nada real — se mockean las dependencias inyectadas (`PedidoRepository`, `NotificadorEmail`), nunca la clase que se está probando.
- **Sobre-especificar interacciones irrelevantes**: `verify()` sobre cada llamada interna acopla el test a la implementación exacta, rompiendo el test ante cualquier refactor válido que no cambia el comportamiento observable.
- **`@Mock` en lugar de un Fake cuando el test necesita comportamiento real simple**: si el repositorio en memoria (Fake, ver [`../architecture/testing.md`](../architecture/testing.md)) comunica mejor la intención del test que un `when/thenReturn` extenso, preferir el Fake.
- **Stubbing innecesario** (`UnnecessaryStubbingException` de Mockito strict stubs): configurar un `when()` que el test nunca llega a ejercitar — señal de que el test tiene más setup del que realmente necesita.

## Checklist

- [ ] ¿Se mockean las dependencias de la clase bajo test, nunca la clase bajo test misma?
- [ ] ¿`verify()` se usa solo cuando el propósito del test es comprobar un efecto secundario explícito?
- [ ] ¿Se ha evitado el "stubbing innecesario" (configuración de mocks no usada por el test)?
- [ ] ¿Un Fake (repositorio en memoria) sería más claro que un mock extenso para este caso?

Ver también: [`junit.md`](../junit/junit.md), [`../architecture/testing.md`](../architecture/testing.md).
