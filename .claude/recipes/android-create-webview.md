# Recipe: Crear un WebView (Android)

Aplica: `CLAUDE.md` sección 12, [`../knowledge/android/permissions.md`](../knowledge/android/permissions.md).

## Pasos

### 1. Configuración mínima segura

```kotlin
@Composable
fun PantallaWebView(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true // solo si es estrictamente necesario para el contenido a mostrar
            settings.domStorageEnabled = true
            settings.allowFileAccess = false // deshabilitado por defecto — riesgo de acceso a archivos locales
            settings.allowContentAccess = false

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    // whitelist de dominios permitidos — nunca navegar libremente a cualquier URL
                    return if (esDominioPermitido(request.url)) false else true.also {
                        abrirEnNavegadorExterno(request.url)
                    }
                }
            }
            loadUrl(url)
        }
    })
}
```

### 2. Whitelist de dominios (nunca navegación libre sin control)

```kotlin
private val dominiosPermitidos = setOf("miempresa.com", "pagos.proveedor-confiable.com")

fun esDominioPermitido(uri: Uri): Boolean = dominiosPermitidos.any { uri.host?.endsWith(it) == true }
```

Un `WebView` sin control de navegación puede seguir enlaces hacia dominios no confiables — verificar cada navegación contra una whitelist explícita, redirigiendo lo que no coincide al navegador externo del sistema (que tiene sus propias protecciones) en lugar de cargarlo dentro de la app.

### 3. JavaScript Interface: superficie de ataque, minimizar

```kotlin
class PuenteJavaScript(private val onMensaje: (String) -> Unit) {
    @JavascriptInterface // solo métodos anotados son accesibles desde JS — nunca exponer más de lo necesario
    fun enviarMensaje(mensaje: String) {
        onMensaje(mensaje) // validar el contenido antes de actuar sobre él, igual que cualquier input externo
    }
}

webView.addJavascriptInterface(PuenteJavaScript(::manejarMensaje), "AndroidBridge")
```

Cada método expuesto vía `@JavascriptInterface` es código Kotlin invocable directamente desde cualquier JavaScript que se ejecute en el WebView — si el contenido cargado no es de confianza absoluta, cada método expuesto es una superficie de ataque potencial; exponer el mínimo estrictamente necesario y validar cualquier dato recibido igual que cualquier input externo no confiable (ver [`../standards/validation.md`](../standards/validation.md)).

### 4. Manejo de errores de carga

```kotlin
webViewClient = object : WebViewClient() {
    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (request.isForMainFrame) mostrarPantallaError() // solo para el recurso principal, no subrecursos menores
    }
}
```

### 5. Limpieza al desmontar (evitar fugas)

```kotlin
DisposableEffect(Unit) {
    onDispose {
        webView.destroy() // libera recursos nativos del WebView explícitamente
    }
}
```

## Checklist final

- [ ] ¿La navegación está restringida a una whitelist explícita de dominios?
- [ ] ¿`allowFileAccess`/`allowContentAccess` están deshabilitados salvo necesidad específica justificada?
- [ ] ¿Los `@JavascriptInterface` expuestos son el mínimo necesario, con validación de cualquier dato recibido?
- [ ] ¿El WebView se destruye explícitamente al desmontar el Composable?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: `CLAUDE.md` sección 12, [`../standards/validation.md`](../standards/validation.md).
