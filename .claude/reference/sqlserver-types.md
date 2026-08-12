# Reference: SQL Server Data Types

Consulta rápida de tipos de datos y su equivalencia en Java. Sin razonamiento — ver [`../knowledge/sqlserver/`](../knowledge/sqlserver/) para explicaciones.

| SQL Server | Equivalente Java | Notas |
|---|---|---|
| `INT` | `Integer` | 4 bytes, -2.147.483.648 a 2.147.483.647 |
| `BIGINT` | `Long` | 8 bytes |
| `SMALLINT` | `Short` | 2 bytes |
| `TINYINT` | `Short`/`Integer` | 1 byte, sin signo (0-255) — Java no tiene tipo sin signo de 1 byte |
| `BIT` | `Boolean` | 0/1/NULL |
| `DECIMAL(p,s)` / `NUMERIC(p,s)` | `BigDecimal` | Precisión exacta — usar siempre para dinero, nunca `FLOAT`/`REAL` |
| `FLOAT` | `Double` | Precisión aproximada — evitar para valores monetarios |
| `REAL` | `Float` | Precisión aproximada, menor que `FLOAT` |
| `VARCHAR(n)` | `String` | Caracteres no-Unicode, tamaño fijo máximo `n` |
| `NVARCHAR(n)` | `String` | Caracteres Unicode — preferido por defecto para evitar problemas de codificación |
| `NVARCHAR(MAX)` | `String` | Sin límite práctico de tamaño (hasta 2GB) |
| `CHAR(n)` / `NCHAR(n)` | `String` | Longitud fija, rellenado con espacios — evitar salvo requisito específico |
| `DATE` | `LocalDate` | Solo fecha, sin hora |
| `TIME` | `LocalTime` | Solo hora, sin fecha |
| `DATETIME2` | `LocalDateTime` / `Instant` | Preferido sobre `DATETIME` (mayor precisión, rango más amplio) |
| `DATETIME` | `LocalDateTime` | Legado — preferir `DATETIME2` en esquemas nuevos |
| `DATETIMEOFFSET` | `OffsetDateTime` | Incluye zona horaria — usar cuando el dato debe preservar el offset original |
| `UNIQUEIDENTIFIER` | `UUID` | GUID de 16 bytes |
| `VARBINARY(n)` / `VARBINARY(MAX)` | `byte[]` | Datos binarios de tamaño variable |
| `XML` | `String` (o tipo XML del driver) | Validable contra un esquema XSD registrado en el motor |

## Notas de mapeo JPA/Hibernate

Ver [`../knowledge/hibernate/jpa.md`](../knowledge/hibernate/jpa.md) para las anotaciones `@Column`/`@Enumerated` correspondientes. Regla general: `NVARCHAR` sobre `VARCHAR` por defecto para evitar problemas de codificación con caracteres no ASCII (nombres con acentos, ñ, etc.), y `DECIMAL`/`NUMERIC` siempre para columnas monetarias, nunca `FLOAT`/`REAL`.
