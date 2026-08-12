package com.azalogistics.walkie.net

import android.util.Log
import com.azalogistics.walkie.domain.model.Channel
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.concurrent.thread

/**
 * One UDP socket bound to a channel's port. Audio and presence both travel as unicast
 * datagrams fanned out to whichever peers NetworkManager currently has in this channel's
 * roster — not an IP multicast group. Multicast/IGMP handling turned out to be asymmetric
 * across real Wi-Fi APs (some PDAs could transmit but were never heard by others); plain
 * unicast only needs ordinary client-to-client connectivity, which is a lower bar (the one
 * case this doesn't fix is an AP with full client isolation, which blocks unicast between
 * clients too — that's a network-level fix, not a code one).
 *
 * Receive runs on its own thread and hands raw datagrams + sender address to [onPacket]
 * (which must not block).
 */
class ChannelTransport(
    val channel: Channel,
    private val peersToSendTo: () -> Collection<InetAddress>,
    private val onPacket: (ByteArray, Int, InetAddress) -> Unit
) {
    private var socket: DatagramSocket? = null
    @Volatile private var running = false
    private var rxThread: Thread? = null

    fun open() {
        if (running) return
        // Captured as a local: inside DatagramSocket.apply {}, bare "channel" would
        // instead resolve to the Java getter DatagramSocket.getChannel() (a DatagramChannel,
        // unrelated to our Channel), since Kotlin synthesizes a property for it.
        val port = channel.port
        val s = DatagramSocket(null).apply {
            reuseAddress = true            // must be set BEFORE bind to allow co-located peers
            bind(InetSocketAddress(port))
            soTimeout = 1000
        }
        socket = s
        running = true
        Log.i(TAG, "OPEN ch='${channel.id}' port=${channel.port}")

        rxThread = thread(name = "wl-rx-${channel.id}", priority = Thread.NORM_PRIORITY + 2) {
            val buf = ByteArray(2048)
            val pkt = DatagramPacket(buf, buf.size)
            while (running) {
                try {
                    pkt.setData(buf, 0, buf.size)
                    s.receive(pkt)
                    if (Protocol.isValid(buf, pkt.length)) onPacket(buf, pkt.length, pkt.address)
                } catch (_: java.net.SocketTimeoutException) {
                    // loop to re-check running flag
                } catch (e: Exception) {
                    if (running) { Log.w(TAG, "rx error ch='${channel.id}': ${e.message}"); Thread.sleep(50) }
                }
            }
        }
    }

    /** Fan out a pre-built datagram (header+payload already in [data]) to an explicit target list. */
    fun sendTo(data: ByteArray, len: Int, targets: Collection<InetAddress>) {
        val s = socket ?: return
        for (addr in targets) {
            runCatching { s.send(DatagramPacket(data, len, addr, channel.port)) }
                .onFailure { Log.w(TAG, "send error ch='${channel.id}' to=$addr: ${it.message}") }
        }
    }

    /** Fan out to every peer currently known to be on this channel (the common case: audio). */
    fun send(data: ByteArray, len: Int) = sendTo(data, len, peersToSendTo())

    fun close() {
        running = false
        rxThread?.join(200); rxThread = null
        runCatching { socket?.close() }
        socket = null
    }

    private companion object { const val TAG = "WalkieLAN" }
}
