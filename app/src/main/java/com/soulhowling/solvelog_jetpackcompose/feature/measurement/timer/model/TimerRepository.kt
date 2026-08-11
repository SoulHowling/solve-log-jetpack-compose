package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import kotlinx.coroutines.flow.Flow

class TimerRepository(private val timerDao: TimerDao) {
    val allTimers: Flow<List<TimerState>> = timerDao.getAllTimerStates()

    fun createTimer(
        title: String,
        trainingSet: Int,
        trainingSeconds: Int,
        breakingSeconds: Int
    ): TimerState {
        return TimerState(
            id = 0,
            title = title,
            workSet = trainingSet,
            workSeconds = trainingSeconds,
            breakSeconds = breakingSeconds,
            phase = TimerPhase.WORK,
        )
    }

    suspend fun insertTimer(timerState: TimerState) {
        timerDao.insert(timerState)
    }

    suspend fun upsertTimer(timerState: TimerState) {
        timerDao.upsert(timerState)
    }

    suspend fun deleteTimer(timerId: Int) {
        timerDao.deleteTimerState(timerId)
    }

    suspend fun deleteAll() {
        timerDao.deleteAll()
    }

    fun getTimerBy(timerStateId: Int): Flow<TimerState?> {
        return timerDao.getTimerState(timerStateId)
    }
}
