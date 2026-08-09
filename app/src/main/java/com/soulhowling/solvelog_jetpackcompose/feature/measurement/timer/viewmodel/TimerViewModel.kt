package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.core.Screens
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerService
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerService.Companion.ACTION_PAUSE
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerService.Companion.ACTION_RESET
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerService.Companion.ACTION_RESTART
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerService.Companion.ACTION_RESUME
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerService.Companion.ACTION_START
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerService.Companion.EXTRA_TIMER_ID
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerRepository
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(application) {
    private val repository: TimerRepository
    private val timerStateId: Int = checkNotNull(savedStateHandle[Screens.ARG_TIMER_STATE_ID])
    
    private val _dbTimer = MutableStateFlow<TimerState?>(null)
    
    val displayedTimer: StateFlow<TimerState?> = combine(
        _dbTimer,
        TimerService.timerState
    ) { dbTimer, serviceTimer ->
        if (serviceTimer != null && serviceTimer.id == timerStateId) {
            serviceTimer
        } else {
            dbTimer
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val totalSets: StateFlow<Int> = combine(
        _dbTimer,
        TimerService.initialWorkSet,
        TimerService.timerState
    ) { dbTimer, serviceInitialSet, serviceTimer ->
        if (serviceTimer != null && serviceTimer.id == timerStateId) {
            serviceInitialSet
        } else {
            dbTimer?.workSet ?: 0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        val solveLogApp = application as SolveLogApplication
        val db = solveLogApp.database
        repository = TimerRepository(db.timerDao())

        viewModelScope.launch {
            repository.getTimerBy(timerStateId).collect { state ->
                _dbTimer.value = state
            }
        }
    }

    fun toggleTimer() {
        val currentState = displayedTimer.value ?: return
        if (currentState.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        val context = getApplication<Application>()
        val serviceTimer = TimerService.timerState.value
        
        if (serviceTimer != null && serviceTimer.id == timerStateId) {
            // Already running/paused this timer
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        } else {
            // Start new timer
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TIMER_ID, timerStateId)
            }
            context.startForegroundService(intent)
        }
    }

    fun pauseTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resetTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = ACTION_RESET
        }
        context.startService(intent)
    }

    fun restartTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = ACTION_RESTART
        }
        context.startService(intent)
    }
}
