# Debugging (Java)

> Ver [`../architecture/debugging.md`](../architecture/debugging.md) para la metodología sistemática completa (reproducir, hipótesis, bisección). Este documento cubre las herramientas y técnicas **específicas de Java** para ejecutar esa metodología.

## Leer un stack trace correctamente

```
Exception in thread "main" java.lang.NullPointerException:
    Cannot invoke "Cliente.getEmail()" because "cliente" is null
        at com.empresa.pedidos.application.NotificarClienteUseCase.ejecutar(NotificarClienteUseCase.java:34)
        at com.empresa.pedidos.interface.PedidoController.confirmar(PedidoController.java:52)
```

- Leer **de arriba hacia abajo**: la primera línea (`at ...NotificarClienteUseCase...:34`) es el punto exacto donde ocurrió el fallo — el resto es la cadena de llamadas que llevó hasta ahí.
- El mensaje moderno de NPE (JDK 14+, "Helpful NullPointerMessages") indica **qué referencia exacta** era nula (`"cliente" is null`), evitando tener que adivinar entre varias posibles en la misma línea.
- `Caused by:` en un stack trace señala una excepción envuelta — la causa raíz suele estar en el `Caused by:` más profundo, no en la excepción externa (ver [`../../standards/exceptions.md`](../../standards/exceptions.md) sobre preservar la causa raíz al envolver excepciones).

## Breakpoints condicionales (IDE)

En lugar de un breakpoint que detiene la ejecución en cada iteración de un bucle, un breakpoint condicional (`pedido.getId().equals("p-9931")`) detiene solo cuando la condición sospechosa se cumple — esencial para depurar un fallo que ocurre solo con datos concretos dentro de una colección grande.

## `jstack` para diagnosticar bloqueos (deadlocks, hilos colgados)

```bash
jstack <pid> > threads.txt
```

Busca en la salida bloques `Found one Java-level deadlock` (deadlock detectado explícitamente por la JVM) o hilos en estado `BLOCKED` esperando el mismo monitor — diagnóstico directo de contención de locks sin necesidad de instrumentar el código.

## Remote debugging (depurar una JVM en otro proceso/contenedor)

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar
```

Permite conectar el debugger del IDE a una JVM en ejecución en un contenedor/entorno de staging, poniendo breakpoints sin necesidad de reproducir el problema únicamente en local — útil cuando el fallo depende de datos o configuración presentes solo en ese entorno.

## `git bisect` combinado con tests JUnit

```bash
git bisect start
git bisect bad                     # el commit actual reproduce el bug
git bisect good v2.3.0              # esta versión anterior no lo tenía
git bisect run mvn test -Dtest=PedidoConfirmacionTest  # o ./gradlew test --tests ...
```

Automatiza la bisección (ver [`../architecture/debugging.md`](../architecture/debugging.md)) ejecutando un test específico que reproduce el fallo en cada commit intermedio, hasta que Git identifica automáticamente el commit exacto que introdujo la regresión.

## `LazyInitializationException` — diagnóstico específico

Un fallo frecuente en aplicaciones Spring/Hibernate: acceder a una relación `LAZY` fuera de una transacción activa. El stack trace señala la línea exacta de acceso, pero la **causa raíz** suele estar en dónde termina la transacción (el límite del método `@Transactional`), no en la línea donde se lanza la excepción — ver [`hibernate.md`](../hibernate/hibernate.md) para la explicación completa y la corrección (cargar explícitamente con `JOIN FETCH` dentro de la transacción, mapear a DTO antes de salir de ella).

## Logging temporal de alta verbosidad, con contexto

```java
log.debug("Estado del pedido antes de confirmar: id={}, lineas={}, estado={}",
    pedido.getId(), pedido.getLineas().size(), pedido.getEstado());
```

Preferir logs estructurados con variables explícitas (ver [`../../standards/logging-standard.md`](../../standards/logging-standard.md)) sobre `System.out.println` de depuración rápida — los primeros quedan disponibles para diagnósticos futuros si se elevan a `DEBUG` permanente; los segundos hay que recordar retirarlos y no aparecen en el sistema de logs agregados.

## Checklist

- [ ] ¿Se ha leído el `Caused by:` más profundo del stack trace, no solo la excepción externa?
- [ ] ¿Se ha usado un breakpoint condicional en lugar de detenerse en cada iteración de un bucle grande?
- [ ] ¿Se ha descartado un deadlock/contención de locks con `jstack` si el síntoma es un hilo/petición colgada?
- [ ] ¿Se ha usado `git bisect` con un test automatizado para localizar el commit exacto de una regresión?

Ver también: [`../architecture/debugging.md`](../architecture/debugging.md), [`hibernate.md`](../hibernate/hibernate.md), [`../../standards/exceptions.md`](../../standards/exceptions.md).
