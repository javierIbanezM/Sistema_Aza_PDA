package com.azalogistics.pda;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Bloqueo de acceso restringido (antes "Bloquear acceso a Ajustes" en la pantalla de
 * Walkie, ahora gestionado desde el panel admin nativo — ver MainActivity.
 * mostrarDialogoBloqueoAcceso). Activado por defecto. SharedPreferences porque
 * PttAccessibilityService (Kotlin, otro paquete) lo consulta vía SettingsLockManager.
 */
public final class AjustesBloqueo {

    private static final String PREFS = "pda_bloqueo";
    private static final String KEY_ACTIVO = "activo";

    private AjustesBloqueo() {
    }

    public static boolean estaActivo(Context context) {
        return prefs(context).getBoolean(KEY_ACTIVO, true);
    }

    public static void setActivo(Context context, boolean activo) {
        prefs(context).edit().putBoolean(KEY_ACTIVO, activo).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
