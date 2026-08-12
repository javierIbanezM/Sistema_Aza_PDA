# Pattern: Adapter

## Descripción

Adapter convierte la interfaz de una clase existente en otra interfaz que el código cliente espera, permitiendo que clases con interfaces incompatibles colaboren sin modificar ninguna de las dos. Es el patrón central detrás de un Anti-Corruption Layer (ver [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md)) y de cualquier integración con una librería o API externa cuyo contrato no coincide con el que el dominio necesita.

## Cuándo usarlo

- Al integrar una librería de terceros o un sistema legado cuya interfaz no coincide con la que el resto del código espera.
- Al migrar de una dependencia a otra (por ejemplo, de un SDK de pagos a otro) sin que el código cliente tenga que cambiar, adaptando ambas al mismo contrato interno.
- Al construir un Anti-Corruption Layer entre Bounded Contexts (ver [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md)).

## Cuándo NO usarlo

- Cuando se controla el código de ambas partes y se puede simplemente ajustar una de las dos interfaces directamente — el Adapter tiene sentido cuando una de las dos partes es código externo que no se puede/debe modificar.
- Como capa "por si acaso" sobre una única librería sin planes reales de sustituirla ni de aislar su contrato (ver YAGNI en [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).

## Ventajas

- Permite reutilizar código existente (propio o de terceros) sin modificarlo.
- Aísla al resto del sistema de los detalles y cambios de la interfaz externa.
- Facilita sustituir una dependencia externa por otra sin propagar el cambio por todo el código cliente.

## Desventajas

- Añade una capa de indirección y de mapeo que hay que mantener.
- Si se abusa del patrón (un Adapter por cada pequeña librería usada una sola vez), añade complejidad sin beneficio real.

## Ejemplo

```java
// Interfaz que el dominio/aplicación espera (puerto propio)
public interface ProcesadorPagos {
    ResultadoPago procesar(Pago pago);
}

// SDK de terceros con una interfaz incompatible que no controlamos
public class StripeSdkClient {
    public StripeChargeResponse createCharge(StripeChargeRequest request) { /* ... */ }
}

// Adapter: traduce el contrato propio al contrato del SDK externo
public class StripeProcesadorPagosAdapter implements ProcesadorPagos {
    private final StripeSdkClient stripeClient;

    public StripeProcesadorPagosAdapter(StripeSdkClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public ResultadoPago procesar(Pago pago) {
        StripeChargeRequest request = new StripeChargeRequest(
            pago.getMonto().getValor(),
            pago.getMonto().getMoneda().getCodigoIso(),
            pago.getTarjetaToken()
        );
        StripeChargeResponse response = stripeClient.createCharge(request);
        return new ResultadoPago(response.isSuccess(), response.getChargeId());
    }
}
```

El resto de la aplicación depende únicamente de `ProcesadorPagos` (ver DIP en [`../knowledge/architecture/solid.md`](../knowledge/architecture/solid.md)); si mañana se cambia de Stripe a otro proveedor, se escribe un nuevo Adapter sin tocar ningún caso de uso.

## Errores comunes

- Filtrar tipos del SDK externo (`StripeChargeResponse`) fuera del Adapter, hacia la capa de dominio/aplicación — el Adapter debe ser el único punto que conoce el tipo externo.
- Confundir Adapter con Facade (ver [`facade.md`](./facade.md)): el Adapter cambia una interfaz por otra que el cliente ya espera; el Facade simplifica un subsistema complejo con una interfaz nueva más simple, sin necesariamente adaptarse a un contrato preexistente.

Ver también: [`facade.md`](./facade.md), [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md) (Anti-Corruption Layer), [`../knowledge/architecture/solid.md`](../knowledge/architecture/solid.md) (DIP).
