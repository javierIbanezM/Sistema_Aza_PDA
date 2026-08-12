# CloudTrail

> Ver [`../aws-security/iam.md`](../aws-security/iam.md) — CloudTrail es el mecanismo de auditoría de las acciones realizadas con esas identidades.

## Qué es: registro de auditoría de toda la actividad de la cuenta AWS

Registra **quién hizo qué, cuándo y desde dónde** para prácticamente cualquier llamada a la API de AWS (crear/modificar/eliminar recursos, cambios de política IAM, acceso a datos en algunos servicios) — la fuente de verdad para investigar cualquier incidente de seguridad o cambio no autorizado en la infraestructura.

## Habilitado por defecto, pero con retención limitada salvo configuración explícita

CloudTrail registra los últimos 90 días de eventos de gestión automáticamente sin configuración adicional — para retención mayor (requisito de auditoría/cumplimiento) o para registrar eventos de datos (accesos individuales a objetos S3, invocaciones de Lambda), es necesario crear un **Trail** explícito que envíe los eventos a un bucket S3 propio con la retención deseada.

## Eventos de gestión vs. eventos de datos

| Tipo | Ejemplo | Volumen |
|---|---|---|
| Eventos de gestión | Crear una instancia EC2, modificar una política IAM | Bajo, habilitado por defecto |
| Eventos de datos | Cada `GetObject` en S3, cada invocación de Lambda | Alto volumen, coste adicional, deshabilitado por defecto |

Habilitar eventos de datos selectivamente solo donde el requisito de auditoría lo justifica (buckets con datos sensibles) — habilitarlos globalmente sin criterio genera un volumen y coste considerable sin beneficio proporcional.

## Trail multi-región y multi-cuenta

```
Trail organizacional: agrega eventos de todas las cuentas de la organización a un bucket S3 centralizado
```

En una organización con múltiples cuentas AWS, un Trail organizacional centraliza la auditoría — evita que cada cuenta gestione su propio Trail de forma aislada, con el riesgo de que una cuenta comprometida pueda desactivar su propio registro de auditoría sin que se detecte desde una cuenta central independiente.

## Integridad del log: protección contra manipulación

```
Log file validation habilitado + bucket S3 con versionado + política que impide eliminar objetos
```

Un atacante que compromete una cuenta podría intentar borrar el rastro de sus acciones eliminando logs de CloudTrail — habilitar validación de integridad del archivo de log (detecta manipulación) y proteger el bucket S3 destino con versionado y políticas restrictivas de eliminación, idealmente en una cuenta AWS separada dedicada a auditoría, fuera del alcance de las credenciales comprometidas en la cuenta operativa.

## Checklist

- [ ] ¿Existe un Trail explícito con retención mayor a los 90 días por defecto, si el requisito de auditoría lo exige?
- [ ] ¿Los eventos de datos están habilitados selectivamente donde el volumen/coste está justificado por el riesgo real?
- [ ] ¿El Trail es organizacional (multi-cuenta) si existe más de una cuenta AWS?
- [ ] ¿El bucket S3 destino está protegido contra eliminación, idealmente en una cuenta separada de auditoría?

Ver también: [`cloudwatch.md`](./cloudwatch.md), [`../aws-security/iam.md`](../aws-security/iam.md), `CLAUDE.md` sección 12.1.
