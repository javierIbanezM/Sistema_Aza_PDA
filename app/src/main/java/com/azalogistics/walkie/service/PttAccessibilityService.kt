package com.azalogistics.walkie.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!SettingsLockManager.enabled) return
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.packageName?.toString() != settingsPackageName) return
        // Excepción para el botón "Ajustes de pantalla" del menú (tamaño de pantalla/zoom):
        // deja pasar solo esa pantalla concreta de Settings, el resto (WiFi, apps, cuentas...)
        // sigue bloqueado. Sin verificar todavía si Android 13 (Memor11) resuelve
        // ACTION_DISPLAY_SETTINGS a esta misma clase o a una distinta — si en la Memor11 esta
        // pantalla sigue rebotando, hay que sacar el nombre real con
        // "adb shell dumpsys window windows | grep mCurrentFocus" y añadirlo aquí también.
        if (event.className?.toString() == "com.android.settings.Settings\$DisplaySettingsActivity") return

        performGlobalAction(GLOBAL_ACTION_BACK)
        startActivity(
            Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    override fun onInterrupt() { PttManager.release() }
}
