# FSx

> Ver [`efs.md`](./efs.md) para el sistema de archivos compartido nativo de AWS — FSx cubre casos donde se necesita un sistema de archivos específico de un proveedor concreto.

## Qué es: sistemas de archivos gestionados de terceros

FSx ofrece versiones completamente gestionadas por AWS de sistemas de archivos específicos que EFS no replica:

| Variante | Uso |
|---|---|
| FSx for Windows File Server | Compatibilidad SMB nativa para cargas Windows, integración con Active Directory |
| FSx for Lustre | Sistema de archivos de alto rendimiento para HPC/machine learning, integrable directamente con datasets en S3 |
| FSx for NetApp ONTAP | Migración de cargas que ya usan NetApp on-premise, con sus funcionalidades específicas (snapshots, replicación) |
| FSx for OpenZFS | Funcionalidades específicas de ZFS (snapshots, clones) gestionadas |

## Cuándo FSx en lugar de EFS

- La aplicación requiere específicamente protocolo SMB (aplicaciones Windows) — EFS es NFS únicamente.
- Cargas de alto rendimiento de cómputo (HPC) que se benefician de FSx for Lustre, con integración directa a datasets de S3 para entrenamiento de modelos.
- Migración de una infraestructura on-premise que ya usa NetApp/ZFS y se busca preservar esa funcionalidad específica sin reescribir procesos operativos existentes.

## Checklist

- [ ] ¿Se necesita específicamente SMB, Lustre, o funcionalidad NetApp/ZFS que EFS no provee?
- [ ] ¿Se ha evaluado EFS primero, dado su menor coste operativo, antes de justificar FSx?

Ver también: [`efs.md`](./efs.md), [`s3.md`](./s3.md).
