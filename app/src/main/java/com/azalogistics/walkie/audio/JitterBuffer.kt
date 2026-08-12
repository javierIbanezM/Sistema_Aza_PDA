package com.azalogistics.walkie.audio

import java.util.concurrent.ConcurrentHashMap

/**
 * Per-(channel,sender) reordering buffer that absorbs network jitter and conceals loss.
 *
 * Design: a small ring keyed by sequence number. The mixer pulls one frame per tick;
 * if the expected sequence is missing we emit silence (packet-loss concealment stub)
 * and advance, so playback never stalls. Target depth keeps latency bounded.
 */
class JitterBuffer(private val targetFrames: Int = 3, private val maxFrames: Int = 12) {

    private class Slot(val seq: Int, val pcm: ShortArray)

    private val map = ConcurrentHashMap<Int, ShortArray>()
    @Volatile private var nextSeq = -1
    @Volatile private var primed = false
    @Volatile var lastInsertMs = 0L; private set

    fun insert(seq: Int, pcm: ShortArray) {
        lastInsertMs = System.currentTimeMillis()
        if (nextSeq < 0) nextSeq = seq
        // Drop very old / absurdly future packets.
        val ahead = seq - nextSeq
        if (ahead < -maxFrames || ahead > 256) return
        map[seq] = pcm
        if (map.size >= targetFrames) primed = true
        // Bound memory.
        if (map.size > maxFrames) {
            map.keys.minOrNull()?.let { if (it < nextSeq) map.remove(it) }
        }
    }

    /** @return next frame (SAMPLES_PER_FRAME) or null if not yet primed / empty. */
    fun pull(): ShortArray? {
        if (!primed) return null
        val out = map.remove(nextSeq)
        nextSeq++
        if (map.isEmpty()) primed = false
        // null => loss; mixer treats as silence for this tick.
        return out ?: ShortArray(AudioConfig.SAMPLES_PER_FRAME)
    }

    fun isStale(now: Long, timeoutMs: Long = 1500): Boolean =
        lastInsertMs != 0L && now - lastInsertMs > timeoutMs

    fun isActive(): Boolean = map.isNotEmpty()
}
