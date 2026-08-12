package com.azalogistics.walkie.domain.model

/** High-level connectivity state surfaced to the UI. */
enum class ConnectionState {
    NO_NETWORK,      // no usable LAN / wifi
    CONNECTING,      // joining groups / acquiring locks
    CONNECTED,       // ready to tx/rx
    RECONNECTING     // lost network, retrying
}

/** Transmit (PTT) state. */
enum class TxState {
    IDLE,
    TRANSMITTING
}

/** A discovered peer on the LAN. */
data class Peer(
    val id: String,
    val displayName: String,
    val address: String,
    val lastSeenMs: Long
)

/** Aggregate runtime state observed by the UI. */
data class WalkieState(
    val connection: ConnectionState = ConnectionState.NO_NETWORK,
    val tx: TxState = TxState.IDLE,
    val txChannelId: String? = null,
    val listenChannelIds: Set<String> = emptySet(),
    val receivingChannelIds: Set<String> = emptySet(), // channels with active inbound audio
    val peers: List<Peer> = emptyList()
)
