package com.azalogistics.walkie.service

/**
 * Process-global mirror of AjustesBloqueo (SharedPreferences, com.azalogistics.pda), seeded by
 * WalkieService.onCreate and updated live by MainActivity.mostrarDialogoBloqueoAcceso.
 * PttAccessibilityService reads this synchronously from onAccessibilityEvent, where a
 * SharedPreferences/DataStore read on every event isn't worth it.
 */
object SettingsLockManager {
    @Volatile var enabled: Boolean = true
}
