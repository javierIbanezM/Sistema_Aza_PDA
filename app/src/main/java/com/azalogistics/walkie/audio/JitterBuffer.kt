package com.azalogistics.walkie.audio

import java.util.concurrent.ConcurrentHashMap

/**
 * Per-(channel,sender) reordering buffer that absorbs network jitter and conceals loss.
 *
 * Design: a small ring keyed by sequence number. The mixer pulls one frame per tick;
 * if the expected sequence is missing we emit silence (packet-loss concealment stub)
 * and advance, so playback never stalls. Target depth keeps latency bounded.
 *
 * La secuencia viaja en 16 bits (ver Protocol), así que se compara con aritmética circular:
 * al pasar de 65535 a 0, o si el emisor reinicia su contador, no se queda mudo para siempre.
 */
class JitterBuffer(private val targetFrames: Int = 3, private val maxFrames: Int = 12) {

    private val map = ConcurrentHashMap<Int, ShortArray>()
    @Volatile private var nextSeq = -1
    @Volatile private var primed = false
    @Volatile private var fueraDeVentana = 0
    @Volatile var lastInsertMs = 0L; private set

    fun insert(seq: Int, pcm: ShortArray) {
        lastInsertMs = System.currentTimeMillis()
        if (nextSeq < 0) nextSeq = seq
        val ahead = diff(seq, nextSeq)
        // Drop very old / absurdly future packets — pero si llegan varios seguidos fuera de
        // ventana es que el emisor reinició su contador (o hubo un corte largo): resincronizar
        // en vez de descartar su audio indefinidamente.
        if (ahead < -maxFrames || ahead > 256) {
            if (++fueraDeVentana < RESYNC_AFTER) return
            map.clear()
            nextSeq = seq
            primed = false
        }
        fueraDeVentana = 0
        map[seq] = pcm
        if (map.size >= targetFrames) primed = true
        // Bound memory.
        if (map.size > maxFrames) {
            map.keys.minByOrNull { diff(it, nextSeq) }?.let { if (diff(it, nextSeq) < 0) map.remove(it) }
        }
    }

    /** @return next frame (SAMPLES_PER_FRAME) or null if not yet primed / empty. */
    fun pull(): ShortArray? {
        if (!primed) return null
        val out = map.remove(nextSeq)
        nextSeq = (nextSeq + 1) and SEQ_MASK
        if (map.isEmpty()) primed = false
        // null => loss; mixer treats as silence for this tick.
        return out ?: ShortArray(AudioConfig.SAMPLES_PER_FRAME)
    }

    fun isStale(now: Long, timeoutMs: Long = 1500): Boolean =
        lastInsertMs != 0L && now - lastInsertMs > timeoutMs

    fun isActive(): Boolean = map.isNotEmpty()

    /** Diferencia con signo entre dos secuencias de 16 bits (positivo = [a] va por delante de [b]). */
    private fun diff(a: Int, b: Int): Int = ((a - b) shl 16) shr 16

    private companion object {
        const val SEQ_MASK = 0xFFFF
        const val RESYNC_AFTER = 3
    }
}
