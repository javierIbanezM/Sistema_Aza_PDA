package com.azalogistics.walkie.data

import com.azalogistics.walkie.domain.model.Channel
import com.azalogistics.walkie.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ChannelRepositoryImpl(private val settings: SettingsStore) : ChannelRepository {

    override val channels: Flow<List<Channel>> = settings.channelsCsv.map(::parse)

    override val listenChannelIds: Flow<Set<String>> = settings.listening

    override val txChannelId: Flow<String?> = settings.txChannel

    override suspend fun addChannel(channel: Channel) {
        val current = parse(settings.channelsCsv.first()).toMutableList()
        if (current.none { it.id == channel.id }) {
            current += channel
            settings.setChannelsCsv(serialize(current))
            settings.setListening(settings.listening.first() + channel.id)
            if (settings.txChannel.first() == null) settings.setTxChannel(channel.id)
        }
    }

    override suspend fun removeChannel(id: String) {
        val current = parse(settings.channelsCsv.first()).filterNot { it.id == id }
        settings.setChannelsCsv(serialize(current))
        settings.setListening(settings.listening.first() - id)
        if (settings.txChannel.first() == id) settings.setTxChannel(null)
    }

    override suspend fun setListening(id: String, listening: Boolean) {
        val now = settings.listening.first().toMutableSet()
        if (listening) now += id else now -= id
        settings.setListening(now)
    }

    override suspend fun setTxChannel(id: String?) {
        settings.setTxChannel(id)
    }

    private fun parse(csv: String): List<Channel> =
        csv.split(';').filter { it.isNotBlank() }.mapNotNull { entry ->
            val parts = entry.split('|')
            if (parts.size == 2) Channel(parts[0], parts[1]) else null
        }

    private fun serialize(list: List<Channel>): String =
        list.joinToString(";") { "${it.id}|${it.name}" }
}
