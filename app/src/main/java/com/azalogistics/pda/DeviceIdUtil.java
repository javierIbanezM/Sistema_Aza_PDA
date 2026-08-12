package com.azalogistics.pda;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * Mismo criterio que usa el JS (getDeviceId() en index.html/indexDev.html, ya idénticos):
 * siempre el número de serie Datalogic, con fallback a ANDROID_ID solo si no es un
 * dispositivo Datalogic (equipos de pruebas). Sin distinción producción/desarrollo — los
 * PDA se identifican por su SN en ambos entornos. Centralizado aquí para que el heartbeat
 * nativo de WalkieService use exactamente el mismo id que el resto de la app.
 */
public final class DeviceIdUtil {

    private DeviceIdUtil() {
    }

    public static String obtenerAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String obtenerNumeroSerie(Context context) {
        try {
            String serial = com.datalogic.device.info.SYSTEM.SERIAL_NUMBER;
            if (serial != null && !serial.trim().isEmpty()) {
                return serial;
            }
        } catch (Throwable e) {
            Log.e("PDA", "No se pudo leer el serial Datalogic, usando ANDROID_ID", e);
        }
        return obtenerAndroidId(context);
    }

    public static String obtenerDeviceId(Context context) {
        return obtenerNumeroSerie(context);
    }
}
