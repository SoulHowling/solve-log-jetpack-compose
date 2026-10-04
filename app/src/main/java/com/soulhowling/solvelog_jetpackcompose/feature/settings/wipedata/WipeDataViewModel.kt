package com.soulhowling.solvelog_jetpackcompose.feature.settings.wipedata

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerRepository
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.MemoRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.FolderType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.FolderRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WipeDataViewModel(application: Application): AndroidViewModel(application) {

    sealed class WipeStatus {
        data object Idle : WipeStatus()
        data object Loading : WipeStatus()
        data object Success : WipeStatus()
        data object Error : WipeStatus()
    }

    private val _wipeStatus = MutableStateFlow<WipeStatus>(WipeStatus.Idle)
    val wipeStatus: StateFlow<WipeStatus> = _wipeStatus.asStateFlow()
    private val folderRepository: FolderRepository
    private val solveLogRepository: SolveLogRepository
    private val questionRepository: QuestionRepository
    private val timerRepository: TimerRepository
    private val memoRepository: MemoRepository
    private val database: SolveLogDatabase = (application as SolveLogApplication).database

    init {
        folderRepository = FolderRepository(database.folderDao())
        solveLogRepository = SolveLogRepository(database.solveLogDao())
        questionRepository = QuestionRepository(database.questionDao())
        timerRepository = TimerRepository(database.timerDao())
        memoRepository = MemoRepository(database.memoDao())
    }

    fun onWipeConfirm(item: WipeDataItem) {
        viewModelScope.launch {
            _wipeStatus.value = WipeStatus.Loading
            val success = when (item) {
                is WipeDataItem.WipeAllData -> wipeAllData()
                is WipeDataItem.WipeAllLogs -> wipeAllLogs()
                is WipeDataItem.WipeAllTimers -> wipeAllTimers()
                is WipeDataItem.WipeAllMemos -> wipeAllMemos()
            }
            _wipeStatus.value = if (success) WipeStatus.Success else WipeStatus.Error
        }
    }

    fun resetWipeStatus() {
        _wipeStatus.value = WipeStatus.Idle
    }

    private suspend fun wipeAllData(): Boolean {
        return try {
            database.withTransaction {
                questionRepository.deleteAll()
                solveLogRepository.deleteAll()
                folderRepository.deleteAll()
                timerRepository.deleteAll()
                memoRepository.deleteAll()
            }
            true
        } catch (e: Exception) {
            LogUtils.e("Failed to wipe all data", e)
            false
        }
    }

    private suspend fun wipeAllLogs(): Boolean {
        return try {
            database.withTransaction {
                questionRepository.deleteAll()
                solveLogRepository.deleteAll()
                folderRepository.deleteAllBy(FolderType.SOLVE_LOG)
            }
            true
        } catch (e: Exception) {
            LogUtils.e("Failed to wipe all logs", e)
            false
        }
    }

    private suspend fun wipeAllTimers(): Boolean {
        return try {
            timerRepository.deleteAll()
            true
        } catch (e: Exception) {
            LogUtils.e("Failed to wipe all timers", e)
            false
        }
    }

    private suspend fun wipeAllMemos(): Boolean {
        return try {
            memoRepository.deleteAll()
            folderRepository.deleteAllBy(FolderType.MEMO)
            true
        } catch (e: Exception) {
            LogUtils.e("Failed to wipe all memos", e)
            false
        }
    }
}