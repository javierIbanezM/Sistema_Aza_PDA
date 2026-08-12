# Artifact Management

> Ver [`ci-cd.md`](./ci-cd.md) para dónde se genera el artefacto en el pipeline, y [`containerization.md`](./containerization.md) para el caso específico de imágenes de contenedor.

## Repositorio de artefactos: fuente única de verdad de qué se puede desplegar

```
Artefactos publicados: JARs (Maven/Nexus), imágenes (ECR/Docker Hub), paquetes npm (registro privado)
```

Un artefacto solo se considera "desplegable" tras publicarse en el repositorio correspondiente desde el pipeline de CI — nunca desplegado directamente desde la máquina de un desarrollador, ni desde un build local, para preservar la trazabilidad de qué pipeline y qué commit exacto lo generó (ver [`ci-cd.md`](./ci-cd.md)).

## Inmutabilidad: un artefacto publicado nunca se sobrescribe

```
pedidos-service:2.4.1 — una vez publicado con este tag, el contenido asociado a ese tag NUNCA cambia
```

Mismo principio que `CLAUDE.md` sección 9.3 aplica a tags de imagen — si se necesita corregir algo, se publica una versión nueva (`2.4.2`), nunca se sobrescribe el contenido de una versión ya publicada, que podría estar referenciada por un despliegue ya en curso o por documentación/tickets que apuntan a esa versión específica.

## Escaneo de vulnerabilidades antes de permitir el despliegue

```
Pipeline: build → escaneo de vulnerabilidades (Trivy/Snyk) → SOLO si pasa, publicar el artefacto
```

Ver `CLAUDE.md` sección 12.1 (Vulnerable and Outdated Components) — el escaneo debe ocurrir antes de la publicación, no como una verificación posterior opcional; un artefacto con vulnerabilidades críticas conocidas nunca debería llegar a estar disponible para desplegar.

## Retención y limpieza de artefactos antiguos

```
Retener: últimas N versiones desplegadas + versiones actualmente en producción/staging
Limpiar: versiones antiguas sin referencia activa, tras un periodo de gracia
```

Sin política de retención, el repositorio de artefactos crece indefinidamente con coste asociado — pero nunca eliminar una versión que podría necesitarse para un rollback (ver [`rollback.md`](./rollback.md)), calibrando el periodo de retención según cuánto tiempo atrás es razonable necesitar revertir.

## Firma de artefactos: verificar integridad antes de desplegar

```
Artefacto firmado criptográficamente en el pipeline → verificación de firma antes de desplegar en producción
```

Protege contra la posibilidad de que un artefacto haya sido alterado entre su publicación y su despliegue (compromiso del repositorio, ataque de intermediario) — relevante especialmente en cadenas de suministro de software con múltiples pasos entre build y despliegue final.

## Checklist

- [ ] ¿Todo artefacto desplegable se publica exclusivamente desde el pipeline, nunca desde una máquina local?
- [ ] ¿Los artefactos publicados son inmutables, nunca sobrescritos bajo el mismo identificador/tag?
- [ ] ¿El escaneo de vulnerabilidades bloquea la publicación de artefactos con hallazgos críticos?
- [ ] ¿Existe una política de retención que equilibra coste con capacidad de rollback?

Ver también: [`ci-cd.md`](./ci-cd.md), [`containerization.md`](./containerization.md), `CLAUDE.md` sección 9.3 y 12.1.
