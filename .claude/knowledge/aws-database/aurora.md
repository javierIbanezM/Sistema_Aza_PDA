# Aurora

> Ver [`rds.md`](./rds.md) para RDS estándar — Aurora es un motor propio de AWS compatible con MySQL/PostgreSQL, con arquitectura de almacenamiento distinta.

## Qué es: motor relacional cloud-native de AWS

Compatible con el protocolo MySQL o PostgreSQL (la aplicación se conecta igual que a esos motores estándar) pero con una capa de almacenamiento distribuida propia de AWS, desacoplada del cómputo — permite recuperación más rápida ante fallo, replicación de lectura con menor lag, y escalado de almacenamiento automático hasta 128TB sin intervención manual.

## Cuándo Aurora en lugar de RDS estándar

- Se necesita mayor rendimiento/disponibilidad que MySQL/PostgreSQL estándar en RDS, con el mismo protocolo de conexión (sin cambiar drivers ni ORM).
- El volumen de datos crece de forma significativa e impredecible — el escalado de almacenamiento de Aurora es automático, sin necesidad de aprovisionar capacidad por adelantado.
- Se necesitan hasta 15 Read Replicas con lag de replicación típicamente menor a 100ms (frente al lag más variable de RDS estándar con replicación tradicional).

## Aurora Serverless v2: escalado de capacidad de cómputo bajo demanda

```
Capacidad mínima: 0.5 ACU | Capacidad máxima: 16 ACU
Escala automáticamente según carga real, en segundos
```

Apropiado para cargas con patrón muy variable (picos impredecibles, entornos de desarrollo/test que no necesitan capacidad constante) — evita pagar por capacidad de pico permanentemente reservada cuando el uso real fluctúa significativamente.

## Aurora Global Database: replicación entre regiones

```
Región primaria (escrituras) → replicación < 1 segundo → Región secundaria (lectura, o promovible ante desastre)
```

Para aplicaciones globales que necesitan baja latencia de lectura en múltiples regiones, o recuperación ante desastre a nivel de región completa — la región secundaria puede promoverse a primaria en minutos ante un fallo de la región primaria completa.

## Compatibilidad: verificar antes de migrar

Aurora es compatible con MySQL/PostgreSQL a nivel de protocolo y en la gran mayoría de funcionalidades, pero no es un clon exacto — verificar extensiones específicas de PostgreSQL o características muy específicas de MySQL usadas por la aplicación antes de asumir compatibilidad total al migrar desde una instancia autogestionada.

## Checklist

- [ ] ¿Se ha confirmado que Aurora aporta beneficio real (rendimiento, escalado automático, Read Replicas) sobre RDS estándar para este caso concreto?
- [ ] ¿Se ha verificado la compatibilidad de extensiones/funcionalidades específicas usadas por la aplicación antes de migrar?
- [ ] ¿Aurora Serverless v2 se ha evaluado para cargas con patrón de uso muy variable, en lugar de capacidad fija sobredimensionada?

Ver también: [`rds.md`](./rds.md), [`../../decisions/rds-vs-dynamodb.md`](../../decisions/rds-vs-dynamodb.md).
