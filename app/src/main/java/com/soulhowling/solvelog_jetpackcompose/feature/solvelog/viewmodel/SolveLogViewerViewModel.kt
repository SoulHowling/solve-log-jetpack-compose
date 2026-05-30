package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.core.Screens
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SolveLogViewerUiState {
    object Loading: SolveLogViewerUiState
    data class Success(
        val solveLog: SolveLog,
        val questions: Map<Int, List<Question>>
    ): SolveLogViewerUiState
    object Error: SolveLogViewerUiState
}

class SolveLogViewerViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(application) {

    private val solveLogRepository: SolveLogRepository
    private val questionRepository: QuestionRepository
    private val solveLogId: Int = checkNotNull(savedStateHandle[Screens.ARG_SOLVE_LOG_ID])

    init {
        val database = (application as SolveLogApplication).database
        solveLogRepository = SolveLogRepository(database.solveLogDao())
        questionRepository = QuestionRepository(database.questionDao())
    }

    val uiState: StateFlow<SolveLogViewerUiState> = combine(
        solveLogRepository.getSolveLog(solveLogId),
        questionRepository.getQuestionBy(solveLogId)
    ) { solveLog, questions ->
            if (solveLog != null) {
                val group = questions.groupBy { it.questionIndex }
                SolveLogViewerUiState.Success(
                    solveLog = solveLog,
                    questions = group
                )
            } else {
                SolveLogViewerUiState.Error
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SolveLogViewerUiState.Loading
        )




}