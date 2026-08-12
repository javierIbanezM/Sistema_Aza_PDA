package com.azalogistics.walkie.ptt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-global PTT event bus. Every source of "talk now" (in-app button, notification
 * action, overlay button, AccessibilityService hardware key) funnels press()/release()
 * here, and WalkieService observes [pressed] to start/stop transmission. This decouples
 * input sources from the audio engine.
 */
object PttManager {
    @Volatile var configuredKeyCode: Int = PttKey.DEFAULT_KEYCODE

    private val _pressed = MutableStateFlow(false)
    val pressed: StateFlow<Boolean> = _pressed

    fun press() { _pressed.value = true }
    fun release() { _pressed.value = false }

    /** @return true if this key event was consumed as a PTT action. */
    fun onKeyEvent(keyCode: Int, down: Boolean): Boolean {
        if (keyCode != configuredKeyCode) return false
        if (down) press() else release()
        return true
    }
}
