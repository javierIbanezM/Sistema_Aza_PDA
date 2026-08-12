# Maven

> Ver [`gradle.md`](../gradle/gradle.md) para el sistema de build alternativo. Este framework no impone Maven sobre Gradle ni viceversa — usa el que ya esté establecido en el proyecto; ambos documentos existen porque ambos son comunes en proyectos Java/Spring reales.

## Estructura mínima de un `pom.xml` con buenas prácticas

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.empresa.pedidos</groupId>
    <artifactId>pedidos-service</artifactId>
    <version>1.4.2</version> <!-- versionado semántico, nunca "1.0-SNAPSHOT" permanente en main -->

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
    </parent>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope> <!-- scope explícito, separado de producción -->
        </dependency>
    </dependencies>
</project>
```

## Versiones fijas, nunca rangos abiertos

```xml
<!-- Evitar -->
<version>[3.0,)</version>
<version>LATEST</version>

<!-- Correcto -->
<version>3.3.4</version>
```

Un rango abierto o `LATEST` produce builds no reproducibles: la misma configuración de `pom.xml` puede resolver una versión de dependencia distinta en cada ejecución, según cuándo se ejecute — rompe la reproducibilidad del build, esencial para CI/CD (ver `CLAUDE.md` sección 9.3).

## Gestión centralizada de versiones con BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Un BOM (Bill of Materials) centraliza las versiones de un conjunto de dependencias relacionadas en un único lugar — evita declarar la versión de cada starter de Spring Cloud individualmente y arriesgarse a combinaciones incompatibles entre ellos.

## Scopes de dependencia

| Scope | Uso |
|---|---|
| `compile` (por defecto) | Disponible en compilación, test y runtime |
| `provided` | Disponible en compilación/test, pero provisto por el entorno de ejecución en producción (ej. un servlet container) |
| `runtime` | No necesario en compilación, solo en ejecución (drivers JDBC) |
| `test` | Solo disponible en compilación y ejecución de tests |

Nunca dejar una dependencia de test (JUnit, Mockito, Testcontainers) sin `<scope>test</scope>` — de lo contrario se empaqueta también en el artefacto de producción, aumentando su tamaño y superficie de ataque innecesariamente.

## Perfiles de Maven para configuración por entorno

```xml
<profiles>
    <profile>
        <id>ci</id>
        <properties>
            <skipITs>false</skipITs> <!-- ejecutar integration tests solo en CI, no en cada build local -->
        </properties>
    </profile>
</profiles>
```

```bash
mvn verify -Pci
```

## Ciclo de vida y comandos esenciales

| Comando | Efecto |
|---|---|
| `mvn compile` | Compila el código fuente |
| `mvn test` | Ejecuta tests unitarios (`*Test.java` por convención) |
| `mvn verify` | Ejecuta tests de integración (`*IT.java` por convención, con Failsafe plugin) además de los unitarios |
| `mvn package` | Empaqueta el artefacto (jar/war) |
| `mvn install` | Instala el artefacto en el repositorio local, disponible para otros proyectos locales |
| `mvn dependency:tree` | Muestra el árbol de dependencias resuelto — esencial para diagnosticar conflictos de versión |

## Análisis de seguridad de dependencias en el pipeline

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>10.0.4</version>
    <executions>
        <execution>
            <goals><goal>check</goal></goals>
        </execution>
    </executions>
</plugin>
```

Integrado en el pipeline de CI (ver `CLAUDE.md` sección 12.1, Vulnerable and Outdated Components) para fallar el build ante vulnerabilidades conocidas de severidad alta/crítica en las dependencias.

## Checklist

- [ ] ¿Todas las versiones de dependencias son fijas, sin rangos abiertos ni `LATEST`?
- [ ] ¿Las dependencias de test tienen `<scope>test</scope>` explícito?
- [ ] ¿Existe un análisis de vulnerabilidades de dependencias en el pipeline de CI?
- [ ] ¿Se usa un BOM para gestionar versiones de un conjunto de dependencias relacionadas, en lugar de declararlas sueltas?

Ver también: [`gradle.md`](../gradle/gradle.md), `CLAUDE.md` sección 9.3.
