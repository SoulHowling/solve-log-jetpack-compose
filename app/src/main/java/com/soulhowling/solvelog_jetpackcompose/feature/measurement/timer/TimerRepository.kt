package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer

import kotlinx.coroutines.flow.Flow

class TimerRepository(private val timerDao: TimerDao) {
    val allTimers: Flow<List<TimerState>> = timerDao.getAllTimerStates()

    fun createTimer(totalSeconds: Int): TimerState {
        return TimerState(
            id = 0,
            initialSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            isRunning = false
        )
    }

    suspend fun insertTimer(timerState: TimerState) {
        timerDao.insertTimerState(timerState)
    }

    suspend fun upsertTimer(timerState: TimerState) {
        timerDao.upsertTimerState(timerState)
    }

    suspend fun deleteTimer(timerId: Int) {
        timerDao.deleteTimerState(timerId)
    }
}