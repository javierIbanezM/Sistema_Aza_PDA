package com.azalogistics.walkie.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.azalogistics.walkie.ptt.PttKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "walkie_settings")

/**
 * Persistent settings: device identity, channel set, listen selection, tx selection, PTT key.
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val deviceId = stringPreferencesKey("device_id")
        val displayName = stringPreferencesKey("display_name")
        val channelsCsv = stringPreferencesKey("channels_csv") // "id|name;id|name"
        val listening = stringSetPreferencesKey("listening_ids")
        val txChannel = stringPreferencesKey("tx_channel_id")
        val pttKeyCode = intPreferencesKey("ptt_key_code")
    }

    val deviceId: Flow<String> = context.dataStore.data.map { it[Keys.deviceId] ?: "" }
    val displayName: Flow<String> = context.dataStore.data.map { it[Keys.displayName] ?: "Android" }
    val channelsCsv: Flow<String> = context.dataStore.data.map { it[Keys.channelsCsv] ?: "general|General" }
    // Por defecto, el canal "general" (el único que trae la app de fábrica) queda activo
    // para ESCUCHAR sin que un admin tenga que configurarlo — todo el mundo oye desde el
    // primer momento. La transmisión NO tiene valor por defecto (null): comunicación
    // unidireccional por diseño — un dispositivo solo puede hablar si un admin entra a la
    // pantalla de Walkie (PIN) y le selecciona explícitamente un canal de TX.
    val listening: Flow<Set<String>> = context.dataStore.data.map { it[Keys.listening] ?: setOf("general") }
    val txChannel: Flow<String?> = context.dataStore.data.map { it[Keys.txChannel] }
    val pttKeyCode: Flow<Int> = context.dataStore.data.map { it[Keys.pttKeyCode] ?: PttKey.DEFAULT_KEYCODE }

    suspend fun ensureDeviceId(): String {
        var id = ""
        context.dataStore.edit { p ->
            id = p[Keys.deviceId] ?: UUID.randomUUID().toString().take(8).also { p[Keys.deviceId] = it }
        }
        return id
    }

    suspend fun setChannelsCsv(v: String) = context.dataStore.edit { it[Keys.channelsCsv] = v }
    suspend fun setListening(v: Set<String>) = context.dataStore.edit { it[Keys.listening] = v }
    suspend fun setTxChannel(v: String?) = context.dataStore.edit { p ->
        if (v == null) p.remove(Keys.txChannel) else p[Keys.txChannel] = v
    }
    suspend fun setPttKeyCode(v: Int) = context.dataStore.edit { it[Keys.pttKeyCode] = v }
    suspend fun setDisplayName(v: String) = context.dataStore.edit { it[Keys.displayName] = v }
}
