package com.azalogistics.walkie.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.azalogistics.walkie.ptt.PttManager
import com.azalogistics.pda.MainActivity

/**
 * OPTIONAL global hardware PTT. With `flagRequestFilterKeyEvents` (see
 * res/xml/ptt_accessibility_config.xml) this receives key events even when WalkieLAN is
 * NOT in the foreground — the only reliable way on stock Android to use a volume key as a
 * global PTT. The user must enable it manually in Settings > Accessibility.
 *
 * Limits: returning true consumes the event so the OS won't change volume while talking.
 * Some OEMs intercept volume keys earlier; behavior can vary by device.
 *
 * Also enforces the in-app "settings lock" ([SettingsLockManager]): while enabled, opening the
 * system Settings app (from anywhere — launcher, notification shade, recents) is bounced back
 * immediately via typeWindowStateChanged events. This is a best-effort UI-level block, not a
 * device-owner/MDM restriction, so a brief flash of the Settings screen is expected. Toggled
 * from the native admin panel (MainActivity.mostrarDialogoBloqueoAcceso), persisted via
 * AjustesBloqueo; SettingsLockManager is just the live in-memory flag this service reads.
 */
class PttAccessibilityService : AccessibilityService() {

    private val settingsPackageName: String by lazy {
        packageManager.resolveActivity(Intent(Settings.ACTION_SETTINGS), 0)
            ?.activityInfo?.packageName ?: "com.android.settings"
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val down = event.action == KeyEvent.ACTION_DOWN
        val up = event.action == KeyEvent.ACTION_UP
        if (!down && !up) return false
        return PttManager.onKeyEvent(event.keyCode, down)
    }

    // "Tamaño de la fuente" y "Zoom de la pantalla" no son Activities propias — las dos
    // resuelven a la misma com.android.settings.SubSettings genérica que usan muchas otras
    // pantallas de Ajustes (comprobado en el dispositivo real con
    // "adb shell dumpsys activity activities"). Bloquear/permitir solo por nombre de clase no
    // sirve ahí: dejaría pasar cualquier cosa que también use SubSettings. Como paso extra,
    // dentro de una SubSettings se exige además que el título visible en pantalla sea uno de
    // estos dos — así el buscador de Ajustes (que también vive dentro de SubSettings y deja
    // saltar a cualquier otra pantalla) no sirve para colarse, porque el destino tendría un
    // título distinto y seguiría bloqueado.
    private val titulosSubSettingsPermitidos = setOf("Tamaño de la fuente", "Zoom de la pantalla")
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!SettingsLockManager.enabled) return
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.packageName?.toString() != settingsPackageName) return

        val claseVentana = event.className?.toString()
        // Pantalla principal de "Ajustes de pantalla" (no verificado aún si Android 13/Memor11
        // resuelve ACTION_DISPLAY_SETTINGS a esta misma clase — si ahí sigue rebotando, sacar
        // el nombre real con "adb shell dumpsys activity activities" y añadirlo aquí también).
        if (claseVentana == "com.android.settings.Settings\$DisplaySettingsActivity") return

        if (claseVentana == "com.android.settings.SubSettings") {
            // Justo en el instante del cambio de ventana, el árbol de accesibilidad puede
            // no tener aún el contenido (el título) renderizado — comprobarlo al momento
            // encontraba el árbol vacío y bloqueaba siempre, aunque el título sí fuera uno
            // de los permitidos. Se da un pequeño margen antes de decidir.
            handler.postDelayed({
                if (!subSettingsConTituloPermitido()) bloquear()
            }, 300)
            return
        }

        bloquear()
    }

    private fun bloquear() {
        performGlobalAction(GLOBAL_ACTION_BACK)
        startActivity(
            Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun subSettingsConTituloPermitido(): Boolean {
        val root = rootInActiveWindow ?: return false
        return try {
            contieneTexto(root, titulosSubSettingsPermitidos)
        } finally {
            root.recycle()
        }
    }

    private fun contieneTexto(node: AccessibilityNodeInfo, textos: Set<String>): Boolean {
        if (node.text?.toString() in textos) return true
        for (i in 0 until node.childCount) {
            val hijo = node.getChild(i) ?: continue
            val encontrado = contieneTexto(hijo, textos)
            hijo.recycle()
            if (encontrado) return true
        }
        return false
    }

    override fun onInterrupt() { PttManager.release() }
}
