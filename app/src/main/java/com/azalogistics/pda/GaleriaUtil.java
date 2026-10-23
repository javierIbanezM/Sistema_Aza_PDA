package com.azalogistics.pda;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copia las fotos hechas con la cámara a la galería del sistema (MediaStore, Pictures/PDA).
 * La foto original queda en getExternalFilesDir(), que es privada de la app y la galería no ve:
 * sin esta copia el operario no la encuentra después de subirla.
 */
final class GaleriaUtil {

    private GaleriaUtil() {
    }

    /** Sin efecto antes de Android 10: ahí MediaStore no admite RELATIVE_PATH. */
    static void guardar(Context context, Uri imagen) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PDA");

        try {
            Uri destino = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (destino == null) return;

            try (InputStream in = resolver.openInputStream(imagen);
                 OutputStream out = resolver.openOutputStream(destino)) {
                if (in == null || out == null) return;
                byte[] buffer = new byte[8192];
                int leidos;
                while ((leidos = in.read(buffer)) != -1) {
                    out.write(buffer, 0, leidos);
                }
            }
        } catch (Exception e) {
            Log.e("PDA", "Error guardando la foto en la galería", e);
        }
    }
}
