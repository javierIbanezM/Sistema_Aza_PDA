package com.azalogistics.walkie.net

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.azalogistics.walkie.domain.model.Peer
import kotlin.concurrent.thread

/**
 * LAN presence via NSD/mDNS. We register this device as `_walkielan._udp` and resolve
 * others, so the UI can show who is on the network and NetworkManager has candidate IPs
 * to seed its first presence probe on a channel.
 *
 * NSD only calls [NsdManager.DiscoveryListener.onServiceLost] on an explicit mDNS
 * goodbye, which real Wi-Fi (roaming, dropped packets) doesn't reliably deliver — without
 * the sweep below, a peer that silently disappeared stays in the list forever, which is
 * why the on-screen peer count could grow far past the real number of devices. The sweep
 * both prunes anything not re-seen within [STALE_TIMEOUT_MS] and restarts discovery so
 * peers that are still actually there get a fresh onServiceResolved (and thus a fresh
 * lastSeenMs) before they'd otherwise be pruned.
 */
class Discovery(
    context: Context,
    private val deviceId: String,
    private val displayName: String,
    private val onPeers: (List<Peer>) -> Unit
) {
    private val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val serviceType = "_walkielan._udp."
    private val peers = LinkedHashMap<String, Peer>()
    private var regListener: NsdManager.RegistrationListener? = null
    private var discListener: NsdManager.DiscoveryListener? = null
    private var localPort = 0
    @Volatile private var sweeping = false
    private var sweepThread: Thread? = null

    fun start(localPort: Int) {
        this.localPort = localPort
        register(localPort)
        discover()
        startSweep()
    }

    private fun register(port: Int) {
        val info = NsdServiceInfo().apply {
            serviceName = "WL-$displayName-$deviceId"
            serviceType = this@Discovery.serviceType
            setPort(port)
        }
        val l = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(p0: NsdServiceInfo) {}
            override fun onRegistrationFailed(p0: NsdServiceInfo, p1: Int) {}
            override fun onServiceUnregistered(p0: NsdServiceInfo) {}
            override fun onUnregistrationFailed(p0: NsdServiceInfo, p1: Int) {}
        }
        regListener = l
        runCatching { nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, l) }
    }

    private fun discover() {
        val l = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(p0: String) {}
            override fun onDiscoveryStopped(p0: String) {}
            override fun onStartDiscoveryFailed(p0: String, p1: Int) {}
            override fun onStopDiscoveryFailed(p0: String, p1: Int) {}
            override fun onServiceFound(info: NsdServiceInfo) {
                if (info.serviceName.contains(deviceId)) return // skip self
                nsd.resolveService(info, resolveListener())
            }
            override fun onServiceLost(info: NsdServiceInfo) {
                synchronized(peers) { peers.remove(info.serviceName) }?.let { publish() }
            }
        }
        discListener = l
        runCatching { nsd.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, l) }
    }

    private fun resolveListener() = object : NsdManager.ResolveListener {
        override fun onResolveFailed(p0: NsdServiceInfo, p1: Int) {}
        override fun onServiceResolved(info: NsdServiceInfo) {
            val peer = Peer(
                id = info.serviceName,
                displayName = info.serviceName.removePrefix("WL-").substringBeforeLast('-'),
                address = info.host?.hostAddress ?: "?",
                lastSeenMs = System.currentTimeMillis()
            )
            synchronized(peers) { peers[peer.id] = peer }
            publish()
        }
    }

    private fun publish() {
        onPeers(synchronized(peers) { peers.values.toList() })
    }

    private fun startSweep() {
        sweeping = true
        sweepThread = thread(name = "wl-discovery-sweep") {
            while (sweeping) {
                try { Thread.sleep(SWEEP_INTERVAL_MS) } catch (_: InterruptedException) { return@thread }
                if (!sweeping) return@thread
                pruneStale()
                // Force Android to re-enumerate everything currently advertised, refreshing
                // lastSeenMs for peers that are still really there.
                runCatching { discListener?.let { nsd.stopServiceDiscovery(it) } }
                discover()
            }
        }
    }

    private fun pruneStale() {
        val now = System.currentTimeMillis()
        val removed = synchronized(peers) {
            val stale = peers.values.filter { now - it.lastSeenMs > STALE_TIMEOUT_MS }.map { it.id }
            stale.forEach { peers.remove(it) }
            stale.isNotEmpty()
        }
        if (removed) {
            Log.i(TAG, "sweep pruned stale peers, ${synchronized(peers) { peers.size }} remain")
            publish()
        }
    }

    fun stop() {
        sweeping = false
        sweepThread?.join(200); sweepThread = null
        runCatching { regListener?.let { nsd.unregisterService(it) } }
        runCatching { discListener?.let { nsd.stopServiceDiscovery(it) } }
        regListener = null; discListener = null
        synchronized(peers) { peers.clear() }
    }

    private companion object {
        const val TAG = "WalkieLAN"
        const val SWEEP_INTERVAL_MS = 45_000L
        const val STALE_TIMEOUT_MS = 100_000L
    }
}
