package com.soulhowling.solvelog_jetpackcompose.core

import android.app.Application
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SolveLogApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val database: SolveLogDatabase
        get() = SolveLogDatabase.getDatabase(this, applicationScope)

    val timerManager: TimerManager by lazy {
        TimerManager()
    }
}