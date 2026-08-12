package com.azalogistics.walkie.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.azalogistics.walkie.ptt.PttManager
import com.azalogistics.walkie.service.PttAccessibilityService
import com.azalogistics.walkie.service.WalkieService

class MainActivity : ComponentActivity() {

    private val vm: WalkieViewModel by viewModels()
    private val accessibilityEnabled = mutableStateOf(false)
    private val batteryOptimized = mutableStateOf(false)

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermissions()
        WalkieService.start(this)
        setContent {
            WalkieApp(
                vm = vm,
                accessibilityEnabled = accessibilityEnabled.value,
                batteryOptimized = batteryOptimized.value,
                onOpenAccessibility = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onOpenBatterySettings = {
                    runCatching {
                        startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        })
                    }.onFailure {
                        startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        accessibilityEnabled.value = isAccessibilityServiceEnabled()
        batteryOptimized.value = isIgnoringBatteryOptimizations()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val target = ComponentName(this, PttAccessibilityService::class.java).flattenToString()
        return flat.split(':').any { it.equals(target, ignoreCase = true) }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    // No se pide POST_NOTIFICATIONS a propósito: se quiere que las notificaciones
    // queden desactivadas por defecto (ver también IMPORTANCE_NONE en
    // WalkieService.startForegroundWithNotification, que cubre Android 8-12, donde
    // este permiso ni existe). Sin pedirlo, en Android 13+ el sistema lo trata como
    // no concedido y el interruptor de notificaciones de la app sale desactivado.
    private fun requestRuntimePermissions() {
        val needed = buildList {
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) add(Manifest.permission.RECORD_AUDIO)
        }
        if (needed.isNotEmpty()) permLauncher.launch(needed.toTypedArray())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.repeatCount == 0) {
            if (vm.detectingPttKey.value) {
                vm.onDetectedPttKey(keyCode)
                return true
            }
            if (PttManager.onKeyEvent(keyCode, down = true)) return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (vm.detectingPttKey.value) return true
        if (PttManager.onKeyEvent(keyCode, down = false)) return true
        return super.onKeyUp(keyCode, event)
    }
}
