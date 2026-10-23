package com.azalogistics.walkie.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Owns the single output AudioTrack and the mixing loop. Pulls one frame per active
 * (channel,sender) jitter buffer each 20 ms tick, mixes with clamping, and writes.
 *
 * Mixing is additive with saturation (clamp to Short range) — safe and simple. When
 * transmitting we duck/mute to avoid acoustic feedback (set via [muted]).
 */
class AudioPlayback {

    private data class Key(val channelId: String, val senderId: String)

    private val buffers = ConcurrentHashMap<Key, JitterBuffer>()
    @Volatile private var running = false
    @Volatile var muted = false              // half-duplex: mute RX while we TX
    @Volatile private var track: AudioTrack? = null
    private var worker: Thread? = null

    /** Channels currently producing audio — surfaced to UI as "receiving". */
    val receivingChannels: Set<String>
        get() = buffers.entries.filter { it.value.isActive() }.map { it.key.channelId }.toSet()

    private fun construirTrack(): AudioTrack {
        val minBuf = AudioTrack.getMinBufferSize(
            AudioConfig.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(AudioConfig.PCM_FRAME_BYTES * 4)

        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(AudioConfig.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()
    }

    fun start() {
        if (running) return
        track = construirTrack().also { it.play() }
        running = true

        worker = thread(name = "wl-playback") {
            // Ver AudioCapture.kt: THREAD_PRIORITY_URGENT_AUDIO en vez de Thread.MAX_PRIORITY.
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            val mix = IntArray(AudioConfig.SAMPLES_PER_FRAME)
            val out = ShortArray(AudioConfig.SAMPLES_PER_FRAME)
            var erroresSeguidos = 0
            while (running) {
                java.util.Arrays.fill(mix, 0)
                var contributed = 0
                val ahora = System.currentTimeMillis()
                for ((key, jb) in buffers) {
                    // Se comprueba antes de pull(): con el buffer vacío pull() devuelve null y
                    // el `continue` saltaba esta limpieza, dejando buffers de emisores ya
                    // callados (o con el contador desincronizado) para siempre.
                    if (jb.isStale(ahora)) { buffers.remove(key); continue }
                    val frame = jb.pull() ?: continue
                    contributed++
                    for (i in mix.indices) mix[i] += frame[i].toInt()
                }
                if (contributed == 0 || muted) {
                    // Write silence to keep the track from underrunning, but cheaply.
                    java.util.Arrays.fill(out, 0)
                } else {
                    for (i in out.indices) out[i] = mix[i].coerceIn(-32768, 32767).toShort()
                }

                val actual = track
                if (actual == null) {
                    // La recreación anterior falló: se reintenta con espera en vez de morir.
                    try { Thread.sleep(200L) } catch (_: InterruptedException) { break }
                    track = runCatching { construirTrack().also { it.play() } }.getOrNull()
                    continue
                }
                if (actual.write(out, 0, out.size) >= 0) {
                    erroresSeguidos = 0
                    continue
                }
                // write() devuelve un código de error al instante (servidor de audio
                // reiniciado, ruta de audio cambiada...): sin pausa esto giraba al 100 % de CPU.
                if (++erroresSeguidos >= MAX_ERRORES_ANTES_DE_RECREAR) {
                    Log.w(TAG, "AudioTrack.write falla repetidamente; se recrea la pista")
                    runCatching { actual.release() }
                    track = runCatching { construirTrack().also { it.play() } }.getOrNull()
                    erroresSeguidos = 0
                }
                try { Thread.sleep(FRAME_MS) } catch (_: InterruptedException) { break }
            }
        }
    }

    /** Called from the network thread when a decoded frame arrives. Non-blocking. */
    fun submit(channelId: String, senderId: String, seq: Int, pcm: ShortArray) {
        val jb = buffers.getOrPut(Key(channelId, senderId)) { JitterBuffer() }
        jb.insert(seq, pcm.copyOf())
    }

    fun stop() {
        running = false
        worker?.join(200); worker = null
        runCatching { track?.stop() }; runCatching { track?.release() }
        track = null
        buffers.clear()
    }

    private companion object {
        const val TAG = "WalkieAudio"
        const val FRAME_MS = 20L
        const val MAX_ERRORES_ANTES_DE_RECREAR = 10
    }
}
