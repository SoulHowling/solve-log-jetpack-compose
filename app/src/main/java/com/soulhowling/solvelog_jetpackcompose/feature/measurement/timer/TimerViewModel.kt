package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.SolveLogApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class TimerViewModel(application: Application): AndroidViewModel(application) {
    private val repository: TimerRepository
    private val alarm: Alarm

    init {
        val solveLogApp = application as SolveLogApplication
        val db = solveLogApp.database
        repository = TimerRepository(db.timerDao())
        alarm = Alarm(application)
    }

    val timers: StateFlow<List<TimerState>> = repository.allTimers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertTimer(minutes: Int, seconds: Int) {
        viewModelScope.launch {
            val totalSeconds = minutes * 60 + seconds
            if (totalSeconds <= 0) return@launch
            val newTimer = repository.createTimer(totalSeconds = totalSeconds)
            repository.insertTimer(newTimer)
        }
    }

    fun deleteTimer(timerId: Int) {
        viewModelScope.launch {
            repository.deleteTimer(timerId)
        }
    }

    fun start(timerId: Int) {
        viewModelScope.launch {
            val timer = timers.value.find { it.id == timerId }
            if (timer != null && !timer.isRunning) {
                repository.upsertTimer(timer.copy(isRunning = true))
            }
        }
    }

    fun pause(timerId: Int) {
        viewModelScope.launch {
            val timer = timers.value.find { it.id == timerId }
            if (timer != null && timer.isRunning) {
                repository.upsertTimer(timer.copy(isRunning = false))
            }
        }
    }

    fun stop(timerId: Int) {
        viewModelScope.launch {
            val timer = timers.value.find { it.id == timerId }
            if (timer != null && timer.isRunning) {
                repository.upsertTimer(timer.copy(remainingSeconds = timer.initialSeconds, isRunning = false))
                alarm.stop()
            }
        }
    }

    fun reset(timerId: Int) {
        viewModelScope.launch {
            val timer = timers.value.find { it.id == timerId }
            if (timer != null) {
                repository.upsertTimer(timer.copy(remainingSeconds = timer.initialSeconds, isRunning = false))
            }
        }
    }

    fun tick(timerId: Int) {
        viewModelScope.launch {
            val currentTimer = timers.value.find { it.id == timerId }
            if (currentTimer != null && currentTimer.hasTimeLeft()) {
                val updateTimer = currentTimer.tick()
                repository.upsertTimer(updateTimer)
                
                // 残り時間が0になったらアラートを鳴らす
                if (updateTimer.remainingSeconds == 0) {
                    alarm.play()
                }
            }
        }
    }

}