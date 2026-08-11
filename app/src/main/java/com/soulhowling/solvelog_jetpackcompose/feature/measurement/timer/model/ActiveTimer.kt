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
                onWork()
            }
            TimerPhase.BREAK -> {
                onBreak()
            }
            TimerPhase.COMPLETE -> {
                this
            }
        }
    }

    private fun onWork(): ActiveTimer {
        val remainingWorkSet = workSet - 1
        if (remainingWorkSet > 0) {
            if (breakSeconds > 0) {
                return copy(
                    state = state.copy(
                        phase = TimerPhase.BREAK,
                        workSet = remainingWorkSet
                    ),
                    initialSeconds = breakSeconds,
                    remainingSeconds = breakSeconds,
                    isRunning = true
                )
            } else {
                return copy(
                    state = state.copy(
                        phase = TimerPhase.WORK,
                        workSet = remainingWorkSet
                    ),
                    initialSeconds = workSeconds,
                    remainingSeconds = workSeconds,
                    isRunning = true
                )
            }
        } else {
            return copy(
                state = state.copy(
                    phase = TimerPhase.COMPLETE,
                    workSet = 0
                ),
                remainingSeconds = 0,
                isRunning = false
            )
        }
    }

    private fun onBreak(): ActiveTimer {
        return copy(
            state = state.copy(
                phase = TimerPhase.WORK
            ),
            initialSeconds = workSeconds,
            remainingSeconds = workSeconds,
            isRunning = true
        )
    }
}
