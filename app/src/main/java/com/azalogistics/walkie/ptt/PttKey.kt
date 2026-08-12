package com.azalogistics.walkie.ptt

import android.view.KeyEvent

/** Configurable hardware key used for Push-To-Talk. */
object PttKey {
    /** Volume-Up is the most commonly available physical key for PTT. */
    const val DEFAULT_KEYCODE = KeyEvent.KEYCODE_VOLUME_UP

    /** Candidate keys offered in settings. */
    val candidates: List<Pair<Int, String>> = listOf(
        KeyEvent.KEYCODE_VOLUME_UP to "Volume Up",
        KeyEvent.KEYCODE_VOLUME_DOWN to "Volume Down",
        KeyEvent.KEYCODE_CAMERA to "Camera",
        KeyEvent.KEYCODE_HEADSETHOOK to "Headset button",
    )

    fun label(code: Int): String =
        candidates.firstOrNull { it.first == code }?.second
            ?: KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_").replace('_', ' ').lowercase()
                .replaceFirstChar { it.uppercase() }
}
