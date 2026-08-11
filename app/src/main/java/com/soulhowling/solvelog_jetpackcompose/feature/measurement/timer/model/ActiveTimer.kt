package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

data class ActiveTimer(
    val state: TimerState,
    val remainingSeconds: Int,
    val isRunning: Boolean = false,
    val initialSeconds: Int = 0
) {
    val id get() = state.id
    val title get() = state.title
    val workSet get() = state.workSet
    val workSeconds get() = state.workSeconds
    val breakSeconds get() = state.breakSeconds
    val phase get() = state.phase

    fun nextPhase(): ActiveTimer {
        return when (phase) {
            TimerPhase.WORK -> {
                val remainingWork = workSet - 1
                if (remainingWork > 0) {
                    copy(
                        state = state.copy(
                            phase = TimerPhase.BREAK,
                            workSet = remainingWork
                        ),
                        initialSeconds = breakSeconds,
                        remainingSeconds = breakSeconds,
                        isRunning = true
                    )
                } else {
                    copy(
                        state = state.copy(
                            phase = TimerPhase.COMPLETE,
                            workSet = 0
                        ),
                        remainingSeconds = 0,
                        isRunning = false
                    )
                }
            }
            TimerPhase.BREAK -> {
                copy(
                    state = state.copy(
                        phase = TimerPhase.WORK
                    ),
                    initialSeconds = workSeconds,
                    remainingSeconds = workSeconds,
                    isRunning = true
                )
            }
            TimerPhase.COMPLETE -> {
                this
            }
        }
    }
}
