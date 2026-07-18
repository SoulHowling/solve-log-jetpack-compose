package com.soulhowling.solvelog_jetpackcompose.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.room.withTransaction
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.data.PreferenceRepository
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.TimerRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.FolderRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import kotlinx.coroutines.flow.StateFlow

class SettingViewModel(application: Application): AndroidViewModel(application) {

    val themeMode: StateFlow<ThemeMode> = PreferenceRepository.themeMode

    private val folderRepository: FolderRepository
    private val solveLogRepository: SolveLogRepository
    private val questionRepository: QuestionRepository
    private val timerRepository: TimerRepository
    private val database: SolveLogDatabase = (application as SolveLogApplication).database

    init {
        folderRepository = FolderRepository(database.folderDao())
        solveLogRepository = SolveLogRepository(database.solveLogDao())
        questionRepository = QuestionRepository(database.questionDao())
        timerRepository = TimerRepository(database.timerDao())
    }

    fun setThemeMode(mode: ThemeMode) {
        PreferenceRepository.setThemeMode(application, mode)
    }

    suspend fun wipeAllData(): Boolean {
        return try {
            database.withTransaction {
                questionRepository.deleteAll()
                solveLogRepository.deleteAll()
                folderRepository.deleteAll()
                timerRepository.deleteAll()
            }
            true
        } catch (e: Exception) {
            LogUtils.e("Failed to wipe all data", e)
            false
        }
    }

    suspend fun wipeAllLogs(): Boolean {
        return try {
            database.withTransaction {
                questionRepository.deleteAll()
                solveLogRepository.deleteAll()
                folderRepository.deleteAll()
            }
            true
        } catch (e: Exception) {
            LogUtils.e("Failed to wipe all logs", e)
            false
        }
    }

    suspend fun wipeAllTimers(): Boolean {
        return try {
            timerRepository.deleteAll()
            true
        } catch (e: Exception) {
            LogUtils.e("Failed to wipe all timers", e)
            false
        }
    }
}