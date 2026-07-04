package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch.StopwatchService.Companion.ACTION_PAUSE
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch.StopwatchService.Companion.ACTION_RESET
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch.StopwatchService.Companion.ACTION_RESUME
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch.StopwatchService.Companion.ACTION_START
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StopwatchViewModel(application: Application) : AndroidViewModel(application) {

    val elapsedTime: StateFlow<Long> = StopwatchService.elapsedTime
    val isRunning: StateFlow<Boolean> = StopwatchService.isRunning

    val isPaused: StateFlow<Boolean> = combine(elapsedTime, isRunning) { time, running ->
        time > 0 && !running
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun toggleStartStop() {
        val context = application
        val intent = Intent(context, StopwatchService::class.java)

        if (isRunning.value) {
            intent.action = ACTION_PAUSE
            context.startService(intent)
        } else {
            if (elapsedTime.value == 0L) {
                intent.action = ACTION_START
                context.startForegroundService(intent)
            } else {
                intent.action = ACTION_RESUME
                context.startService(intent)
            }
        }
    }

    fun reset() {
        val context = getApplication<Application>()
        val intent = Intent(context, StopwatchService::class.java).apply {
            action = ACTION_RESET
        }
        context.startService(intent)
    }

    fun formatTime(ms: Long): String {
        val hundredths = (ms % 1000) / 10
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60))

        return if (hours > 0) {
            "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, hundredths)
        } else {
            "%02d:%02d.%02d".format(minutes, seconds, hundredths)
        }
    }
}
