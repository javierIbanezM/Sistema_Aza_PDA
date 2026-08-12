package com.azalogistics.walkie.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
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
    private var track: AudioTrack? = null
    private var worker: Thread? = null

    /** Channels currently producing audio — surfaced to UI as "receiving". */
    val receivingChannels: Set<String>
        get() = buffers.entries.filter { it.value.isActive() }.map { it.key.channelId }.toSet()

    fun start() {
        if (running) return
        val minBuf = AudioTrack.getMinBufferSize(
            AudioConfig.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(AudioConfig.PCM_FRAME_BYTES * 4)

        val t = AudioTrack.Builder()
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
        track = t
        t.play()
        running = true

        worker = thread(name = "wl-playback") {
            // Ver AudioCapture.kt: THREAD_PRIORITY_URGENT_AUDIO en vez de Thread.MAX_PRIORITY.
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            val mix = IntArray(AudioConfig.SAMPLES_PER_FRAME)
            val out = ShortArray(AudioConfig.SAMPLES_PER_FRAME)
            while (running) {
                java.util.Arrays.fill(mix, 0)
                var contributed = 0
                for ((key, jb) in buffers) {
                    val frame = jb.pull() ?: continue
                    contributed++
                    for (i in mix.indices) mix[i] += frame[i].toInt()
                    if (jb.isStale(System.currentTimeMillis())) buffers.remove(key)
                }
                if (contributed == 0 || muted) {
                    // Write silence to keep the track from underrunning, but cheaply.
                    java.util.Arrays.fill(out, 0)
                } else {
                    for (i in out.indices) out[i] = mix[i].coerceIn(-32768, 32767).toShort()
                }
                t.write(out, 0, out.size)
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
}
