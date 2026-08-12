package com.azalogistics.pda;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Estado de sesión del usuario logueado en el WebView, compartido entre MainActivity
 * (quien lo escribe, avisado por JS en login/logout) y WalkieService (quien lo lee cada
 * 30s para decidir si manda heartbeat nativo). Vía SharedPreferences porque son dos
 * clases en paquetes/lenguajes distintos (Java/Kotlin) sin otra relación entre ellas.
 */
public final class PdaSesion {

    private static final String PREFS = "pda_sesion";
    private static final String KEY_ACTIVA = "activa";
    private static final String KEY_USUARIO = "usuario";
    private static final String KEY_NUM_PDA = "numPDA";

    private PdaSesion() {
    }

    public static void marcarActiva(Context context, String usuario, int numPDA) {
        prefs(context).edit()
                .putBoolean(KEY_ACTIVA, true)
                .putString(KEY_USUARIO, usuario)
                .putInt(KEY_NUM_PDA, numPDA)
                .apply();
    }

    public static void marcarInactiva(Context context) {
        prefs(context).edit().putBoolean(KEY_ACTIVA, false).apply();
    }

    public static boolean estaActiva(Context context) {
        return prefs(context).getBoolean(KEY_ACTIVA, false);
    }

    public static String usuario(Context context) {
        return prefs(context).getString(KEY_USUARIO, null);
    }

    public static int numPDA(Context context) {
        return prefs(context).getInt(KEY_NUM_PDA, 0);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
