package com.azalogistics.walkie.net

import android.content.Context
import android.util.Log
import com.azalogistics.walkie.audio.AudioCapture
import com.azalogistics.walkie.audio.AudioCodec
import com.azalogistics.walkie.audio.AudioConfig
import com.azalogistics.walkie.audio.AudioPlayback
import com.azalogistics.walkie.audio.PcmCodec
import com.azalogistics.walkie.domain.model.Channel
import com.azalogistics.walkie.domain.model.Peer
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Wires discovery + per-channel unicast transports + audio pipeline together.
 *
 * Audio and presence both travel as unicast UDP, fanned out per-channel to whichever
 * peers have recently proven (via a received packet) that they're on that channel — see
 * [roster]. This replaced IP multicast: on real warehouse Wi-Fi (several APs, roaming),
 * multicast group membership (IGMP snooping) was asymmetric — some PDAs could transmit
 * but were never heard by others. Plain unicast only needs ordinary client-to-client
 * connectivity, a lower bar than multicast (the one thing this doesn't fix is an AP with
 * full client isolation, which blocks unicast between clients too — network-level fix,
 * not a code one).
 *
 * [Discovery] (mDNS/NSD) is only used to seed the very first presence probe to a peer
 * we've never heard from yet; once a peer has sent us anything, roster membership is
 * self-sustaining via the periodic presence heartbeat and no longer depends on mDNS.
 */
class NetworkManager(
    private val context: Context,
    private val deviceId: String,
    private val displayName: String,
    private val codec: AudioCodec = PcmCodec(),
    private val onState: (active: Boolean) -> Unit = {},
    private val onPeers: (List<Peer>) -> Unit = {},
    private val onReceivingChanged: (Set<String>) -> Unit = {}
) {
    private val transports = ConcurrentHashMap<String, ChannelTransport>()
    private val channelById = ConcurrentHashMap<String, Channel>()

    // channelId -> deviceId -> presence entry, built entirely from received packets
    // (AUDIO/TX_END/PRESENCE all count as proof of life).
    private val roster = ConcurrentHashMap<String, ConcurrentHashMap<String, PresenceEntry>>()
    private class PresenceEntry(@Volatile var address: InetAddress, @Volatile var lastSeenMs: Long)

    @Volatile private var knownPeerAddresses: List<InetAddress> = emptyList()

    private val playback = AudioPlayback()
    private var capture: AudioCapture? = null
    @Volatile private var txChannelId: String? = null
    @Volatile private var transmitting = false
    @Volatile private var txSeq = 0
    @Volatile private var presenceSeq = 0

    private var discovery: Discovery? = null
    @Volatile private var heartbeatRunning = false
    private var heartbeatThread: Thread? = null

    // reusable tx buffers (capture thread / heartbeat thread only, never shared)
    private val encoded = ByteArray(codec.maxEncodedBytes)
    private val datagram = ByteArray(Protocol.HEADER + codec.maxEncodedBytes)
    private val presenceDatagram = ByteArray(Protocol.HEADER + 32)
    // reusable rx decode buffer is per-thread-unsafe across channels, so allocate per call cheaply
    private val rxPcmPool = ThreadLocal.withInitial { ShortArray(AudioConfig.SAMPLES_PER_FRAME) }

    fun start() {
        Log.i(TAG, "start deviceId='$deviceId' name='$displayName'")
        playback.start()
        discovery = Discovery(context, deviceId, displayName) { peers ->
            knownPeerAddresses = peers.mapNotNull {
                runCatching { InetAddress.getByName(it.address) }.getOrNull()
            }
            onPeers(peers)
        }.also { it.start(localPort = Channel.BASE_PORT) }
        startHeartbeat()
        onState(true)
    }

    /** Idempotently reconcile which channels we are joined to. */
    fun setListenChannels(channels: List<Channel>) {
        val wanted = channels.associateBy { it.id }
        channelById.putAll(wanted)
        // open new
        for ((id, ch) in wanted) {
            if (!transports.containsKey(id)) openTransport(ch)
        }
        // close removed (but keep tx channel open even if not listened)
        for (id in transports.keys.toList()) {
            if (id !in wanted && id != txChannelId) {
                transports.remove(id)?.close()
                roster.remove(id)
            }
        }
    }

    /** Select the single transmit channel; ensures its transport exists. */
    fun setTxChannel(channel: Channel?) {
        txChannelId = channel?.id
        if (channel != null) {
            channelById[channel.id] = channel
            if (!transports.containsKey(channel.id)) openTransport(channel)
        }
    }

    private fun openTransport(channel: Channel) {
        val channelRoster = roster.getOrPut(channel.id) { ConcurrentHashMap() }
        val t = ChannelTransport(
            channel = channel,
            peersToSendTo = { channelRoster.values.map { it.address } },
            onPacket = { buf, len, fromAddress -> onPacket(channel.id, buf, len, fromAddress) }
        )
        runCatching { t.open() }.onSuccess { transports[channel.id] = t }
        // Probe every currently-known LAN peer right away so a freshly opened channel
        // doesn't have to wait up to HEARTBEAT_MS for its first roster entry.
        sendPresence(channel.id, heartbeatTargets(channel.id))
    }

    fun startTransmit() {
        val txId = txChannelId ?: run { Log.w(TAG, "startTransmit: no TX channel selected"); return }
        if (transmitting) return
        if (transports[txId] == null) { Log.w(TAG, "startTransmit: TX transport for '$txId' not open"); return }
        transmitting = true
        playback.muted = true   // half-duplex: avoid feedback while talking
        Log.i(TAG, "TX start -> ch='$txId'")
        var sent = 0
        val cap = AudioCapture { frame ->
            // capture thread: encode + send only, no allocation/IO
            val n = codec.encode(frame, encoded)
            Protocol.writeHeader(datagram, Protocol.TYPE_AUDIO, deviceId, txSeq++ and 0xFFFF, n)
            System.arraycopy(encoded, 0, datagram, Protocol.HEADER, n)
            transports[txId]?.send(datagram, Protocol.HEADER + n)
            if (sent++ % 50 == 0) Log.d(TAG, "TX frames sent=$sent ch='$txId'")
        }
        capture = cap
        runCatching { cap.start() }.onFailure { Log.e(TAG, "AudioCapture.start failed: ${it.message}") }
    }

    fun stopTransmit() {
        if (!transmitting) return
        transmitting = false
        capture?.stop(); capture = null
        // notify listeners that the floor is free
        val txId = txChannelId
        if (txId != null) {
            Protocol.writeHeader(datagram, Protocol.TYPE_TX_END, deviceId, txSeq++ and 0xFFFF, 0)
            transports[txId]?.send(datagram, Protocol.HEADER)
        }
        playback.muted = false
    }

    @Volatile private var rxCount = 0
    private fun onPacket(channelId: String, buf: ByteArray, len: Int, fromAddress: InetAddress) {
        val senderId = Protocol.senderId(buf)
        if (senderId == deviceId) return // ignore loopback of our own audio

        // Any valid packet on this channel's socket proves the sender is present on it —
        // this is what makes the roster self-sustaining without depending on mDNS/NSD.
        roster.getOrPut(channelId) { ConcurrentHashMap() }
            .getOrPut(senderId) { PresenceEntry(fromAddress, 0L) }
            .apply { address = fromAddress; lastSeenMs = System.currentTimeMillis() }

        when (Protocol.type(buf)) {
            Protocol.TYPE_AUDIO -> {
                val plen = Protocol.payloadLen(buf).coerceAtMost(len - Protocol.HEADER)
                val pcm = rxPcmPool.get()!!
                codec.decode(buf, Protocol.HEADER, plen, pcm)
                playback.submit(channelId, senderId, Protocol.seq(buf), pcm)
                onReceivingChanged(playback.receivingChannels)
                if (rxCount++ % 50 == 0) Log.d(TAG, "RX audio ch='$channelId' from='$senderId' n=$rxCount")
            }
            Protocol.TYPE_TX_END -> onReceivingChanged(playback.receivingChannels)
            Protocol.TYPE_PRESENCE -> { /* roster already updated above */ }
        }
    }

    fun stop() {
        stopTransmit()
        heartbeatRunning = false
        heartbeatThread?.join(200); heartbeatThread = null
        discovery?.stop(); discovery = null
        transports.values.forEach { it.close() }
        transports.clear()
        roster.clear()
        playback.stop()
        onState(false)
    }

    // --- presence heartbeat ---

    private fun heartbeatTargets(channelId: String): Collection<InetAddress> {
        val known = roster[channelId]?.values?.map { it.address }?.toSet() ?: emptySet()
        return (known + knownPeerAddresses).distinct()
    }

    private fun sendPresence(channelId: String, targets: Collection<InetAddress>) {
        if (targets.isEmpty()) return
        val transport = transports[channelId] ?: return
        val nameBytes = displayName.take(32).toByteArray(Charsets.UTF_8)
        Protocol.writeHeader(presenceDatagram, Protocol.TYPE_PRESENCE, deviceId, presenceSeq++ and 0xFFFF, nameBytes.size)
        System.arraycopy(nameBytes, 0, presenceDatagram, Protocol.HEADER, nameBytes.size)
        transport.sendTo(presenceDatagram, Protocol.HEADER + nameBytes.size, targets)
    }

    private fun pruneRoster() {
        val now = System.currentTimeMillis()
        for (channelRoster in roster.values) {
            val stale = channelRoster.entries.filter { now - it.value.lastSeenMs > PRESENCE_TIMEOUT_MS }.map { it.key }
            stale.forEach { channelRoster.remove(it) }
        }
    }

    private fun startHeartbeat() {
        heartbeatRunning = true
        heartbeatThread = thread(name = "wl-presence", priority = Thread.NORM_PRIORITY) {
            while (heartbeatRunning) {
                try {
                    pruneRoster()
                    for (channelId in transports.keys) {
                        sendPresence(channelId, heartbeatTargets(channelId))
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "heartbeat error: ${e.message}")
                }
                try { Thread.sleep(HEARTBEAT_MS) } catch (_: InterruptedException) { return@thread }
            }
        }
    }

    private companion object {
        const val TAG = "WalkieLAN"
        const val HEARTBEAT_MS = 8_000L
        const val PRESENCE_TIMEOUT_MS = 25_000L
    }
}
