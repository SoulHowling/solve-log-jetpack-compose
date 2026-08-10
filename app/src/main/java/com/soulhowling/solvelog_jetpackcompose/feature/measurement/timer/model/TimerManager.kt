package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class TimerManager {
    private val _timerState = MutableStateFlow<TimerState?>(null)
    val timerState: StateFlow<TimerState?> = _timerState.asStateFlow()

    private val _initialWorkSet = MutableStateFlow(0)
    val initialWorkSet: StateFlow<Int> = _initialWorkSet.asStateFlow()

    fun updateTimerState(state: TimerState?) {
        _timerState.value = state
    }

    fun updateInitialWorkSet(sets: Int) {
        _initialWorkSet.value = sets
    }
}
