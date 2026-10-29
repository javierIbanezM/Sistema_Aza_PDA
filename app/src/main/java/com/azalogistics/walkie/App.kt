package com.azalogistics.walkie

import android.app.Application
import com.azalogistics.walkie.data.ChannelRepositoryImpl
import com.azalogistics.walkie.data.SettingsStore
import com.azalogistics.walkie.domain.repository.ChannelRepository

/**
 * Poor-man's DI container. Keeps the skeleton dependency-free; swap for Hilt/Koin later.
 */
class App : Application() {
    lateinit var settings: SettingsStore
        private set
    lateinit var channelRepository: ChannelRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settings = SettingsStore(this)
        channelRepository = ChannelRepositoryImpl(settings)
        // Canal de órdenes remotas (instalar/desinstalar/cerrar apps sin ADB). Nunca debe tumbar la app.
        runCatching { com.azalogistics.pda.OrdenesRemotas.iniciar(this) }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
