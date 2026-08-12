package com.azalogistics.walkie.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azalogistics.walkie.App
import com.azalogistics.walkie.domain.model.Channel
import com.azalogistics.walkie.domain.model.WalkieState
import com.azalogistics.walkie.ptt.PttKey
import com.azalogistics.walkie.ptt.PttManager
import com.azalogistics.walkie.service.WalkieRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WalkieViewModel : ViewModel() {
    private val app = App.instance
    private val repo = app.channelRepository

    val state: StateFlow<WalkieState> = WalkieRuntime.state

    val channels: StateFlow<List<Channel>> =
        repo.channels.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val listenChannelIds: StateFlow<Set<String>> =
        repo.listenChannelIds.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val txChannelId: StateFlow<String?> =
        repo.txChannelId.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pttKeyCode: StateFlow<Int> =
        app.settings.pttKeyCode.stateIn(viewModelScope, SharingStarted.Eagerly, PttKey.DEFAULT_KEYCODE)

    // --- channel actions ---
    fun toggleListen(channelId: String, listening: Boolean) =
        viewModelScope.launch { repo.setListening(channelId, listening) }

    fun selectTx(channelId: String?) =
        viewModelScope.launch { repo.setTxChannel(channelId) }

    fun addChannel(id: String, name: String) =
        viewModelScope.launch { if (id.isNotBlank()) repo.addChannel(Channel(id.trim(), name.ifBlank { id }.trim())) }

    fun removeChannel(id: String) = viewModelScope.launch { repo.removeChannel(id) }

    // --- ptt ---
    fun pttDown() = PttManager.press()
    fun pttUp() = PttManager.release()

    fun setPttKey(code: Int) = viewModelScope.launch {
        app.settings.setPttKeyCode(code)
        PttManager.configuredKeyCode = code
    }

    // --- ptt key detection ---
    private val _detectingPttKey = MutableStateFlow(false)
    val detectingPttKey: StateFlow<Boolean> = _detectingPttKey.asStateFlow()

    fun startPttKeyDetection() { _detectingPttKey.value = true }

    fun onDetectedPttKey(code: Int) {
        if (!_detectingPttKey.value) return
        _detectingPttKey.value = false
        setPttKey(code)
    }

    fun cancelPttKeyDetection() { _detectingPttKey.value = false }
}
