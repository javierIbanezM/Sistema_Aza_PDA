# Gradle

> Ver [`maven.md`](../maven/maven.md) para el sistema de build alternativo. Usa el que ya esté establecido en el proyecto.

## `build.gradle.kts` (Kotlin DSL, preferido sobre Groovy DSL en proyectos nuevos)

```kotlin
plugins {
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.0.20" // si el proyecto usa Kotlin; omitir en Java puro
    java
}

group = "com.empresa.pedidos"
version = "1.4.2" // versionado semántico

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.springframework.boot:spring-boot-starter-test") // separado de producción
    testImplementation("org.testcontainers:junit-jupiter:1.20.2")
}

tasks.test {
    useJUnitPlatform()
}
```

## Configuraciones de dependencias: `implementation` vs. `api` vs. `compileOnly`

| Configuración | Efecto |
|---|---|
| `implementation` | Dependencia interna del módulo; no se propaga a módulos que dependen de este (por defecto, preferido) |
| `api` | Se propaga a módulos que dependen de este — usar solo cuando el tipo de la dependencia forma parte del contrato público expuesto |
| `compileOnly` | Disponible en compilación, no en runtime (anotaciones de procesamiento en tiempo de compilación) |
| `runtimeOnly` | No necesario en compilación, solo en runtime (drivers JDBC) |
| `testImplementation` | Solo en compilación/ejecución de tests |

Preferir `implementation` sobre `api` por defecto — `api` filtra transitivamente las dependencias hacia todo módulo consumidor, aumentando el acoplamiento entre módulos de un proyecto multi-módulo de forma no siempre intencional.

## Versiones fijas y gestión centralizada

```kotlin
// gradle/libs.versions.toml — catálogo de versiones centralizado (Gradle 7.4+)
[versions]
spring-boot = "3.3.4"
mapstruct = "1.6.2"

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
mapstruct = { module = "org.mapstruct:mapstruct", version.ref = "mapstruct" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.mapstruct)
}
```

El catálogo de versiones (`libs.versions.toml`) centraliza las versiones en un único archivo, compartible entre módulos de un proyecto multi-módulo, evitando declarar la misma versión repetida en cada `build.gradle.kts` — aplicación directa de DRY (ver [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)) al propio sistema de build.

## Proyectos multi-módulo

```kotlin
// settings.gradle.kts
rootProject.name = "pedidos-monorepo"
include("pedidos-api", "pedidos-domain", "pedidos-infrastructure")
```

Reflejar la separación de capas de Clean Architecture (ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md)) como módulos Gradle separados es una forma de **forzar en tiempo de compilación** la regla de dependencia: si `pedidos-domain` no declara ninguna dependencia hacia `pedidos-infrastructure`, es físicamente imposible que el dominio importe una clase de infraestructura, sin depender solo de la disciplina del equipo.

## Comandos esenciales

| Comando | Efecto |
|---|---|
| `./gradlew build` | Compila, testea y empaqueta |
| `./gradlew test` | Ejecuta tests |
| `./gradlew dependencies` | Árbol de dependencias resuelto, para diagnosticar conflictos de versión |
| `./gradlew bootRun` | Ejecuta la aplicación Spring Boot localmente |
| `./gradlew clean` | Limpia artefactos de build previos |

Usar siempre el **Gradle Wrapper** (`./gradlew`), nunca una instalación global de `gradle` — garantiza que todo el equipo y el pipeline de CI usan exactamente la misma versión de Gradle, evitando builds no reproducibles.

## Caché de build y ejecución incremental

Gradle cachea automáticamente las tareas cuyo input no ha cambiado (`UP-TO-DATE`), y soporta caché remota compartida entre desarrolladores/CI (`Gradle Build Cache`) para evitar recompilar/retestar código que ya se procesó en otra máquina con el mismo input exacto — relevante en pipelines de CI con múltiples builds concurrentes.

## Checklist

- [ ] ¿Se usa el Gradle Wrapper (`./gradlew`), no una instalación global?
- [ ] ¿Las dependencias usan `implementation` por defecto, `api` solo cuando es necesario propagarlas?
- [ ] ¿Las versiones están centralizadas en un catálogo (`libs.versions.toml`) en proyectos multi-módulo?
- [ ] ¿La estructura de módulos refleja las capas de Clean Architecture cuando el proyecto lo justifica?

Ver también: [`maven.md`](../maven/maven.md), [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md).
