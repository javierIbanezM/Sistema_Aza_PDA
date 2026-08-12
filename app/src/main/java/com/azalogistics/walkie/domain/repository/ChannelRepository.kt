package com.azalogistics.walkie.domain.repository

import com.azalogistics.walkie.domain.model.Channel
import kotlinx.coroutines.flow.Flow

/**
 * Source of truth for channel configuration and selection. Persists across restarts.
 */
interface ChannelRepository {
    val channels: Flow<List<Channel>>
    /** Channels the user is currently listening to (>= 0, target supports >= 2). */
    val listenChannelIds: Flow<Set<String>>
    /** The single channel selected for transmission (or null). */
    val txChannelId: Flow<String?>

    suspend fun addChannel(channel: Channel)
    suspend fun removeChannel(id: String)
    suspend fun setListening(id: String, listening: Boolean)
    suspend fun setTxChannel(id: String?)
}
