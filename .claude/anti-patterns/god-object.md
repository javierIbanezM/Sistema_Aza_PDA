# Antipatrón: God Object (Objeto Dios)

## Descripción

Una clase que acumula demasiadas responsabilidades, conociendo y controlando gran parte del sistema, en lugar de delegar en colaboradores especializados. Es la manifestación extrema de la violación del Single Responsibility Principle (ver [`../solid.md`](../knowledge/architecture/solid.md)).

## Cómo detectarlo

- Clases con nombres genéricos que van acumulando métodos con el tiempo: `Manager`, `Helper`, `Utils`, `Service` sin calificar de qué es servicio.
- Un archivo que supera varios cientos (o miles) de líneas y sigue creciendo porque "es donde va todo lo relacionado con X".
- La clase tiene dependencias inyectadas de prácticamente todos los subsistemas del proyecto (repositorios, clientes HTTP, servicios de email, de notificaciones, de facturación... todos en la misma clase).
- Cualquier cambio en el sistema, sin importar el área funcional, termina requiriendo tocar esta clase.
- Alta tasa de conflictos de merge en este archivo, porque distintos desarrolladores trabajando en features distintas terminan editándolo simultáneamente.

## Ejemplo del antipatrón

```java
public class PedidoManager {
    // Depende de prácticamente todo el sistema
    private PedidoRepository pedidoRepository;
    private ClienteRepository clienteRepository;
    private ServicioEmail servicioEmail;
    private ServicioFacturacion servicioFacturacion;
    private ServicioInventario servicioInventario;
    private ServicioNotificacionesPush servicioNotificaciones;
    private ServicioAnalytics servicioAnalytics;

    public void crearPedido(...) { /* ... */ }
    public void confirmarPedido(...) { /* ... */ }
    public void cancelarPedido(...) { /* ... */ }
    public void calcularDescuentos(...) { /* ... */ }
    public void generarFactura(...) { /* ... */ }
    public void enviarEmailConfirmacion(...) { /* ... */ }
    public void actualizarInventario(...) { /* ... */ }
    public void registrarEventoAnalytics(...) { /* ... */ }
    public void generarReporteVentas(...) { /* ... */ }
    // ... 40 métodos más
}
```

## Cómo evitarlo

- Identificar los distintos **motivos de cambio** (actores) que conviven en la clase y extraer una clase/servicio por cada uno (ver SRP en [`../solid.md`](../knowledge/architecture/solid.md)).
- Aplicar Domain-Driven Design: dividir por Bounded Context y Agregado (ver [`../ddd.md`](../knowledge/architecture/ddd.md)) en lugar de por conveniencia técnica ("todo lo de pedidos va junto").
- Usar Domain Events (ver [`../patterns/observer.md`](../patterns/observer.md)) para desacoplar efectos secundarios (enviar email, actualizar analítica) del flujo principal, en lugar de que un único orquestador central llame directamente a todos los subsistemas.
- Revisar en cada Pull Request si una clase existente está creciendo con responsabilidades ajenas a su propósito original, y extraerlas de inmediato en lugar de "arreglarlo más adelante".

## Cómo solucionarlo (si ya existe)

1. Escribir tests de caracterización que cubran el comportamiento actual (ver [`../refactoring.md`](../knowledge/architecture/refactoring.md)) antes de tocar nada, dado el alto riesgo de regresión en una clase con tanta superficie.
2. Identificar los grupos de métodos/campos cohesivos entre sí (los que cambian juntos, los que comparten los mismos campos).
3. Extraer cada grupo a una clase nueva con nombre específico (Extract Class, ver [`../refactoring.md`](../knowledge/architecture/refactoring.md)), en pasos pequeños e independientes, verificando los tests tras cada extracción.
4. Sustituir las llamadas directas entre las nuevas clases por inyección de dependencias explícita (ver DIP en [`../solid.md`](../knowledge/architecture/solid.md)), evitando que la clase original simplemente delegue todo internamente y siga siendo el único punto de entrada obligatorio.
5. Repetir hasta que cada clase resultante tenga un único motivo de cambio claro.

## Checklist para prevenirlo

- [ ] ¿Puedo nombrar en una frase corta y específica la única responsabilidad de esta clase?
- [ ] ¿Esta clase depende de más de 4-5 colaboradores de subsistemas no relacionados entre sí?
- [ ] ¿Ha crecido este archivo de forma sostenida en los últimos PRs sin que nadie lo cuestione?

Ver también: [`../solid.md`](../knowledge/architecture/solid.md), [`../ddd.md`](../knowledge/architecture/ddd.md), [`SpaghettiCode.md`](./spaghetti.md).
