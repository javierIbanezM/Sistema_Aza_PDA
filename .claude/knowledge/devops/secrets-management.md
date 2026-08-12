# Secrets Management

> Ver [`../aws-security/secrets-manager.md`](../aws-security/secrets-manager.md) y [`../../decisions/secrets-manager-vs-parameter-store.md`](../../decisions/secrets-manager-vs-parameter-store.md) para la implementación AWS — este documento cubre el proceso DevOps de gestión de secretos a través de todo el ciclo de vida (pipeline, despliegue, rotación), no solo el almacén.

## Secretos nunca en el repositorio de código, en ningún commit, ni siquiera histórico

```
Pre-commit hook / escaneo en el pipeline: bloquea cualquier commit que parezca contener
una credencial (patrones de API key, claves privadas) ANTES de que llegue al historial
```

Ver `CLAUDE.md` sección 12.2 y [`../../standards/git.md`](../../standards/git.md) — un secreto commiteado y luego eliminado sigue existiendo en el historial de git accesible a cualquiera con acceso al repositorio; el escaneo preventivo (herramientas como `gitleaks`/`git-secrets`) debe bloquear el commit, no solo alertar después.

## Secretos del pipeline de CI/CD: mismo rigor que en producción

```
Credenciales usadas POR el pipeline (para desplegar, para acceder a un registro privado)
gestionadas con el mismo cuidado que los secretos de la aplicación en producción
```

Un pipeline de CI/CD con credenciales de despliegue de amplio alcance es un objetivo de alto valor para un atacante — aplicar menor privilegio también a las credenciales que el propio pipeline usa (ver [`../aws-security/iam.md`](../aws-security/iam.md)), no solo a las de la aplicación en ejecución.

## Rotación como proceso, no como evento único

Ver [`../aws-security/secrets-manager.md`](../aws-security/secrets-manager.md) para rotación automática nativa donde existe integración — para secretos sin esa integración nativa, la rotación debe ser un proceso programado y probado (¿la aplicación recoge el nuevo valor sin reinicio, o requiere uno?), no un evento manual excepcional que solo ocurre tras un incidente de seguridad.

## Inyección en tiempo de despliegue, nunca en la imagen

```
Imagen de contenedor: SIN secretos embebidos, ni siquiera en una capa "eliminada" después
(las capas de Docker persisten en el historial de la imagen)
Secreto inyectado como variable de entorno/volumen montado EN TIEMPO DE EJECUCIÓN
```

Ver [`containerization.md`](./containerization.md) — un secreto copiado a una imagen y luego "eliminado" en una instrucción posterior del Dockerfile sigue presente en la capa anterior, extraíble por cualquiera con acceso a la imagen; los secretos se inyectan al arrancar el contenedor, nunca se construyen dentro de la imagen.

## Auditoría de acceso a secretos

```
Cada lectura de un secreto queda registrada: quién/qué accedió, cuándo (ver aws-monitoring/cloudtrail.md)
```

Ante la sospecha de compromiso de una credencial, poder responder "¿quién/qué ha leído este secreto y cuándo?" es esencial para acotar el alcance del incidente — un almacén de secretos sin auditoría de acceso no permite esa investigación.

## Checklist

- [ ] ¿Existe escaneo preventivo que bloquea commits con secretos antes de que lleguen al historial?
- [ ] ¿Las credenciales del propio pipeline de CI/CD siguen el mismo principio de menor privilegio que las de producción?
- [ ] ¿Los secretos se inyectan en tiempo de ejecución, nunca embebidos en la imagen de contenedor?
- [ ] ¿Existe auditoría de acceso a cada secreto?

Ver también: [`../aws-security/secrets-manager.md`](../aws-security/secrets-manager.md), [`containerization.md`](./containerization.md), `CLAUDE.md` sección 12.2.
