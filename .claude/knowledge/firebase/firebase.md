# Firebase

> Ver `CLAUDE.md` sección 12 para el marco general de seguridad que las reglas de Firestore/Realtime Database implementan.

## Reglas de seguridad: nunca abiertas en producción

```javascript
// firestore.rules — NUNCA en producción
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true; // cualquiera puede leer/escribir cualquier cosa
    }
  }
}
```

```javascript
// Correcto: reglas explícitas por colección, verificando autenticación y propiedad del dato
service cloud.firestore {
  match /databases/{database}/documents {
    match /pedidos/{pedidoId} {
      allow read: if request.auth != null && resource.data.clienteId == request.auth.uid;
      allow write: if request.auth != null && request.resource.data.clienteId == request.auth.uid;
    }
  }
}
```

Las reglas de seguridad de Firestore/Realtime Database son la **única** barrera de acceso cuando el cliente accede directamente (sin backend intermedio) — un error aquí es equivalente a no tener autenticación/autorización en absoluto, ver `CLAUDE.md` sección 12.1 (Broken Access Control).

## Firebase Auth como proveedor de identidad

```kotlin
val auth = Firebase.auth

suspend fun iniciarSesion(email: String, password: String): FirebaseUser {
    return auth.signInWithEmailAndPassword(email, password).await().user
        ?: throw AutenticacionFallidaException()
}
```

Firebase Auth resuelve la identidad del usuario; la autorización de negocio (qué puede hacer ese usuario concreto) suele requerir un backend propio que combine el token de Firebase con reglas de negocio más ricas que las expresables en reglas de seguridad declarativas — ver el criterio de menor privilegio en `CLAUDE.md` sección 12.

## Firebase Cloud Messaging (notificaciones push)

```kotlin
class MiFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(mensaje: RemoteMessage) {
        mensaje.notification?.let { mostrarNotificacionLocal(it.title, it.body) }
    }

    override fun onNewToken(token: String) {
        // enviar el nuevo token al backend propio para asociarlo al usuario autenticado
        enviarTokenAlBackend(token)
    }
}
```

El token de FCM puede cambiar (reinstalación, restauración de backup) — `onNewToken` debe sincronizarse siempre con el backend, nunca asumir que el token capturado una vez sigue siendo válido indefinidamente.

## Crashlytics: contexto útil sin datos sensibles

```kotlin
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("pedido_id", pedidoId) // contexto útil para diagnóstico
    setUserId(usuarioIdHasheado) // NUNCA email/nombre en claro, ver standards/logging-standard.md
}
```

Mismo principio que [`../../standards/logging-standard.md`](../../standards/logging-standard.md) aplicado a un servicio externo — cualquier dato adjuntado a un reporte de crash está sujeto a las mismas reglas de qué nunca debe loguearse.

## Checklist

- [ ] ¿Las reglas de seguridad de Firestore/Realtime Database son explícitas por colección, nunca `allow read, write: if true`?
- [ ] ¿El token de FCM se sincroniza con el backend en `onNewToken`, no solo en el registro inicial?
- [ ] ¿Los datos adjuntados a Crashlytics excluyen información personal identificable sin hashear/enmascarar?

Ver también: `CLAUDE.md` sección 12, [`../../standards/logging-standard.md`](../../standards/logging-standard.md).
