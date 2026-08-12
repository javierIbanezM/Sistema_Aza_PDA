# Security

> Ver `CLAUDE.md` sección 12 (OWASP) y [`../../anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md) para el marco general de seguridad que este documento aplica específicamente a SQL Server.

## Principio de menor privilegio: roles, no permisos directos al login de aplicación

```sql
-- Crear un usuario de aplicación con permisos mínimos, nunca db_owner ni sysadmin
CREATE LOGIN AppPedidosLogin WITH PASSWORD = '...'; -- gestionado vía vault, ver CLAUDE.md sección 12.2
CREATE USER AppPedidosUser FOR LOGIN AppPedidosLogin;

-- Rol de aplicación con permisos exactos a las tablas que necesita
CREATE ROLE RolAppPedidos;
GRANT SELECT, INSERT, UPDATE ON Pedidos TO RolAppPedidos;
GRANT SELECT, INSERT, UPDATE ON LineasPedido TO RolAppPedidos;
GRANT EXECUTE ON sp_ConfirmarPedido TO RolAppPedidos;
-- Sin DELETE si la aplicación nunca borra físicamente (soft delete), sin DDL, sin acceso a otras tablas

ALTER ROLE RolAppPedidos ADD MEMBER AppPedidosUser;
```

El usuario que la aplicación usa para conectarse **nunca** debe tener `db_owner`, `sysadmin`, ni permisos sobre tablas/operaciones que la aplicación no necesita realmente — si la aplicación es comprometida (ej. mediante una vulnerabilidad de inyección SQL no detectada en otra capa), el radio de daño queda acotado a exactamente lo que ese usuario puede hacer.

## Prevención de SQL Injection en el motor

Ver [`../../anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md) para el detalle completo (parametrización desde la capa de aplicación). A nivel de SQL Server específicamente:

```sql
-- Procedimiento almacenado parametrizado — el motor trata @clienteId como dato, nunca como SQL
CREATE PROCEDURE sp_BuscarPedidosPorCliente
    @clienteId VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM Pedidos WHERE ClienteId = @clienteId;
END;
```

```sql
-- Peligroso: SQL dinámico con concatenación, incluso dentro de un procedimiento almacenado
CREATE PROCEDURE sp_BuscarDinamico @filtro VARCHAR(100)
AS
BEGIN
    DECLARE @sql NVARCHAR(MAX) = 'SELECT * FROM Pedidos WHERE ' + @filtro; -- VULNERABLE
    EXEC(@sql);
END;
```

Si se necesita SQL verdaderamente dinámico (nombres de columna/tabla variables, que no se pueden parametrizar de forma estándar), usar `sp_executesql` con parámetros para cualquier **valor**, y validar contra una whitelist explícita cualquier **identificador** (nombre de columna/tabla) que deba variar — nunca concatenar un identificador recibido directamente de input de usuario.

## Row-Level Security (RLS)

```sql
CREATE FUNCTION dbo.fn_FiltroClientePorTenant(@tenantId UNIQUEIDENTIFIER)
RETURNS TABLE
WITH SCHEMABINDING
AS RETURN SELECT 1 AS Resultado WHERE @tenantId = CAST(SESSION_CONTEXT(N'TenantId') AS UNIQUEIDENTIFIER);

CREATE SECURITY POLICY PoliticaTenant
ADD FILTER PREDICATE dbo.fn_FiltroClientePorTenant(TenantId) ON dbo.Pedidos,
ADD BLOCK PREDICATE dbo.fn_FiltroClientePorTenant(TenantId) ON dbo.Pedidos AFTER INSERT;
```

En sistemas multi-tenant, RLS aplica el filtro de aislamiento **a nivel de motor**, de forma transparente a cualquier query — incluso si una capa de aplicación olvida añadir `WHERE TenantId = ...` en alguna query, el motor lo aplica igualmente. Defensa en profundidad más robusta que depender únicamente de que cada query de la aplicación incluya el filtro correctamente.

## Cifrado

- **En tránsito**: forzar TLS en la conexión (`Encrypt=True` en la cadena de conexión, certificado válido en el servidor) — nunca tráfico sin cifrar entre la aplicación y SQL Server en producción.
- **En reposo**: Transparent Data Encryption (TDE) cifra los archivos de datos/log/backup en disco, transparente a las queries — protege ante robo físico de los archivos, no ante un usuario autorizado que consulta la base de datos normalmente.
- **Columnas específicas** (`Always Encrypted`): cifra columnas concretas (números de tarjeta, datos muy sensibles) de forma que ni siquiera un administrador de base de datos con acceso completo puede leer el valor en claro — el cifrado/descifrado ocurre en el driver del cliente, la clave nunca llega al servidor.

## Auditoría

```sql
CREATE SERVER AUDIT AuditoriaAccesosSensibles
TO FILE (FILEPATH = 'D:\Auditoria\');

CREATE DATABASE AUDIT SPECIFICATION AuditPedidos
FOR SERVER AUDIT AuditoriaAccesosSensibles
ADD (SELECT, UPDATE, DELETE ON dbo.Pedidos BY public);

ALTER SERVER AUDIT AuditoriaAccesosSensibles WITH (STATE = ON);
ALTER DATABASE AUDIT SPECIFICATION AuditPedidos WITH (STATE = ON);
```

Relevante para cumplimiento normativo y para el requisito de logging de seguridad de `CLAUDE.md` sección 12.1 (Security Logging and Monitoring Failures) — auditar accesos a tablas con datos sensibles, no solo confiar en el logging de aplicación.

## Checklist

- [ ] ¿El usuario de aplicación tiene únicamente los permisos exactos que necesita, nunca `db_owner`/`sysadmin`?
- [ ] ¿Todo SQL dinámico usa `sp_executesql` parametrizado, con identificadores validados contra whitelist si deben variar?
- [ ] ¿Se ha evaluado Row-Level Security en sistemas multi-tenant como defensa adicional a nivel de motor?
- [ ] ¿Las conexiones fuerzan TLS y los datos sensibles en reposo están protegidos (TDE/Always Encrypted según el caso)?
- [ ] ¿Existe auditoría habilitada sobre tablas con datos sensibles?

Ver también: `CLAUDE.md` sección 12, [`../../anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md), [`../../standards/validation.md`](../../standards/validation.md).
