# Antipatrón: SQL Injection

## Descripción

Vulnerabilidad de seguridad que ocurre cuando input de usuario se concatena directamente en una sentencia SQL sin sanear ni parametrizar, permitiendo que un atacante inyecte fragmentos de SQL arbitrario que se ejecutan con los mismos privilegios que la query original. Es el número 3 del OWASP Top 10 (categoría *Injection*) y una de las vulnerabilidades más antiguas y con mayor potencial de impacto (lectura/modificación/borrado no autorizado de datos, bypass de autenticación, en algunos casos ejecución de comandos en el propio servidor de base de datos).

## Cómo detectarlo

- Cualquier concatenación de strings (`+`, template literals, `String.format`) usada para construir una sentencia SQL que incluye datos de entrada del usuario.
- Uso de `Statement` en JDBC en lugar de `PreparedStatement` cuando la query incluye variables.
- Queries construidas dinámicamente para funcionalidades de "búsqueda avanzada"/"filtros dinámicos" sin usar un mecanismo parametrizado para cada filtro.
- Ausencia de un linter/analizador estático (SAST) en el pipeline de CI que detecte patrones de concatenación SQL (ver sección 9.3 y 12 de `CLAUDE.md`).

## Ejemplo del antipatrón

```java
// VULNERABLE: concatenación directa de input de usuario
String query = "SELECT * FROM usuarios WHERE email = '" + emailInput + "' AND password = '" + passwordInput + "'";
Statement stmt = connection.createStatement();
ResultSet rs = stmt.executeQuery(query);
```

Con `emailInput = "' OR '1'='1"`, la query resultante se convierte en:

```sql
SELECT * FROM usuarios WHERE email = '' OR '1'='1' AND password = '...'
```

que devuelve todos los usuarios de la tabla, permitiendo un bypass de autenticación sin conocer ninguna credencial real.

```javascript
// VULNERABLE (Node.js con driver SQL crudo)
const query = `SELECT * FROM productos WHERE nombre LIKE '%${terminoBusqueda}%'`;
const resultado = await connection.query(query);
```

Con `terminoBusqueda = "'; DROP TABLE productos; --"`, un atacante podría intentar ejecutar sentencias destructivas adicionales, dependiendo de si el driver permite múltiples sentencias en una misma llamada.

## Cómo evitarlo

### Java/JDBC: `PreparedStatement` siempre

```java
String query = "SELECT * FROM usuarios WHERE email = ? AND password_hash = ?";
PreparedStatement stmt = connection.prepareStatement(query);
stmt.setString(1, emailInput);
stmt.setString(2, passwordHashInput);
ResultSet rs = stmt.executeQuery();
```

El driver JDBC trata `emailInput`/`passwordHashInput` como **datos**, nunca como parte de la estructura SQL, sin importar qué caracteres contengan.

### ORM parametrizado (JPA/Hibernate, TypeORM, Prisma)

Los ORMs parametrizan automáticamente cuando se usan correctamente (query methods, `@Query` con parámetros nombrados/posicionales, query builders con placeholders):

```java
@Query("SELECT u FROM Usuario u WHERE u.email = :email")
Usuario findByEmail(@Param("email") String email); // parametrizado automáticamente por JPA
```

```typescript
// TypeORM QueryBuilder parametrizado
const usuario = await connection
  .createQueryBuilder(Usuario, 'u')
  .where('u.email = :email', { email: emailInput }) // parametrizado, no concatenado
  .getOne();
```

**Advertencia**: los ORMs también permiten construir queries con concatenación cruda si el desarrollador usa APIs de "raw query" sin cuidado (`query.raw(\`... ${input} ...\`)`) — el ORM no protege automáticamente contra SQL Injection si se le da una query ya concatenada.

### Node.js con drivers SQL: placeholders del driver

```javascript
// Seguro: el driver parametriza internamente
const resultado = await connection.query(
  'SELECT * FROM productos WHERE nombre LIKE ?',
  [`%${terminoBusqueda}%`]
);
```

### SQL Server: procedimientos almacenados parametrizados

```sql
CREATE PROCEDURE sp_BuscarUsuarioPorEmail
    @Email NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM Usuarios WHERE Email = @Email; -- parámetro, no concatenación
END
```

Invocado siempre con el mecanismo de parámetros del cliente (`SqlCommand.Parameters.Add` en .NET, `PreparedStatement` en Java), nunca construyendo el texto de la llamada al procedimiento por concatenación.

### Validación adicional de defensa en profundidad

Aunque la parametrización es la defensa principal y suficiente contra SQL Injection, se recomienda además:

- Validar el formato del input en el borde de la aplicación (whitelisting de caracteres permitidos según el tipo de dato esperado) — ver sección 12 de `CLAUDE.md`.
- Aplicar el principio de menor privilegio a las credenciales de base de datos que usa la aplicación (el usuario de base de datos de la aplicación no debería tener permisos de `DROP TABLE` si la aplicación nunca necesita ejecutar esa operación).

## Checklist

- [ ] ¿Existe alguna concatenación de string usada para construir una sentencia SQL que incluya datos de entrada del usuario?
- [ ] ¿Todas las queries dinámicas (búsquedas, filtros configurables) usan parámetros en lugar de concatenación, incluso para nombres de columna/orden (usando whitelisting de valores permitidos en esos casos, ya que los parámetros SQL no pueden sustituir identificadores de columna)?
- [ ] ¿El usuario de base de datos que usa la aplicación tiene únicamente los privilegios mínimos necesarios?
- [ ] ¿Hay un análisis estático (SAST) en el pipeline de CI que detecte patrones de concatenación SQL?

Ver también: sección 12 de `CLAUDE.md` (Seguridad y OWASP), sección 6 de `CLAUDE.md` (Bases de Datos).
