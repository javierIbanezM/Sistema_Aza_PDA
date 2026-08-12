# Spring Boot

> Ver [`spring-core.md`](./spring-core.md) para el contenedor Spring subyacente (DI, estereotipos). Este documento cubre lo que Spring Boot añade específicamente: auto-configuración, starters y arranque de aplicación.

## Qué añade Spring Boot sobre Spring Framework

Spring Framework provee el contenedor de IoC y programación orientada a aspectos; Spring Boot añade **auto-configuración** (configura beans automáticamente según las dependencias presentes en el classpath) y **starters** (agrupaciones curadas de dependencias compatibles entre sí), eliminando la configuración manual extensa que Spring Framework puro requería.

## Starters: una dependencia, un conjunto de capacidades

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Este único starter trae Spring MVC, Tomcat embebido, Jackson (serialización JSON) y validación básica — todas las versiones ya verificadas como mutuamente compatibles por el equipo de Spring Boot (ver [`../maven/maven.md`](../maven/maven.md)/[`../gradle/gradle.md`](../gradle/gradle.md) para la gestión de versiones vía BOM).

## Auto-configuración: cómo funciona y cuándo desactivarla

Spring Boot inspecciona el classpath y la configuración presente para decidir qué beans crear automáticamente (`@ConditionalOnClass`, `@ConditionalOnMissingBean` internamente). Es transparente en el caso común, pero cuando algo "mágico" no se comporta como se espera:

```bash
# Ver exactamente qué auto-configuraciones se aplicaron y cuáles no, y por qué
java -jar app.jar --debug
```

El reporte de auto-configuración (`--debug`) es la primera herramienta de diagnóstico ante un comportamiento inesperado de arranque — señala explícitamente cada auto-configuración evaluada y la condición que la activó o descartó.

## Punto de entrada

```java
@SpringBootApplication // combina @Configuration + @EnableAutoConfiguration + @ComponentScan
public class PedidosApplication {
    public static void main(String[] args) {
        SpringApplication.run(PedidosApplication.class, args);
    }
}
```

## Propiedades externalizadas y su orden de precedencia (resumen práctico)

De mayor a menor prioridad (las de mayor prioridad sobreescriben a las de menor):

1. Argumentos de línea de comandos (`--server.port=8081`)
2. Variables de entorno (`SERVER_PORT=8081`)
3. `application-{perfil}.yml` (específico del perfil activo)
4. `application.yml` (por defecto)

Este orden permite que la configuración de un entorno concreto (variables de entorno inyectadas por el orquestador de contenedores) sobreescriba siempre los valores por defecto del artefacto, sin reconstruir la imagen — esencial para no hardcodear secretos ni configuración específica de entorno dentro del jar (ver `CLAUDE.md` sección 12).

## Empaquetado como imagen de contenedor

```bash
./gradlew bootBuildImage   # o mvn spring-boot:build-image
```

Spring Boot 2.3+ integra Cloud Native Buildpacks — genera una imagen Docker optimizada (capas separadas para dependencias/código, sin necesidad de escribir un `Dockerfile` manual) directamente desde el build. Ver la Skill de Docker/Kubernetes (milestone futuro) para el detalle de contenerización más allá de este mecanismo integrado.

## Checklist

- [ ] ¿Se usa `--debug` para diagnosticar auto-configuración inesperada antes de forzar configuración manual?
- [ ] ¿Los secretos/configuración específica de entorno se inyectan vía variables de entorno, no hardcodeados en `application.yml`?
- [ ] ¿Se han evaluado los starters oficiales antes de ensamblar manualmente un conjunto de dependencias equivalente?

Ver también: [`spring-core.md`](./spring-core.md), [`spring-actuator.md`](./spring-actuator.md), [`../maven/maven.md`](../maven/maven.md), [`../gradle/gradle.md`](../gradle/gradle.md).
