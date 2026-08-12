package com.azalogistics.walkie.audio

/**
 * Shared audio format. 16 kHz mono / 20 ms frames is a good voice/latency tradeoff.
 * All peers MUST agree on these values (they are part of the wire contract).
 */
object AudioConfig {
    const val SAMPLE_RATE = 16_000
    const val CHANNELS = 1
    const val BYTES_PER_SAMPLE = 2          // PCM 16-bit
    const val FRAME_MS = 20
    const val SAMPLES_PER_FRAME = SAMPLE_RATE * FRAME_MS / 1000      // 320
    const val PCM_FRAME_BYTES = SAMPLES_PER_FRAME * BYTES_PER_SAMPLE // 640
}
