package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerService
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerPhase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerRepository
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerListViewModel(application: Application): AndroidViewModel(application) {
    private val repository: TimerRepository
    private val timers: StateFlow<List<TimerState>>
    
    val displayTimers: StateFlow<List<TimerState>>

    val activeTimerId: StateFlow<Int?> = TimerService.timerState.map { it?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        val solveLogApp = application as SolveLogApplication
        val db = solveLogApp.database
        repository = TimerRepository(db.timerDao())

        timers = repository.allTimers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        displayTimers = combine(
            timers,
            TimerService.timerState
        ) { dbTimers, serviceTimer ->
            dbTimers.map { dbTimer ->
                if (serviceTimer != null && serviceTimer.id == dbTimer.id) {
                    serviceTimer
                } else {
                    dbTimer
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insertTimer(title: String, sets: Int, workMinutes: Int, workSeconds: Int, breakMinutes: Int, breakSeconds: Int) {
        viewModelScope.launch {
            val totalWorkSeconds = convertToSeconds(workMinutes, workSeconds)
            val totalBreakSeconds = convertToSeconds(breakMinutes, breakSeconds)
            if (totalWorkSeconds <= 0 || sets <= 0) return@launch
            val newTimer = repository.createTimer(
                title = title,
                trainingSet = sets,
                trainingSeconds = totalWorkSeconds,
                breakingSeconds = totalBreakSeconds
            )
            repository.insertTimer(newTimer)
        }
    }

    fun updateTimer(
        timerState: TimerState,
        title: String,
        sets: Int,
        workMinutes: Int,
        workSeconds: Int,
        breakMinutes: Int,
        breakSeconds: Int
    ) {
        viewModelScope.launch {
            val totalWorkSeconds = convertToSeconds(workMinutes, workSeconds)
            val totalBreakSeconds = convertToSeconds(breakMinutes, breakSeconds)
            if (totalWorkSeconds <= 0 || sets <= 0) return@launch
            
            val updatedTimer = timerState.copy(
                title = title,
                workSet = sets,
                workSeconds = totalWorkSeconds,
                breakSeconds = totalBreakSeconds,
                initialSeconds = totalWorkSeconds,
                remainingSeconds = totalWorkSeconds,
                phase = TimerPhase.WORK,
                isRunning = false,
                lastModified = System.currentTimeMillis()
            )
            repository.upsertTimer(updatedTimer)
        }
    }

    private fun convertToSeconds(minutes: Int, seconds: Int): Int {
        return minutes * 60 + seconds
    }

    fun deleteTimer(timerId: Int) {
        viewModelScope.launch {
            repository.deleteTimer(timerId)
        }
    }

}