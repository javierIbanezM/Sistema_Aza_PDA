# Antipatrón: Memory Leak (Fuga de Memoria)

## Descripción

Ocurre cuando un programa retiene referencias a objetos que ya no necesita, impidiendo que el recolector de basura (o el gestor de memoria manual, en lenguajes sin GC) los libere. Con el tiempo, el consumo de memoria crece de forma sostenida hasta degradar el rendimiento o provocar un fallo por falta de memoria (`OutOfMemoryError`, `OOM Killer` en contenedores). Este documento cubre los patrones generales; las fugas específicas de Android (Activity/Context leaks) se documentan en `anti-patterns/MemoryLeaks.md` y `anti-patterns/ContextLeaks.md` de la Skill `android-studio`, que enlazan aquí para los conceptos generales sin repetirlos.

## Cómo detectarlo

- Consumo de memoria del proceso que crece de forma sostenida a lo largo del tiempo bajo carga constante, sin estabilizarse (visible en dashboards de métricas, ver [`../logging.md`](../knowledge/architecture/logging.md)).
- Incremento del tiempo/frecuencia de pausas de recolección de basura (GC) en JVM/Node.js con el tiempo de ejecución.
- Herramientas de profiling de memoria (heap dumps, `Chrome DevTools Memory tab`, `VisualVM`/`JFR` en Java) muestran un número creciente de instancias de una clase concreta que debería haberse liberado.
- Listeners/observadores registrados que nunca se des-registran cuando el objeto que los originó deja de usarse.

## Causas comunes

### 1. Listeners/observadores no des-registrados

```java
public class Dashboard {
    public Dashboard(EventBus eventBus) {
        eventBus.suscribir("actualizacion-precio", this::onActualizacionPrecio); // se registra...
        // ...pero nunca se des-registra al destruir el Dashboard
    }
}
```

Cada instancia de `Dashboard` creada y descartada queda retenida indefinidamente por el `EventBus`, porque el `EventBus` mantiene una referencia activa a su listener. Ver el patrón Observer en [`../patterns/observer.md`](../patterns/observer.md) para el diseño correcto del ciclo de vida de suscripción/des-suscripción.

### 2. Cachés sin límite ni política de expiración

```javascript
const cache = new Map(); // crece indefinidamente, nunca se limpia ni tiene límite de tamaño

function obtenerConCache(id) {
  if (!cache.has(id)) {
    cache.set(id, calcularValorCostoso(id));
  }
  return cache.get(id);
}
```

Si `id` toma un número no acotado de valores distintos a lo largo de la vida del proceso, el `Map` crece sin límite. Ver la Skill de Redis (`cache-aside.md`) para el patrón correcto de caché con TTL y límite de tamaño, y la sección 6.5 de `CLAUDE.md`.

### 3. Closures que retienen referencias innecesarias (JavaScript/Node.js)

```javascript
function crearManejador(datosGrandes) {
  // datosGrandes puede ser innecesario para la función retornada, pero el closure lo retiene igualmente
  return function manejarEvento(evento) {
    console.log('Evento recibido:', evento.tipo); // no usa datosGrandes en absoluto
  };
}
```

Aunque `manejarEvento` no usa `datosGrandes`, el closure mantiene una referencia a todo el ámbito léxico de `crearManejador`, incluyendo `datosGrandes`, impidiendo que se libere mientras `manejarEvento` siga referenciado en algún lugar (ej. registrado como listener de larga duración).

### 4. Conexiones/recursos no cerrados

```java
public List<String> leerArchivo(String ruta) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(ruta));
    List<String> lineas = new ArrayList<>();
    String linea;
    while ((linea = reader.readLine()) != null) {
        lineas.add(linea);
    }
    return lineas; // si ocurre una excepción antes de este return, el reader nunca se cierra
}
```

Recursos externos al heap gestionado (file handles, sockets, conexiones de base de datos) no se liberan automáticamente por el GC de la misma forma que la memoria del heap — requieren cierre explícito.

## Cómo evitarlo

### Try-with-resources / try-finally (Java)

```java
public List<String> leerArchivo(String ruta) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
        List<String> lineas = new ArrayList<>();
        String linea;
        while ((linea = reader.readLine()) != null) {
            lineas.add(linea);
        }
        return lineas;
    } // reader.close() se garantiza incluso si ocurre una excepción
}
```

### Des-registro simétrico de listeners

Todo `suscribir()`/`addListener()`/`addEventListener()` debe tener su contraparte `desuscribir()`/`removeListener()`/`removeEventListener()` invocada en el punto simétrico del ciclo de vida (al destruir el componente, al cerrar la conexión, al finalizar el scope que lo originó).

```java
public class Dashboard implements AutoCloseable {
    private final EventBus eventBus;
    private final Subscription subscription;

    public Dashboard(EventBus eventBus) {
        this.eventBus = eventBus;
        this.subscription = eventBus.suscribir("actualizacion-precio", this::onActualizacionPrecio);
    }

    @Override
    public void close() {
        subscription.cancelar(); // rompe la referencia mantenida por el EventBus
    }
}
```

### Cachés con límite y expiración

Usar una librería de caché con política de evicción (LRU, TTL) en lugar de una estructura de datos sin límites — ver `cache-aside.md` de la Skill de Redis y la sección 6.5 de `CLAUDE.md` para el patrón recomendado con TTL explícito.

### WeakReference/WeakMap cuando la retención debe ser débil por diseño

En casos donde una estructura necesita asociar datos a un objeto sin impedir su recolección (ej. metadatos asociados a un objeto de vida más corta que la propia estructura), `WeakReference` (Java) o `WeakMap`/`WeakSet` (JavaScript) permiten que el GC libere el objeto aunque la estructura aún lo referencie.

## Cómo diagnosticarlo en producción

1. Confirmar la tendencia de crecimiento sostenido de memoria con métricas a lo largo del tiempo, descartando picos normales seguidos de liberación tras GC.
2. Tomar un heap dump en un momento de consumo elevado (`jmap`/`JFR` en Java, `--inspect` + Chrome DevTools en Node.js).
3. Comparar dos heap dumps tomados en momentos distintos bajo carga similar; identificar qué clases han crecido en número de instancias de forma desproporcionada.
4. Rastrear las referencias que retienen esas instancias (*retained paths*, visible en la mayoría de herramientas de análisis de heap) hasta encontrar el punto donde debería haberse liberado la referencia y no ocurrió.

## Checklist

- [ ] ¿Todo listener/observador registrado tiene un des-registro simétrico garantizado en el ciclo de vida correspondiente?
- [ ] ¿Las cachés en memoria tienen un límite de tamaño y/o política de expiración (TTL)?
- [ ] ¿Los recursos externos (archivos, sockets, conexiones) se cierran con `try-with-resources`/`try-finally` o su equivalente, garantizando el cierre incluso ante excepciones?
- [ ] ¿Se ha verificado con profiling de memoria bajo carga sostenida que el consumo se estabiliza en lugar de crecer indefinidamente?

Ver también: [`../patterns/observer.md`](../patterns/observer.md), sección 6.5 de `CLAUDE.md` (Redis), Skill `android-studio` (`anti-patterns/MemoryLeaks.md`, `anti-patterns/ContextLeaks.md`) para fugas específicas de Android.
