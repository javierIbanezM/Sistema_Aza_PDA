# Secrets Manager

> Ver [`../../decisions/secrets-manager-vs-parameter-store.md`](../../decisions/secrets-manager-vs-parameter-store.md) para la comparativa con Parameter Store. Ver `CLAUDE.md` sección 12.2 para el principio general del framework de gestión de secretos vía vault.

## Qué es: almacenamiento gestionado de secretos con rotación

Almacena credenciales, API keys y otros secretos cifrados (vía KMS, ver [`kms.md`](./kms.md)), con soporte nativo de **rotación automática** para tipos de secreto comunes (credenciales de RDS, por ejemplo) — la aplicación nunca contiene la credencial en su configuración estática, la obtiene en tiempo de ejecución.

## Rotación automática: el diferenciador clave

```
Secrets Manager rota la contraseña de RDS cada 30 días automáticamente:
1. Genera nueva contraseña
2. La aplica en RDS
3. Actualiza el secreto almacenado
4. La aplicación obtiene la versión actual en su siguiente consulta, sin intervención manual
```

Para tipos de secreto con integración nativa (RDS, Redshift, DocumentDB), la rotación es gestionada de extremo a extremo por AWS — reduce drásticamente el riesgo de credenciales de larga vida nunca rotadas, un hallazgo de seguridad común en auditorías.

## Consumo desde la aplicación: nunca cachear indefinidamente, pero tampoco consultar en cada petición

```java
@Cacheable(value = "secretos", key = "#secretId")
public String obtenerSecreto(String secretId) {
    return secretsManagerClient.getSecretValue(secretId);
}
```

Cachear el secreto en memoria de la aplicación con un TTL razonable (ver [`../spring/spring-cache.md`](../spring/spring-cache.md)) — consultar Secrets Manager en cada petición añade latencia y coste innecesarios; nunca cachear indefinidamente sin ningún TTL, ya que impediría que una rotación se refleje en la aplicación en un tiempo razonable.

## Versionado de secretos

```
AWSCURRENT: la versión activa que la aplicación debe usar
AWSPREVIOUS: la versión anterior, retenida durante la rotación para evitar cortes si hay una ventana de transición
```

Durante una rotación, ambas versiones pueden ser válidas temporalmente — relevante para bases de datos con conexiones de larga duración que podrían seguir usando la credencial anterior brevemente mientras se completa la transición.

## Integración con ECS/Lambda: inyección sin exponer en variables de entorno estáticas

```json
"secrets": [{ "name": "DB_PASSWORD", "valueFrom": "arn:aws:secretsmanager:...:secret:db-password" }]
```

ECS/Lambda pueden inyectar el valor del secreto directamente como variable de entorno en tiempo de arranque del contenedor, obtenido de Secrets Manager — nunca hardcodeado en la Task Definition ni en el código, cumpliendo `CLAUDE.md` sección 12.2.

## Checklist

- [ ] ¿Los secretos con integración nativa de rotación (RDS, etc.) tienen rotación automática habilitada?
- [ ] ¿La aplicación cachea el secreto con TTL razonable, ni lo consulta en cada petición ni lo cachea indefinidamente?
- [ ] ¿Los secretos se inyectan en tiempo de ejecución (variable de entorno desde Secrets Manager), nunca hardcodeados en código ni en Task Definitions?

Ver también: [`kms.md`](./kms.md), [`../../decisions/secrets-manager-vs-parameter-store.md`](../../decisions/secrets-manager-vs-parameter-store.md), `CLAUDE.md` sección 12.2.
