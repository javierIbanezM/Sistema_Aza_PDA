package com.azalogistics.pda;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

/**
 * Mini-navegador interno para CD Muelles/ActivaLink (ver AndroidBridge.abrirNavegador).
 * No usa el navegador del sistema (Chrome): en estos PDA, Chrome da "website access
 * denied... ask your administrator" al pedir permisos de sitio, por alguna política del
 * perfil/dispositivo — con nuestro propio WebView no pasa por esa restricción. Al ser una
 * Activity aparte, la principal (con el polling de alertas) se queda viva en segundo plano,
 * igual que ya pasa con Whales/Scan.
 */
public class ExternalWebActivity extends Activity {

    public static final String EXTRA_URL = "url";
    private static final int FILE_CHOOSER_REQUEST = 2000;

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        webView.setWebChromeClient(new WebChromeClient() {
            // Auto-concede geolocalización sin diálogo nativo, igual que MainActivity —
            // ese diálogo es justo lo que Chrome bloquea con "ask your administrator".
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                             GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            // Auto-concede cámara/micrófono a getUserMedia() (p.ej. escaneo por cámara en
            // "Carga paquetería"), igual que con la geolocalización: el permiso de Android
            // (CAMERA/RECORD_AUDIO) ya se pidió al usuario en MainActivity, pero WebView
            // deniega estos permisos a nivel de página por defecto salvo que se concedan aquí.
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }

            // Sin esto, un <input type="file"> (p.ej. "subir imagen" en CD Muelles) no
            // hace nada al tocarlo — el WebView no abre ningún selector por sí solo.
            // Mismo patrón que MainActivity: cámara nueva o elegir de galería.
            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                ExternalWebActivity.this.filePathCallback = filePathCallback;

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PDA");
                    if (!dir.exists()) dir.mkdirs();
                    File photoFile = new File(dir, "IMG_" + System.currentTimeMillis() + ".jpg");
                    photoUri = FileProvider.getUriForFile(
                            ExternalWebActivity.this,
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

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null) {
            finish();
            return;
        }

        // Solo se permite navegar dentro del mismo host con el que se abrió esta pantalla
        // (login normal, redirecciones internas del propio portal): cualquier otro destino
        // se bloquea, para que esto no se convierta en un navegador de propósito general.
        String hostPermitido = anfitrionDe(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String destino) {
                String host = anfitrionDe(destino);
                if (hostPermitido != null && hostPermitido.equals(host)) {
                    return false;
                }
                Log.w("PDA", "ExternalWebActivity: navegación bloqueada fuera de " + hostPermitido + " -> " + destino);
                return true;
            }
        });

        webView.loadUrl(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != FILE_CHOOSER_REQUEST) return;
        if (filePathCallback == null) return;

        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                results = new Uri[]{data.getData()};
            } else if (photoUri != null) {
                try {
                    InputStream is = getContentResolver().openInputStream(photoUri);
                    if (is != null && is.available() > 0) {
                        is.close();
                        results = new Uri[]{photoUri};
                    }
                } catch (Exception e) {
                    Log.w("PDA", "Foto cancelada o fichero vacío");
                }
            }
        }

        filePathCallback.onReceiveValue(results);
        filePathCallback = null;
    }

    private static String anfitrionDe(String url) {
        try {
            String host = Uri.parse(url).getHost();
            return host != null ? host.toLowerCase(Locale.ROOT) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
