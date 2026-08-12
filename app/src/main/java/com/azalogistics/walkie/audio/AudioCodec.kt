package com.azalogistics.walkie.audio

/**
 * Pluggable codec boundary. The whole pipeline works in terms of "one frame in / one
 * frame out" so Opus (libopus via JNI) can replace [PcmCodec] without touching net/audio.
 */
interface AudioCodec {
    /** Max bytes an encoded frame can occupy (sizing buffers). */
    val maxEncodedBytes: Int

    /** Encode exactly one PCM frame (SAMPLES_PER_FRAME shorts) into [out]; returns bytes written. */
    fun encode(pcm: ShortArray, out: ByteArray): Int

    /** Decode one encoded frame into [pcm]; returns samples written (== SAMPLES_PER_FRAME). */
    fun decode(encoded: ByteArray, offset: Int, length: Int, pcm: ShortArray): Int
}

/** Identity codec: little-endian PCM16. Zero deps, perfect for LAN bring-up. */
class PcmCodec : AudioCodec {
    override val maxEncodedBytes = AudioConfig.PCM_FRAME_BYTES

    override fun encode(pcm: ShortArray, out: ByteArray): Int {
        var j = 0
        for (i in 0 until AudioConfig.SAMPLES_PER_FRAME) {
            val s = pcm[i].toInt()
            out[j++] = (s and 0xFF).toByte()
            out[j++] = (s shr 8 and 0xFF).toByte()
        }
        return AudioConfig.PCM_FRAME_BYTES
    }

    override fun decode(encoded: ByteArray, offset: Int, length: Int, pcm: ShortArray): Int {
        val n = minOf(AudioConfig.SAMPLES_PER_FRAME, length / 2)
        var j = offset
        for (i in 0 until n) {
            val lo = encoded[j++].toInt() and 0xFF
            val hi = encoded[j++].toInt()
            pcm[i] = (lo or (hi shl 8)).toShort()
        }
        // zero-fill on short frame (concealment is the jitter buffer's job)
        for (i in n until AudioConfig.SAMPLES_PER_FRAME) pcm[i] = 0
        return AudioConfig.SAMPLES_PER_FRAME
    }
}
