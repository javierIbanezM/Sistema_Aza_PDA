package com.azalogistics.pda;

import android.content.Context;
import android.content.SharedPreferences;

public class ServerConfig {

    private static final String PREFS_NAME = "server_config";
    private static final String KEY_MODO_SERVIDOR = "modoServidor";

    private static final String MODO_PRODUCCION = "PRODUCCION";
    private static final String MODO_DESARROLLO = "DESARROLLO";

    private static final String URL_PRODUCCION = "http://SRVAutoN8N.zar.local:8000";
    private static final String URL_DESARROLLO = "http://SRVAutoN8N.zar.local:8002";

    // Host de la API (usuarios, login, heartbeat...), distinto del host que sirve la
    // página web (/home). Mismo puerto que el entorno activo en ambos casos.
    private static final String API_URL_PRODUCCION = "http://SRVAutoN8N.zar.local:8000";
    private static final String API_URL_DESARROLLO = "http://SRVAutoN8N.zar.local:8002";

    // Carpeta del servidor de actualizaciones (mismo servidor que versionPDA.json) donde se publican los
    // ficheros de órdenes remotas firmados: <base>/<NºSERIE>.json y <base>/todos.json (ver OrdenesRemotas).
    private static final String URL_ORDENES = "http://SRVAutoN8N.zar.local:5000/updates/ordenes";
    private static final String KEY_ORDENES_URL = "ordenesUrl";

    private ServerConfig() {
    }

    /** Base de las órdenes remotas. Se puede sustituir (p. ej. para pruebas en un servidor local). */
    public static String getOrdenesBaseUrl(Context context) {
        String personalizada = prefs(context).getString(KEY_ORDENES_URL, "");
        String url = personalizada.isEmpty() ? URL_ORDENES : personalizada;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static String getBaseUrl(Context context) {
        return isDesarrollo(context) ? URL_DESARROLLO : URL_PRODUCCION;
    }

    public static String getApiBaseUrl(Context context) {
        return isDesarrollo(context) ? API_URL_DESARROLLO : API_URL_PRODUCCION;
    }

    public static boolean isProduccion(Context context) {
        return !isDesarrollo(context);
    }

    public static boolean isDesarrollo(Context context) {
        String modo = prefs(context).getString(KEY_MODO_SERVIDOR, MODO_PRODUCCION);
        return MODO_DESARROLLO.equals(modo);
    }

    public static void setProduccion(Context context) {
        prefs(context).edit().putString(KEY_MODO_SERVIDOR, MODO_PRODUCCION).apply();
    }

    public static void setDesarrollo(Context context) {
        prefs(context).edit().putString(KEY_MODO_SERVIDOR, MODO_DESARROLLO).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
