package com.azalogistics.pda;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

/** Generación de códigos de barras (Code128: admite todo ASCII, sin restricción de
 *  mayúsculas/símbolos como Code39) para mostrar el ID del dispositivo en pantalla,
 *  de forma que se pueda escanear directamente con el lector del propio PDA. */
final class BarcodeUtil {

    private BarcodeUtil() {
    }

    static Bitmap generarCode128(String texto, int width, int height) {
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(texto, BarcodeFormat.CODE_128, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
