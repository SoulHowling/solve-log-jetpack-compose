package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class TimerManager {
    private val _timer = MutableStateFlow<ActiveTimer?>(null)
    val timer: StateFlow<ActiveTimer?> = _timer.asStateFlow()

    private val _initialWorkSet = MutableStateFlow(0)
    val initialWorkSet: StateFlow<Int> = _initialWorkSet.asStateFlow()

    fun updateTimer(timer: ActiveTimer?) {
        _timer.value = timer
    }

    fun updateInitialWorkSet(sets: Int) {
        _initialWorkSet.value = sets
    }
}
