# Permissions (Android)

> Ver `CLAUDE.md` sección 12 para el marco general de seguridad — este documento cubre el modelo de permisos específico de Android.

## Solicitud de permisos en tiempo de ejecución (Android 6+)

```kotlin
@Composable
fun PantallaCamara() {
    val permisoCamara = rememberPermissionState(Manifest.permission.CAMERA)

    when {
        permisoCamara.status.isGranted -> VistaCamara()
        permisoCamara.status.shouldShowRationale -> {
            // el usuario ya denegó una vez — mostrar explicación antes de volver a pedir
            ExplicacionPermiso(onSolicitar = { permisoCamara.launchPermissionRequest() })
        }
        else -> LaunchedEffect(Unit) { permisoCamara.launchPermissionRequest() }
    }
}
```

Declarar el permiso en `AndroidManifest.xml` es necesario pero no suficiente — para permisos "peligrosos" (cámara, ubicación, contactos), la app debe solicitarlo explícitamente en tiempo de ejecución y manejar tanto la concesión como la denegación (incluida la denegación permanente, "no volver a preguntar").

## Principio de menor privilegio en permisos

Solicitar solo los permisos estrictamente necesarios para la funcionalidad actual, en el momento en que se necesitan (no todos al arrancar la app) — un usuario tiene más probabilidad de conceder un permiso de cámara cuando acaba de pulsar "hacer foto" que cuando la app se lo pide en la pantalla de bienvenida sin contexto.

## Permisos de ubicación: granularidad

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

Solicitar `ACCESS_COARSE_LOCATION` (ubicación aproximada) si la funcionalidad no requiere precisión exacta — el usuario puede conceder solo la ubicación aproximada incluso si la app solicita la precisa (Android 12+), y solicitar más precisión de la necesaria genera fricción de confianza sin beneficio funcional real.

## Verificación antes de usar una funcionalidad protegida

```kotlin
fun tomarFoto() {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {
        // nunca asumir que el permiso sigue concedido — el usuario puede revocarlo desde Ajustes en cualquier momento
        solicitarPermiso()
        return
    }
    // proceder con la funcionalidad
}
```

Un permiso concedido no es una garantía permanente — el usuario puede revocarlo desde los ajustes del sistema en cualquier momento sin que la app se entere hasta el siguiente intento de uso; verificar siempre antes de acceder al recurso protegido, no solo la primera vez.

## Checklist

- [ ] ¿Los permisos se solicitan en el momento en que se necesitan, con contexto, no todos al arrancar?
- [ ] ¿Se maneja explícitamente tanto la concesión como la denegación (incluida la denegación permanente)?
- [ ] ¿Se verifica el permiso antes de cada uso de la funcionalidad protegida, no solo en la solicitud inicial?
- [ ] ¿Se solicita el permiso de menor precisión suficiente para la funcionalidad (ubicación aproximada si basta)?

Ver también: `CLAUDE.md` sección 12.
