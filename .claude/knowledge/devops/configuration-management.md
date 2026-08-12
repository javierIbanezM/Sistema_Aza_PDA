# Configuration Management

> Ver [`environments.md`](./environments.md) para cómo la configuración varía por entorno, y [`secrets-management.md`](./secrets-management.md) para el caso especial de configuración sensible.

## Configuración externalizada, nunca embebida en el artefacto

```
El mismo artefacto/imagen se despliega en dev/staging/producción, con configuración
INYECTADA en tiempo de despliegue (variables de entorno, ConfigMap), nunca recompilada por entorno
```

Ver `CLAUDE.md` sección 4.2/5.1 — construir un artefacto distinto por entorno (con configuración hardcodeada en cada build) rompe la garantía de que "lo que se probó en staging es exactamente lo que se despliega en producción", ya que técnicamente son binarios distintos.

## Jerarquía de precedencia explícita y documentada

```
Valor por defecto en código < archivo de configuración base < configuración específica de entorno <
variable de entorno del despliegue < argumento de línea de comandos
```

Ver [`../spring/spring-boot.md`](../spring/spring-boot.md) para el orden de precedencia concreto de Spring Boot como ejemplo — cualquier stack del framework debe tener este orden documentado y consistente, para que nadie tenga que adivinar qué valor prevalece ante configuración definida en múltiples niveles.

## Validación de configuración al arranque, no en el primer uso

```java
@ConfigurationProperties(prefix = "app.pagos")
public record PagosProperties(@NotBlank String proveedor, @Min(1) int timeoutMs) {}
```

Ver [`../node/typescript.md`](../node/typescript.md)/[`../spring/spring-core.md`](../spring/spring-core.md) — la aplicación debe fallar rápido al arrancar si la configuración es inválida/incompleta, no arrancar "aparentemente bien" y fallar más tarde en producción cuando esa configuración concreta se usa por primera vez.

## Configuración como código, versionada, revisada en PR

Mismo principio que [`infrastructure-as-code.md`](./infrastructure-as-code.md) — archivos de configuración base (no los secretos, ver [`secrets-management.md`](./secrets-management.md)) viven en el repositorio, versionados, con historial de quién cambió qué valor y por qué, revisables en Pull Request igual que cualquier otro cambio.

## Feature flags como configuración dinámica, sin necesidad de despliegue

```
Sistema de feature flags (LaunchDarkly, o solución propia) permite cambiar comportamiento
en tiempo real sin pasar por el pipeline de CI/CD completo
```

Ver [`deployment-strategies.md`](./deployment-strategies.md) — complementa (no sustituye) la configuración estándar, específicamente para cambios que necesitan activarse/revertirse con mayor velocidad que un ciclo de despliegue completo.

## Checklist

- [ ] ¿El mismo artefacto se despliega en todos los entornos, con configuración inyectada externamente?
- [ ] ¿La jerarquía de precedencia de configuración está documentada y es consistente?
- [ ] ¿La aplicación valida su configuración al arranque, fallando rápido ante configuración inválida?
- [ ] ¿La configuración base (no secretos) vive versionada en el repositorio, revisable en PR?

Ver también: [`environments.md`](./environments.md), [`secrets-management.md`](./secrets-management.md), [`infrastructure-as-code.md`](./infrastructure-as-code.md).
