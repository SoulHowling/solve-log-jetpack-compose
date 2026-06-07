package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.soulhowling.solvelog_jetpackcompose.core.Screens
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val database = (application as SolveLogApplication).database
    private val solveLogRepository: SolveLogRepository = SolveLogRepository(database.solveLogDao())
    private val questionRepository: QuestionRepository = QuestionRepository(database.questionDao())
    private val solveLogId: Int = checkNotNull(savedStateHandle[Screens.ARG_SOLVE_LOG_ID])

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

    fun startNewAttempt(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val currentState = uiState.value as? SolveLogViewerUiState.Success ?: return@launch
            val solveLog = currentState.solveLog
            val nextAttempt = solveLog.attemptCount + 1

            database.withTransaction {
                solveLogRepository.upsertSolveLog(
                    solveLog.copy(
                        attemptCount = nextAttempt,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                val questions = (0 until solveLog.questionQuantity).map { index ->
                    questionRepository.createQuestion(
                        attempt = nextAttempt,
                        selectedAnswer = -1,
                        questionIndex = index,
                        correctness = Correctness.NONE.index,
                        isFlag = false,
                        solveLogId = solveLog.id
                    )
                }
                questionRepository.insertQuestions(questions)
            }
            onComplete(nextAttempt)
        }
    }




}