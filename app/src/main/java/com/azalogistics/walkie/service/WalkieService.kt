package com.azalogistics.walkie.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.azalogistics.walkie.App
import com.azalogistics.pda.AjustesBloqueo
import com.azalogistics.pda.R
import com.azalogistics.walkie.domain.model.Channel
import com.azalogistics.walkie.domain.model.ConnectionState
import com.azalogistics.walkie.domain.model.TxState
import com.azalogistics.walkie.net.NetworkManager
import com.azalogistics.walkie.ptt.PttManager
import com.azalogistics.walkie.ui.MainActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps audio/network alive in background and with screen off.
 * Holds a partial wake lock (audio/presence is unicast UDP now, so no MulticastLock is
 * needed — see [NetworkManager]). It reconciles channel selection from the repository,
 * drives transmit from [PttManager], and publishes
 * state to [WalkieRuntime].
 */
class WalkieService : Service() {

    // Sin CoroutineExceptionHandler, cualquier excepción no capturada de net.start() (crear el
    // AudioTrack, NSD...) tumbaba el proceso entero — incluido el WebView principal del PDA.
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default +
            CoroutineExceptionHandler { _, e ->
                Log.e(TAG, "Error no controlado en WalkieService", e)
                WalkieRuntime.update { it.copy(connection = ConnectionState.NO_NETWORK) }
            }
    )
    private lateinit var net: NetworkManager
    private var wakeLock: PowerManager.WakeLock? = null
    private var volumeReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        try {
            startForegroundWithNotification()
        } catch (e: Exception) {
            // Android 14+: un servicio de tipo micrófono lanza SecurityException sin
            // RECORD_AUDIO concedido, o ForegroundServiceStartNotAllowedException si se
            // arrancó desde segundo plano (p.ej. BOOT_COMPLETED). Mejor parar el servicio
            // que cerrar la app; se vuelve a arrancar desde MainActivity con el permiso ya dado.
            Log.w(TAG, "No se pudo pasar a primer plano; se detiene WalkieService", e)
            stopSelf()
            return
        }
        acquireWakeLock()
        enforceMaxVolume()
        // Estado persistido gestionado ahora desde el panel admin nativo (ver
        // MainActivity.mostrarDialogoBloqueoAcceso), no desde un toggle en esta pantalla.
        SettingsLockManager.enabled = AjustesBloqueo.estaActivo(applicationContext)

        val app = applicationContext as App
        scope.launch {
            val deviceId = app.settings.ensureDeviceId()
            val displayName = app.settings.displayName.first()
            PttManager.configuredKeyCode = app.settings.pttKeyCode.first()

            net = NetworkManager(
                context = applicationContext,
                deviceId = deviceId,
                displayName = displayName,
                onState = { active ->
                    WalkieRuntime.update {
                        it.copy(connection = if (active) ConnectionState.CONNECTED else ConnectionState.NO_NETWORK)
                    }
                },
                onPeers = { peers -> WalkieRuntime.update { it.copy(peers = peers) } },
                onReceivingChanged = { ch -> WalkieRuntime.update { it.copy(receivingChannelIds = ch) } }
            )
            WalkieRuntime.update { it.copy(connection = ConnectionState.CONNECTING) }
            net.start()

            // Reconcile listen channels + tx channel whenever settings change.
            launch {
                combine(
                    app.channelRepository.channels,
                    app.channelRepository.listenChannelIds,
                    app.channelRepository.txChannelId
                ) { channels, listening, txId ->
                    Triple(channels, listening, txId)
                }.collect { (channels, listening, txId) ->
                    val listenChannels = channels.filter { it.id in listening }
                    net.setListenChannels(listenChannels)
                    val txChannel: Channel? = channels.firstOrNull { it.id == txId }
                    net.setTxChannel(txChannel)
                    WalkieRuntime.update {
                        it.copy(listenChannelIds = listening, txChannelId = txId)
                    }
                }
            }

            // Drive transmit from the PTT bus.
            launch {
                PttManager.pressed.collect { pressed ->
                    if (pressed) net.startTransmit() else net.stopTransmit()
                    WalkieRuntime.update {
                        it.copy(tx = if (pressed) TxState.TRANSMITTING else TxState.IDLE)
                    }
                    updateNotification(pressed)
                }
            }

            // Renew the wake lock before its safety-cap timeout expires, so a PDA left
            // running for days doesn't silently lose it mid-session and let the CPU sleep.
            launch {
                while (isActive) {
                    delay(6 * 60 * 60 * 1000L)
                    runCatching { wakeLock?.acquire(8 * 60 * 60 * 1000L) }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PTT_DOWN -> PttManager.press()
            ACTION_PTT_UP -> PttManager.release()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if (::net.isInitialized) net.stop()
        releaseWakeLock()
        volumeReceiver?.let { unregisterReceiver(it) }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --- foreground notification ---

    private fun startForegroundWithNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // IMPORTANCE_NONE (no LOW): silencia el canal por defecto desde la primera
            // instalación sin depender de POST_NOTIFICATIONS (permiso de Android 13+, no
            // existe en 8-12). El foreground service sigue funcionando igual aunque la
            // notificación no se muestre — Android no exige que sea visible, solo que
            // exista el canal y se llame a startForeground().
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_NONE)
            )
        }
        startForeground(NOTIF_ID, buildNotification(false))
    }

    private fun updateNotification(transmitting: Boolean) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(transmitting))
    }

    private fun buildNotification(transmitting: Boolean): Notification {
        // Sin setContentIntent a propósito: antes tocar la notificación abría
        // walkie.ui.MainActivity directamente, saltándose el PIN de admin que protege
        // el acceso a Walkie desde el menú del PDA (mantener pulsado "Recargar página").
        // Android no permite ocultar del todo la notificación de un foreground service
        // activo (ni con IMPORTANCE_NONE), así que la vía real de cerrar el hueco de
        // seguridad es que no navegue a ningún sitio al tocarla, no que sea invisible.
        val pttDown = serviceAction(ACTION_PTT_DOWN, 1)
        val pttUp = serviceAction(ACTION_PTT_UP, 2)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(if (transmitting) "Transmitting…" else "Listening")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_btn_speak_now, "Talk", pttDown)
            .addAction(android.R.drawable.ic_media_pause, "Stop talk", pttUp)
            .build()
    }

    private fun serviceAction(action: String, req: Int): PendingIntent =
        PendingIntent.getService(
            this, req, Intent(this, WalkieService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "walkielan:audio").apply {
            setReferenceCounted(false)
            acquire(8 * 60 * 60 * 1000L) // safety cap; actively renewed below every 6h
        }
    }

    private fun releaseWakeLock() {
        runCatching { wakeLock?.release() }
        wakeLock = null
    }

    private fun enforceMaxVolume() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        fun setMax() { am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0) }
        setMax()
        // Escucha solo el cambio de volumen real (AudioManager.VOLUME_CHANGED_ACTION), no
        // un ContentObserver sobre Settings.System.CONTENT_URI completo — evita despertar
        // el proceso ante cualquier ajuste del sistema no relacionado (brillo, timeout...).
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) == AudioManager.STREAM_MUSIC) {
                    setMax()
                }
            }
        }
        // Con targetSdk >= 34, registerReceiver sin flag de exportación lanza SecurityException.
        // NOT_EXPORTED sigue recibiendo los broadcasts que envía el propio sistema.
        try {
            ContextCompat.registerReceiver(
                this, receiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            volumeReceiver = receiver
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo registrar el receptor de volumen", e)
        }
    }

    companion object {
        private const val TAG = "WalkieService"
        private const val CHANNEL_ID = "walkie_fgs"
        private const val NOTIF_ID = 1001
        const val ACTION_PTT_DOWN = "com.azalogistics.walkie.PTT_DOWN"
        const val ACTION_PTT_UP = "com.azalogistics.walkie.PTT_UP"
        const val ACTION_STOP = "com.azalogistics.walkie.STOP"

        fun start(context: Context) {
            // Android 14+ exige RECORD_AUDIO ya concedido para un servicio de tipo micrófono:
            // MainActivity vuelve a llamar aquí desde onRequestPermissionsResult.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "RECORD_AUDIO sin conceder: WalkieService no se arranca todavía")
                return
            }
            val i = Intent(context, WalkieService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
                else context.startService(i)
            } catch (e: Exception) {
                // Arranque desde segundo plano (BOOT_COMPLETED en Android 15+, etc.).
                Log.w(TAG, "No se pudo arrancar WalkieService", e)
            }
        }
        fun stop(context: Context) = context.stopService(Intent(context, WalkieService::class.java))
    }
}
