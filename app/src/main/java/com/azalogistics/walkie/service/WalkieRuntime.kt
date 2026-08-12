package com.azalogistics.walkie.service

import com.azalogistics.walkie.domain.model.WalkieState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Single source of truth for live runtime state, published by [WalkieService] and
 * observed by the UI. Kept as a process singleton so the UI can bind without IPC.
 */
object WalkieRuntime {
    private val _state = MutableStateFlow(WalkieState())
    val state: StateFlow<WalkieState> = _state

    fun update(block: (WalkieState) -> WalkieState) = _state.update(block)
}
