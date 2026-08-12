package com.azalogistics.walkie.net

import java.nio.charset.StandardCharsets

/**
 * Compact fixed-header wire format (little-endian). One UDP datagram == one frame.
 *
 *  off size field
 *   0   2  magic 'W','L'
 *   2   1  version (1)
 *   3   1  type (1=AUDIO, 2=TX_END, 3=PRESENCE)
 *   4   8  senderId (ASCII, space-padded)
 *  12   2  seq (uint16, wraps)
 *  14   2  payloadLen (uint16)
 *  16   N  payload (encoded audio for AUDIO; UTF-8 displayName for PRESENCE)
 */
object Protocol {
    const val MAGIC0 = 'W'.code.toByte()
    const val MAGIC1 = 'L'.code.toByte()
    const val VERSION = 1
    const val HEADER = 16

    const val TYPE_AUDIO = 1
    const val TYPE_TX_END = 2
    const val TYPE_PRESENCE = 3

    fun writeHeader(buf: ByteArray, type: Int, senderId: String, seq: Int, payloadLen: Int) {
        buf[0] = MAGIC0; buf[1] = MAGIC1
        buf[2] = VERSION.toByte()
        buf[3] = type.toByte()
        val id = senderId.padEnd(8).take(8).toByteArray(StandardCharsets.US_ASCII)
        System.arraycopy(id, 0, buf, 4, 8)
        buf[12] = (seq and 0xFF).toByte()
        buf[13] = (seq shr 8 and 0xFF).toByte()
        buf[14] = (payloadLen and 0xFF).toByte()
        buf[15] = (payloadLen shr 8 and 0xFF).toByte()
    }

    fun isValid(buf: ByteArray, len: Int): Boolean =
        len >= HEADER && buf[0] == MAGIC0 && buf[1] == MAGIC1 && buf[2].toInt() == VERSION

    fun type(buf: ByteArray) = buf[3].toInt()
    fun senderId(buf: ByteArray): String =
        String(buf, 4, 8, StandardCharsets.US_ASCII).trim()
    fun seq(buf: ByteArray): Int = (buf[12].toInt() and 0xFF) or (buf[13].toInt() and 0xFF shl 8)
    fun payloadLen(buf: ByteArray): Int = (buf[14].toInt() and 0xFF) or (buf[15].toInt() and 0xFF shl 8)
}
