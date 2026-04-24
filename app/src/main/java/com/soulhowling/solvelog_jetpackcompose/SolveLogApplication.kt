package com.soulhowling.solvelog_jetpackcompose

import android.app.Application
import androidx.room.Room
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SolveLogApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val database: SolveLogDatabase by lazy {
        SolveLogDatabase.getDatabase(this, applicationScope)
    }
}