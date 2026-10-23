package com.azalogistics.walkie.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import kotlin.concurrent.thread

/**
 * Captures mic audio in fixed 20 ms frames and pushes them to [onFrame] from a
 * dedicated high-priority thread. The callback must NOT block (no I/O, no locks held
 * across network) — encode+enqueue only. Production: replace with Oboe/AAudio.
 */
class AudioCapture(
    private val onFrame: (ShortArray) -> Unit
) {
    @Volatile private var running = false
    private var record: AudioRecord? = null
    private var worker: Thread? = null
    private var aec: AcousticEchoCanceler? = null
    private var ns: NoiseSuppressor? = null
    private var agc: AutomaticGainControl? = null

    @SuppressLint("MissingPermission") // caller guarantees RECORD_AUDIO granted
    fun start() {
        if (running) return
        val minBuf = AudioRecord.getMinBufferSize(
            AudioConfig.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(AudioConfig.PCM_FRAME_BYTES * 4)

        val rec = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION, // enables platform AEC/NS path
            AudioConfig.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuf
        )
        record = rec
        attachEffects(rec.audioSessionId)
        rec.startRecording()
        running = true

        worker = thread(name = "wl-capture") {
            // THREAD_PRIORITY_URGENT_AUDIO: prioridad real de audio a nivel de SO (nice de
            // Linux), soportada específicamente por Android para este caso — a diferencia de
            // Thread.MAX_PRIORITY (una prioridad Java débil que el scheduler apenas respeta),
            // esto reduce cortes de audio sin acaparar la CPU tan agresivamente como antes.
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            val frame = ShortArray(AudioConfig.SAMPLES_PER_FRAME)
            var fallosSeguidos = 0
            while (running) {
                var read = 0
                while (read < frame.size && running) {
                    val r = rec.read(frame, read, frame.size - read)
                    if (r <= 0) break
                    read += r
                }
                if (read == frame.size) {
                    fallosSeguidos = 0
                    onFrame(frame)
                } else if (running) {
                    // Lectura fallida (micrófono ocupado, objeto muerto...): sin pausa este
                    // bucle giraba al 100 % de CPU a prioridad de audio. Se reintenta con
                    // espera, más larga si el fallo persiste.
                    fallosSeguidos++
                    try { Thread.sleep(if (fallosSeguidos > 50) 200L else 20L) } catch (_: InterruptedException) { break }
                }
            }
        }
    }

    private fun attachEffects(sessionId: Int) {
        runCatching { if (AcousticEchoCanceler.isAvailable()) aec = AcousticEchoCanceler.create(sessionId)?.apply { enabled = true } }
        runCatching { if (NoiseSuppressor.isAvailable()) ns = NoiseSuppressor.create(sessionId)?.apply { enabled = true } }
        runCatching { if (AutomaticGainControl.isAvailable()) agc = AutomaticGainControl.create(sessionId)?.apply { enabled = true } }
    }

    fun stop() {
        running = false
        worker?.join(200)
        worker = null
        runCatching { record?.stop() }
        runCatching { record?.release() }
        record = null
        aec?.release(); ns?.release(); agc?.release()
        aec = null; ns = null; agc = null
    }
}
