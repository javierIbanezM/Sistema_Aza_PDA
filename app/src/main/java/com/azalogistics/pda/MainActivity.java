package com.azalogistics.pda;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.window.OnBackInvokedDispatcher;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.azalogistics.walkie.ptt.PttManager;
import com.azalogistics.walkie.service.SettingsLockManager;
import com.azalogistics.walkie.service.WalkieService;

import com.datalogic.device.app.AppManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    private WebView webView;
    private static final int FILE_CHOOSER_REQUEST = 1000;
    // PIN de emergencia solo para cuando no hay conexión y por tanto no se puede
    // validar contra el servidor. Distinto del PIN real, que vive en el backend.
    private static final String PIN_EMERGENCIA_OFFLINE = "9173";
    // Debe coincidir con la constante del mismo nombre en indexDev.html/index.html.
    private static final String URL_ACTUALIZACION = "http://SRVAutoN8N.zar.local:5000/updates/versionPDA.json";
    private ValueCallback<Uri[]> filePathCallback;
    private Uri photoUri;
    private int volumenAnterior = -1;
    private ToneGenerator toneGeneratorAlerta;
    private final Handler alertaSoundHandler = new Handler(Looper.getMainLooper());
    private Runnable alertaSoundRunnable;
    private boolean bloqueoAjustesSuspendidoPorAdmin = false;
    private long descargaActualizacionId = -1;
    private BroadcastReceiver descargaCompletadaReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pedirPermisos();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        webView = new WebView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        webView.setLayoutParams(params);

        // Recargar la página también relanza inicializar() en el JS, que ya comprueba
        // actualizaciones — así que este botón hace de "recargar" y "buscar actualización" a
        // la vez, sin duplicar un botón aparte para eso. Solo icono, azul y con esquinas
        // redondeadas para un aspecto más moderno que el botón de texto plano de antes.
        float densidad = getResources().getDisplayMetrics().density;
        Button btnRetry = new Button(this);
        btnRetry.setText("🔄");
        btnRetry.setTextSize(22);
        android.graphics.drawable.GradientDrawable fondoRecargar = new android.graphics.drawable.GradientDrawable();
        fondoRecargar.setColor(0xFF007BFF);
        fondoRecargar.setCornerRadius(28 * densidad);
        btnRetry.setBackground(fondoRecargar);
        btnRetry.setOnClickListener(v -> cargarWebConOffline());
        btnRetry.setOnLongClickListener(v -> {
            mostrarDialogoPin();
            return true;
        });
        LinearLayout.LayoutParams paramsBtnRetry = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (52 * densidad));
        int margenBtnRetry = (int) (12 * densidad);
        paramsBtnRetry.setMargins(margenBtnRetry, margenBtnRetry, margenBtnRetry, margenBtnRetry);
        btnRetry.setLayoutParams(paramsBtnRetry);

        layout.addView(webView);
        layout.addView(btnRetry);

        // Requisito de WindowInsetsControllerCompat para poder ocultar barras del sistema
        // sin que se lleve también el teclado (ver ocultarBarras): hay que desactivar el
        // ajuste automático del decor ANTES de que la ventana se dibuje.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(layout);

        ocultarBarras();
        bloquearBarraNotificaciones();
        registrarReceptorDescarga();
        registrarGestoAtras();
        WalkieService.Companion.start(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        // Sin caché HTTP para la página principal: cada carga/recarga trae siempre la versión
        // real del servidor. Evita que un dispositivo se quede ejecutando un index.html/JS
        // antiguo cacheado aunque el fichero ya se haya actualizado en el servidor.
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // ✅ ELIMINADOS: setAllowUniversalAccessFromFileURLs y setAllowFileAccessFromFileURLs

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {

                MainActivity.this.filePathCallback = filePathCallback;

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PDA");
                    if (!dir.exists()) dir.mkdirs();
                    File photoFile = new File(dir, "IMG_" + System.currentTimeMillis() + ".jpg");
                    photoUri = FileProvider.getUriForFile(
                            MainActivity.this,
                            getPackageName() + ".provider",
                            photoFile);
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    takePicture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    Log.e("PDA", "Error creando fichero foto", e);
                }

                Intent pickFile = new Intent(Intent.ACTION_GET_CONTENT);
                pickFile.setType("image/*");

                Intent chooser = Intent.createChooser(pickFile, "Seleccionar imagen");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePicture});

                startActivityForResult(chooser, FILE_CHOOSER_REQUEST);
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("intent:")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        Log.e("PDA", "Error intent: " + url);
                        return true;
                    }
                }

                Uri uri = Uri.parse(url);
                String host = uri.getHost() != null
                        ? uri.getHost().toLowerCase(java.util.Locale.ROOT) : "";

                boolean hostDeConfianza =
                        host.equals("servidorwebs.zar.local") ||
                        host.equals("SRVAutoN8N.zar.local") ||
                        host.equals("srvauton8n.zar.local") ||
                        host.equals("srvwhalesuat.zar.local") ||
                        host.equals("192.168.2.145") ||
                        host.equals("activalink.com") ||
                        host.endsWith(".activalink.com");

                if (hostDeConfianza) {
                    return false;
                }

                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i);
                } catch (Exception e) {
                    Log.e("PDA", "No se pudo abrir URL externa");
                }
                return true;
            }
        });

        String androidId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);
        webView.addJavascriptInterface(new AndroidBridge(androidId), "AndroidApp");

        cargarWebConOffline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarras();
        if (bloqueoAjustesSuspendidoPorAdmin) {
            bloqueoAjustesSuspendidoPorAdmin = false;
            SettingsLockManager.INSTANCE.setEnabled(AjustesBloqueo.estaActivo(this));
        }
    }

    @Override
    protected void onDestroy() {
        if (descargaCompletadaReceiver != null) {
            try { unregisterReceiver(descargaCompletadaReceiver); } catch (Exception e) { /* ya desregistrado */ }
        }
        super.onDestroy();
    }

    // Popup de "hay versión nueva" (ver indexDev.html/index.html): al terminar la descarga del
    // APK con DownloadManager, abre el instalador del sistema apuntando al fichero descargado.
    private void registrarReceptorDescarga() {
        descargaCompletadaReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id != descargaActualizacionId) return;
                try {
                    File apk = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "actualizacion.apk");
                    Uri apkUri = FileProvider.getUriForFile(
                            MainActivity.this, getPackageName() + ".provider", apk);
                    Intent instalar = new Intent(Intent.ACTION_VIEW);
                    instalar.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    instalar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(instalar);
                } catch (Exception e) {
                    Log.e("PDA", "Error abriendo instalador de la actualización", e);
                    Toast.makeText(MainActivity.this, "No se pudo abrir el instalador", Toast.LENGTH_LONG).show();
                }
            }
        };
        ContextCompat.registerReceiver(
                this, descargaCompletadaReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0 && PttManager.INSTANCE.onKeyEvent(keyCode, true)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (PttManager.INSTANCE.onKeyEvent(keyCode, false)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void cargarWebConOffline() {
        if (hayConexion()) {
            webView.loadUrl(ServerConfig.getBaseUrl(this) + "/home");
        } else {
            webView.loadUrl("file:///android_asset/offline.html");
        }
    }

    private boolean hayConexion() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // API correcta para Android 10+, fallback para Android 9
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
    }

    // WalkieService.start() no arranca en Android 14+ sin RECORD_AUDIO (servicio de tipo
    // micrófono): en el primer arranque el permiso aún no está dado, así que se reintenta aquí.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            WalkieService.Companion.start(this);
        }
    }

    private void pedirPermisos() {
        String[] permisos = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
        };

        for (String p : permisos) {
            if (ContextCompat.checkSelfPermission(this, p)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permisos, 1);
                break;
            }
        }
    }

    // View.setSystemUiVisibility (legacy) trata las barras del sistema y el teclado (IME)
    // como el mismo tipo de inset en su capa de compatibilidad de Android 11+, así que
    // ocultar las barras podía bloquear también el teclado (visto en Memor11/Android 13;
    // en Memor10/Android 10 no pasaba porque ahí esas flags son la API nativa real, sin
    // traducción de por medio). WindowInsetsControllerCompat separa systemBars() de ime(),
    // así que solo afecta a las barras y el teclado sigue funcionando en todas las versiones.
    private void ocultarBarras() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE: mismo comportamiento que IMMERSIVE_STICKY
        // (deslizar revela las barras temporalmente, luego se ocultan solas).
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

    // ocultarBarras() la oculta pero el swipe hacia abajo la revela igualmente (a propósito,
    // para poder deslizar el dedo). Esto va más allá: bloquea el desplegable de notificaciones
    // y ajustes rápidos a nivel de sistema, ni el swipe lo saca. API del SDK Datalogic (>=1.31),
    // no de Android, así que funciona igual en Memor10/Android 10 y Memor11/Android 13 — no
    // depende de la versión de Android, depende del firmware Datalogic. Throwable, no Exception:
    // en un equipo no-Datalogic o con firmware antiguo sin esta clase, la JVM lanza
    // NoClassDefFoundError/NoSuchMethodError, que no son Exception.
    private void bloquearBarraNotificaciones() {
        try {
            new AppManager(this).setStatusBarDisabled(true);
        } catch (Throwable e) {
            Log.e("PDA", "No se pudo bloquear la barra de notificaciones (¿no es Datalogic o SDK antiguo?)", e);
        }
    }

    public class AndroidBridge {

        private final String androidId;

        AndroidBridge(String id) {
            this.androidId = id;
        }

        @JavascriptInterface
        public String getPdaId() {
            return androidId;
        }

        @JavascriptInterface
        public String getSerialNumber() {
            return obtenerNumeroSerie();
        }

        // Número de build instalado (PackageInfo.getLongVersionCode), para que el HTML lo
        // compare contra el "build_number" del JSON de actualizaciones y decida si mostrar
        // el popup de "hay versión nueva".
        @JavascriptInterface
        public String getBuildNumber() {
            try {
                return String.valueOf(getPackageManager()
                        .getPackageInfo(getPackageName(), 0).getLongVersionCode());
            } catch (PackageManager.NameNotFoundException e) {
                return "0";
            }
        }

        // Versión legible ("1.4.3"), para mostrarla en pequeño en la pantalla de login.
        @JavascriptInterface
        public String getVersionName() {
            return obtenerVersionApp();
        }

        // Comprueba si hay actualización disponible por HTTP nativo, no por fetch() del
        // WebView: el servidor de actualizaciones (puerto 5000) no está pensado para que lo
        // consulte un navegador (lo usa nativamente com.aza.scanner) y probablemente no manda
        // cabeceras CORS — un fetch() ahí fallaría en silencio. HttpURLConnection no pasa por
        // CORS (es una restricción exclusiva de navegador/WebView). Corre en un hilo aparte
        // para no bloquear el hilo de JS, y avisa al terminar llamando a
        // recibirInfoActualizacion(json) en la página.
        // mostrarFeedback=true (botón manual de la esquina del login): informa por Toast del
        // error concreto si algo falla, en vez de quedarse en silencio como en la comprobación
        // automática al cargar la pantalla — es lo que hace falta para diagnosticar por qué
        // no llega el popup (CORS, timeout, servidor caído, JSON mal formado, etc.).
        @JavascriptInterface
        public void comprobarActualizacionNativo(boolean mostrarFeedback) {
            new Thread(() -> {
                String json = "";
                String error = null;
                java.net.HttpURLConnection conn = null;
                try {
                    java.net.URL url = new java.net.URL(URL_ACTUALIZACION);
                    conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestMethod("GET");
                    int codigo = conn.getResponseCode();
                    if (codigo == 200) {
                        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                        InputStream is = conn.getInputStream();
                        byte[] buffer = new byte[1024];
                        int leido;
                        while ((leido = is.read(buffer)) != -1) baos.write(buffer, 0, leido);
                        is.close();
                        json = baos.toString("UTF-8");
                        // Quitar el BOM UTF-8 (bytes EF BB BF, U+FEFF al decodificar) si el
                        // fichero se guardó con él delante de la '{' — invisible al
                        // copiar/pegar, pero JSON.parse() lo rechaza como "unexpected token"
                        // al no ser JSON válido con eso delante.
                        final char BOM = (char) 0xFEFF;
                        if (!json.isEmpty() && json.charAt(0) == BOM) json = json.substring(1);
                    } else {
                        error = "HTTP " + codigo;
                    }
                } catch (Exception e) {
                    Log.e("PDA", "Error comprobando actualización", e);
                    error = e.getClass().getSimpleName() + ": " + e.getMessage();
                } finally {
                    if (conn != null) conn.disconnect();
                }
                String jsonEscapado = json
                        .replace("\\", "\\\\")
                        .replace("'", "\\'")
                        .replace("\n", "")
                        .replace("\r", "");
                String errorFinal = error;
                runOnUiThread(() -> {
                    if (mostrarFeedback && errorFinal != null) {
                        Toast.makeText(MainActivity.this, "Error comprobando actualización: " + errorFinal, Toast.LENGTH_LONG).show();
                    }
                    webView.evaluateJavascript(
                            "recibirInfoActualizacion('" + jsonEscapado + "', " + mostrarFeedback + ")", null);
                });
            }).start();
        }

        // Descarga el APK de la URL dada con DownloadManager y, al terminar, abre el
        // instalador del sistema (ver registrarReceptorDescarga). No instala en silencio: el
        // usuario tiene que confirmar la instalación en el diálogo estándar de Android.
        @JavascriptInterface
        public void descargarActualizacion(String url) {
            runOnUiThread(() -> {
                try {
                    File destino = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "actualizacion.apk");
                    if (destino.exists()) destino.delete();
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationUri(Uri.fromFile(destino));
                    DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    descargaActualizacionId = dm.enqueue(request);
                    Toast.makeText(MainActivity.this, "Descargando actualización…", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("PDA", "Error iniciando la descarga de la actualización", e);
                    Toast.makeText(MainActivity.this, "No se pudo iniciar la descarga", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Genera el código de barras (Code128) del texto recibido y lo devuelve como PNG
        // en base64, para que el HTML lo pinte directamente en un <img src="data:...">
        // sin necesitar ninguna librería de generación de barcodes en el lado JS.
        @JavascriptInterface
        public String generarBarcodeBase64(String texto) {
            try {
                Bitmap bmp = BarcodeUtil.generarCode128(texto, 600, 130);
                if (bmp == null) return "";
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            } catch (Exception e) {
                Log.e("PDA", "Error generarBarcodeBase64", e);
                return "";
            }
        }

        @JavascriptInterface
        public void abrirApp(String packageName) {
            abrirAppExterna(packageName);
        }

        // Walkie es una Activity interna de este mismo módulo (com.azalogistics.walkie),
        // no un paquete instalado aparte: por eso necesita su propio método en vez de
        // reutilizar abrirApp()/abrirAppExterna(), que solo lanzan apps externas por packageName.
        // El WebView no tiene soporte multi-ventana (onCreateWindow); un window.open()
        // con una URL normal navega el propio WebView fuera de index.html, matando el
        // polling de alertas/heartbeat hasta volver al menú. Se abre en un WebView propio
        // aparte (ExternalWebActivity) en vez del navegador del sistema (Chrome): en estos
        // PDA, Chrome da "website access denied... ask your administrator" al pedir
        // permisos de sitio, por alguna política del perfil/dispositivo — con nuestro
        // propio WebView no pasa por esa restricción, y de paso el WebView principal se
        // queda vivo en segundo plano igual que con Whales/Scan/etc.
        @JavascriptInterface
        public void abrirNavegador(String url) {
            runOnUiThread(() -> {
                try {
                    Intent intent = new Intent(MainActivity.this, ExternalWebActivity.class);
                    intent.putExtra(ExternalWebActivity.EXTRA_URL, url);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("PDA", "Error abrirNavegador: " + url, e);
                }
            });
        }

        @JavascriptInterface
        public void cerrarNavegador() {
            runOnUiThread(ExternalWebActivity::cerrarSiAbierta);
        }

        @JavascriptInterface
        public void abrirWalkie() {
            runOnUiThread(() -> {
                try {
                    Intent intent = new Intent(MainActivity.this, com.azalogistics.walkie.ui.MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("PDA", "Error abrirWalkie", e);
                }
            });
        }

        // ✅ NUEVO: método que faltaba y el HTML ya llamaba
        @JavascriptInterface
        public void cerrarApp(String packageName) {
            try {
                // Volver a primer plano de nuestra app
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("PDA", "Error cerrarApp: " + packageName, e);
            }
        }

        @JavascriptInterface
        public void activarAlerta() {
            runOnUiThread(() -> {
                try {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        setShowWhenLocked(true);
                        setTurnScreenOn(true);
                    } else {
                        getWindow().addFlags(
                                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                        android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        );
                    }

                    getWindow().addFlags(
                            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    );

                    iniciarSonidoAlertaNativo();
                } catch (Exception e) {
                    Log.e("PDA", "Error activar alerta modo", e);
                }
            });
        }

        @JavascriptInterface
        public void desactivarAlerta() {
            runOnUiThread(() -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        setShowWhenLocked(false);
                        setTurnScreenOn(false);
                    } else {
                        getWindow().clearFlags(
                                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                        android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        );
                    }

                    getWindow().clearFlags(
                            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    );

                    detenerSonidoAlertaNativo();
                } catch (Exception e) {
                    Log.e("PDA", "Error desactivar alerta modo", e);
                }
            });
        }

        @JavascriptInterface
        public void solicitarPinAdmin() {
            runOnUiThread(() -> mostrarDialogoPin());
        }

        @JavascriptInterface
        public void abrirAjustesPantalla() {
            runOnUiThread(() -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("PDA", "Error abrirAjustesPantalla", e);
                }
            });
        }

        @JavascriptInterface
        public void setVolumenMaximo() {
            AudioManager am =
                    (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                volumenAnterior = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
            }
        }

        @JavascriptInterface
        public void restaurarVolumen() {
            if (volumenAnterior < 0) return;
            AudioManager am =
                    (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, volumenAnterior, 0);
                volumenAnterior = -1;
            }
        }
    }

    private void mostrarDialogoPin() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final EditText inputUsuario = new EditText(this);
        inputUsuario.setHint("Usuario");
        layout.addView(inputUsuario);

        final EditText inputPin = new EditText(this);
        inputPin.setHint("PIN");
        inputPin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(inputPin);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Acceso de administrador");
        builder.setView(layout);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String usuario = inputUsuario.getText().toString().trim();
            String pin = inputPin.getText().toString().trim();
            verificarAdminEnServidor(usuario, pin);
        });

        builder.setNegativeButton("Cancelar", (d, w) -> d.cancel());
        builder.show();
    }

    private void mostrarMenuAdmin() {
        String[] opciones = {"Cambiar entorno", "Información del dispositivo", "Scan2Deploy", "Scan", "Walkie", "Ajustes de Android", "Bloquear ajustes", "Cambiar launcher", "Salir de la app"};

        new AlertDialog.Builder(this)
                .setTitle("Menú de administrador (v" + obtenerVersionApp() + ")")
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            mostrarSelectorEntorno();
                            break;
                        case 1:
                            mostrarDialogoInfoDispositivo();
                            break;
                        case 2:
                            abrirAppExterna("com.datalogic.scan2deploy");
                            break;
                        case 3:
                            abrirAppExterna("com.aza.scanner");
                            break;
                        case 4:
                            startActivity(new Intent(this, com.azalogistics.walkie.ui.MainActivity.class));
                            break;
                        case 5:
                            abrirAjustesAndroid();
                            break;
                        case 6:
                            mostrarDialogoBloqueoAcceso();
                            break;
                        case 7:
                            cambiarLauncher();
                            break;
                        case 8:
                            salirDeLaApp();
                            break;
                    }
                })
                .setNegativeButton("Cancelar", (d, w) -> d.cancel())
                .show();
    }

    // Limpia la preferencia de "usar esta app siempre" para HOME y fuerza el selector nativo
    // de launcher. No desactiva ni desinstala nada, así que es reversible: se puede volver a
    // elegir esta app en cualquier momento, desde el propio selector o desde Ajustes > Apps
    // predeterminadas.
    private void cambiarLauncher() {
        try {
            getPackageManager().clearPackagePreferredActivities(getPackageName());
        } catch (Exception e) {
            Log.e("PDA", "Error limpiando preferencia de launcher", e);
        }
        try {
            Intent intentHome = new Intent(Intent.ACTION_MAIN);
            intentHome.addCategory(Intent.CATEGORY_HOME);
            // Un startActivity() normal aquí podía no hacer nada visible: como esta misma
            // app ya está en primer plano satisfaciendo el intent de HOME, Android a veces
            // resuelve en silencio de vuelta a la misma tarea en vez de ofrecer elegir.
            // createChooser() fuerza siempre el diálogo de selección, sin ese atajo.
            Intent selector = Intent.createChooser(intentHome, "Elegir launcher");
            selector.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(selector);
        } catch (Exception e) {
            Log.e("PDA", "Error abriendo selector de launcher", e);
            Toast.makeText(this, "No se pudo abrir el selector de launcher", Toast.LENGTH_LONG).show();
        }
    }

    // Antes vivía como un Switch en la pantalla de Walkie (SettingsLockToggle); movido aquí
    // para que solo un admin con PIN pueda tocarlo. AjustesBloqueo (SharedPreferences) es el
    // valor persistido; SettingsLockManager es el flag en memoria que lee en vivo
    // PttAccessibilityService, así que hay que actualizar los dos a la vez.
    private void mostrarDialogoBloqueoAcceso() {
        boolean activo = AjustesBloqueo.estaActivo(this);
        new AlertDialog.Builder(this)
                .setTitle("Bloquear acceso a Ajustes")
                .setMessage(activo
                        ? "El bloqueo está ACTIVO: no se puede entrar a los Ajustes del sistema.\n\n¿Desactivarlo?"
                        : "El bloqueo está DESACTIVADO: cualquiera puede entrar a los Ajustes del sistema.\n\n¿Activarlo?")
                .setPositiveButton(activo ? "Desactivar" : "Activar", (dialog, which) -> {
                    boolean nuevoValor = !activo;
                    AjustesBloqueo.setActivo(this, nuevoValor);
                    SettingsLockManager.INSTANCE.setEnabled(nuevoValor);
                    Toast.makeText(this, nuevoValor ? "Bloqueo activado" : "Bloqueo desactivado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (d, w) -> d.cancel())
                .show();
    }

    private void abrirAjustesAndroid() {
        try {
            SettingsLockManager.INSTANCE.setEnabled(false);
            bloqueoAjustesSuspendidoPorAdmin = true;
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("PDA", "Error abriendo ajustes de Android", e);
            Toast.makeText(this, "No se pudo abrir Ajustes", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarSelectorEntorno() {
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        radioGroup.setPadding(padding, padding, padding, padding);

        RadioButton rbProduccion = new RadioButton(this);
        rbProduccion.setId(View.generateViewId());
        rbProduccion.setText("Producción");

        RadioButton rbDesarrollo = new RadioButton(this);
        rbDesarrollo.setId(View.generateViewId());
        rbDesarrollo.setText("Desarrollo");

        radioGroup.addView(rbProduccion);
        radioGroup.addView(rbDesarrollo);
        radioGroup.check(ServerConfig.isDesarrollo(this) ? rbDesarrollo.getId() : rbProduccion.getId());

        new AlertDialog.Builder(this)
                .setTitle("Cambiar entorno")
                .setView(radioGroup)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    boolean desarrollo = radioGroup.getCheckedRadioButtonId() == rbDesarrollo.getId();
                    if (desarrollo) {
                        ServerConfig.setDesarrollo(this);
                        Toast.makeText(this, "Modo Desarrollo activado", Toast.LENGTH_SHORT).show();
                    } else {
                        ServerConfig.setProduccion(this);
                        Toast.makeText(this, "Modo Producción activado", Toast.LENGTH_SHORT).show();
                    }
                    cargarWebConOffline();
                })
                .setNegativeButton("Cancelar", (d, w) -> d.cancel())
                .show();
    }

    private void salirDeLaApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Sonido de alerta nativo: el audio del WebView (AudioContext en JS) requiere
    // un gesto directo del usuario para sonar, y la alerta se activa sola vía
    // location.reload(), sin ningún toque previo — por eso nunca sonaba desde JS.
    private void iniciarSonidoAlertaNativo() {
        detenerSonidoAlertaNativo();
        try {
            toneGeneratorAlerta = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        } catch (Exception e) {
            Log.e("PDA", "Error creando ToneGenerator de alerta", e);
            return;
        }

        alertaSoundRunnable = new Runnable() {
            @Override
            public void run() {
                if (toneGeneratorAlerta != null) {
                    toneGeneratorAlerta.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300);
                }
                alertaSoundHandler.postDelayed(this, 400);
            }
        };
        alertaSoundHandler.post(alertaSoundRunnable);
    }

    private void detenerSonidoAlertaNativo() {
        if (alertaSoundRunnable != null) {
            alertaSoundHandler.removeCallbacks(alertaSoundRunnable);
            alertaSoundRunnable = null;
        }
        if (toneGeneratorAlerta != null) {
            toneGeneratorAlerta.release();
            toneGeneratorAlerta = null;
        }
    }

    private String obtenerVersionApp() {
        try {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "?";
        }
    }

    private String obtenerInfoDispositivo() {
        return "Modelo: " + Build.MODEL
                + "\nAndroid: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"
                + "\nNúmero de serie: " + obtenerNumeroSerie();
    }

    private void mostrarDialogoInfoDispositivo() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        android.widget.TextView texto = new android.widget.TextView(this);
        texto.setText(obtenerInfoDispositivo());
        layout.addView(texto);

        Bitmap barcode = BarcodeUtil.generarCode128(obtenerNumeroSerie(),
                (int) (280 * getResources().getDisplayMetrics().density),
                (int) (70 * getResources().getDisplayMetrics().density));
        if (barcode != null) {
            ImageView imagen = new ImageView(this);
            imagen.setImageBitmap(barcode);
            imagen.setPadding(0, padding, 0, 0);
            layout.addView(imagen);
        }

        new AlertDialog.Builder(this)
                .setTitle("Información del dispositivo")
                .setView(layout)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private String obtenerNumeroSerie() {
        try {
            String serial = com.datalogic.device.info.SYSTEM.SERIAL_NUMBER;
            if (serial != null && !serial.trim().isEmpty()) {
                return serial;
            }
        } catch (Throwable e) {
            // Throwable, no Exception: en dispositivos que no son Datalogic (o con SDK/firmware
            // antiguo sin este campo) la JVM puede lanzar NoClassDefFoundError/NoSuchFieldError,
            // que no son Exception. Se cae a ANDROID_ID en cualquiera de esos casos.
            Log.e("PDA", "No se pudo leer el serial Datalogic, usando ANDROID_ID", e);
        }
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void abrirAppExterna(String packageName) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

            if (intent == null) {
                Log.e("PDA", "No existe launcher para " + packageName);
                Toast.makeText(this, "No existe launcher para " + packageName, Toast.LENGTH_LONG).show();
                return;
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.i("PDA", "Abriendo " + packageName);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("PDA", "Error abrirApp: " + packageName, e);
        }
    }

    private void verificarAdminEnServidor(String usuario, String pin) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                        ServerConfig.getApiBaseUrl(MainActivity.this) + "/usuarios/?usuario="
                                + java.net.URLEncoder.encode(usuario, "UTF-8"));
                java.net.HttpURLConnection conn =
                        (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int code = conn.getResponseCode();
                boolean esAdmin = false;

                if (code == 200) {
                    String respuesta = leerRespuesta(conn.getInputStream());
                    org.json.JSONArray usuarios = new org.json.JSONArray(respuesta);
                    if (usuarios.length() > 0) {
                        org.json.JSONObject user = usuarios.getJSONObject(0);
                        String pinServidor = user.optString("pin", "");
                        String extraField1 = user.optString("extraField1", "");
                        esAdmin = pinServidor.equals(pin) && "Admin".equals(extraField1);
                    }
                }

                boolean esAdminFinal = esAdmin;
                runOnUiThread(() -> {
                    if (esAdminFinal) {
                        mostrarMenuAdmin();
                    } else {
                        Toast.makeText(this, "Usuario o PIN incorrecto, o sin permisos de administrador", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                // No se pudo contactar con el servidor (sin red, host interno inalcanzable,
                // timeout...): se admite el PIN de emergencia offline como alternativa.
                runOnUiThread(() -> {
                    if (PIN_EMERGENCIA_OFFLINE.equals(pin)) {
                        mostrarMenuAdmin();
                    } else {
                        Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private String leerRespuesta(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int leido;
        while ((leido = is.read(buffer)) != -1) {
            baos.write(buffer, 0, leido);
        }
        return baos.toString("UTF-8");
    }

    // "Recargar página" hace webView.loadUrl() cada vez (no location.reload() en JS),
    // así que cada pulsación apila una entrada nueva en el historial del WebView aunque
    // sea la misma URL. Antes, "atrás" navegaba ese historial con goBack() y podía volver
    // a una carga anterior a haber iniciado sesión sin pasar nunca por el logout() real
    // (que avisa a /logs/logout) — la sesión quedaba huérfana en el backend aunque en
    // pantalla pareciera deslogueado. Con sesión activa, "atrás" ahora dispara ese
    // logout() real vía JS en lugar de navegar el historial.
    // Con targetSdk 36 en Android 16 el sistema ya no llama a onBackPressed() (gesto atrás
    // predictivo): hay que registrar un callback en el dispatcher (API 33+) que reutilice esta
    // misma lógica. En versiones anteriores sigue funcionando el override de abajo.
    private void registrarGestoAtras() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::onBackPressed);
        }
    }

    @Override
    public void onBackPressed() {
        webView.evaluateJavascript(
                "(function(){"
                        + "if (typeof USUARIO_ACTUAL !== 'undefined' && USUARIO_ACTUAL) { logout(); return true; }"
                        + "return false;"
                        + "})();",
                resultado -> {
                    if (!"true".equals(resultado)) {
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            super.onBackPressed();
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;

            Uri[] results = null;

            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    results = new Uri[]{data.getData()};
                } else if (photoUri != null) {
                    // ✅ NUEVO: verificar que la foto tiene contenido antes de usarla
                    try {
                        InputStream is = getContentResolver().openInputStream(photoUri);
                        if (is != null && is.available() > 0) {
                            is.close();
                            results = new Uri[]{photoUri};
                            GaleriaUtil.guardar(this, photoUri);
                        }
                    } catch (Exception e) {
                        Log.w("PDA", "Foto cancelada o fichero vacío");
                    }
                }
            }

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

}