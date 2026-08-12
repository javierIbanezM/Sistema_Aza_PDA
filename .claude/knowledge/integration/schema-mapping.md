# Schema Mapping

> Ver [`../architecture/ddd.md`](../architecture/ddd.md) (Anti-Corruption Layer) y [`../../patterns/adapter.md`](../../patterns/adapter.md) para los patrones de diseño que este documento aplica específicamente al mapeo entre sistemas heterogéneos de integración.

## El problema: cada sistema tiene su propio modelo de datos

Un "cliente" en el CRM tiene campos y semántica distintos a un "cliente" en el ERP, que a su vez difieren de un "cliente" en el sistema propio — mismo concepto de negocio, tres representaciones incompatibles. Integrar sin un mapeo explícito produce, con el tiempo, que el modelo propio se "contamine" con los detalles y peculiaridades de cada sistema externo integrado.

## Anti-Corruption Layer: traducir en la frontera, nunca propagar el modelo externo

```java
// Modelo del ERP externo (fuera de nuestro control, con su propia semántica de campos)
public class SapClienteRecord {
    public String kunnr;      // código de cliente SAP
    public String stcd1;      // NIF/CIF en el formato específico de SAP
    public String vkorg;      // organización de ventas
}

// Modelo propio, en el lenguaje ubicuo del dominio (ver ../architecture/ddd.md)
public class Cliente {
    private final ClienteId id;
    private final Nif nif; // Value Object propio, con su propia validación
}

// Anti-Corruption Layer: único punto que conoce ambos modelos
public class SapClienteAdapter {
    public Cliente aDominio(SapClienteRecord registro) {
        return new Cliente(new ClienteId(registro.kunnr), new Nif(registro.stcd1));
    }
}
```

Ver [`../architecture/ddd.md`](../architecture/ddd.md) para el detalle completo del patrón — el resto del sistema propio nunca ve `SapClienteRecord` ni sus campos crípticos específicos de SAP, solo el modelo de dominio propio, ya traducido.

## Mapeo de campos: explícito, nunca por convención de nombre implícita

```yaml
# Mapeo declarativo, revisable, versionable — evita mapeos "mágicos" implícitos por coincidencia de nombre
mapeo_cliente:
  campo_origen: kunnr        → campo_destino: id
  campo_origen: stcd1        → campo_destino: nif  (con validación adicional de formato)
  campo_origen: name1+name2  → campo_destino: nombreCompleto  (concatenación explícita)
```

Mapear "por convención" (asumir que un campo `nombre` en el origen corresponde automáticamente a `nombre` en el destino solo porque coinciden los nombres) es frágil ante cambios de esquema en cualquiera de los dos sistemas — un mapeo explícito y documentado (código o configuración declarativa) es más verboso pero detecta en revisión/testing cualquier desalineación, en lugar de fallar silenciosamente en producción.

## Valores enumerados: tablas de correspondencia explícitas

```java
private static final Map<String, EstadoPedido> MAPEO_ESTADOS_ERP = Map.of(
    "10", EstadoPedido.BORRADOR,
    "20", EstadoPedido.CONFIRMADO,
    "90", EstadoPedido.CANCELADO
    // el ERP usa códigos numéricos internos sin significado propio fuera de su documentación
);
```

Los códigos de estado/tipo de sistemas legados rara vez son autoexplicativos — documentar la tabla de correspondencia explícitamente (no solo en el código, también en documentación accesible al equipo) y fallar de forma visible (excepción, no un valor por defecto silencioso) ante un código no mapeado, para detectar cuando el sistema externo introduce un valor nuevo no contemplado.

## Diferencias de precisión/unidades: normalizar en la frontera

```java
// El ERP externo devuelve céntimos como entero; el dominio propio usa BigDecimal con divisa
BigDecimal monto = BigDecimal.valueOf(registro.montoEnCentimos).divide(BigDecimal.valueOf(100));
```

Diferencias de unidades (céntimos vs. unidad monetaria completa), de zona horaria (UTC vs. hora local del sistema origen), o de precisión numérica deben normalizarse explícitamente en el Adapter — nunca asumir implícitamente que ambos sistemas usan la misma convención sin haberlo verificado contra la documentación real del sistema externo.

## Checklist

- [ ] ¿Existe un Anti-Corruption Layer explícito, sin que el modelo del sistema externo se filtre al resto del sistema propio?
- [ ] ¿El mapeo de campos es explícito y documentado, no implícito por coincidencia de nombres?
- [ ] ¿Los valores enumerados tienen una tabla de correspondencia explícita, con fallo visible ante valores no mapeados?
- [ ] ¿Se han verificado y normalizado explícitamente las diferencias de unidades/precisión/zona horaria?

Ver también: [`../architecture/ddd.md`](../architecture/ddd.md), [`../../patterns/adapter.md`](../../patterns/adapter.md), [`data-synchronization.md`](./data-synchronization.md).
