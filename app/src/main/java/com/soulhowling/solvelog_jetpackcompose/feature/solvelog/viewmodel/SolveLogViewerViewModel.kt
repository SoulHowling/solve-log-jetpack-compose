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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _selectedQuestionIndices = MutableStateFlow<Set<Int>>(emptySet())
    val selectedQuestionIndices: StateFlow<Set<Int>> = _selectedQuestionIndices.asStateFlow()

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

    fun addQuestion() {
        viewModelScope.launch {
            val successState = uiState.value as? SolveLogViewerUiState.Success ?: return@launch
            val solveLog = successState.solveLog
            val nextIndex = solveLog.questionQuantity

            database.withTransaction {
                val questions = (1..solveLog.attemptCount).map { attempt ->
                    questionRepository.createQuestion(
                        attempt = attempt,
                        selectedAnswer = -1,
                        questionIndex = nextIndex,
                        correctness = Correctness.NONE.index,
                        isFlag = false,
                        solveLogId = solveLog.id
                    )
                }
                questionRepository.insertQuestions(questions)

                solveLogRepository.upsertSolveLog(
                    solveLog.copy(
                        questionQuantity = nextIndex + 1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
        if (!_isEditMode.value) {
            _selectedQuestionIndices.value = emptySet()
        }
    }

    fun toggleQuestionSelection(index: Int) {
        val current = _selectedQuestionIndices.value
        _selectedQuestionIndices.value = if (current.contains(index)) {
            current - index
        } else {
            current + index
        }
    }

    fun deleteAttempt(attemptToDelete: Int) {
        viewModelScope.launch {
            val successState = uiState.value as? SolveLogViewerUiState.Success ?: return@launch
            val solveLog = successState.solveLog

            database.withTransaction {
                questionRepository.deleteQuestionsByAttempt(solveLog.id, attemptToDelete)
                questionRepository.decrementAttemptIndices(solveLog.id, attemptToDelete)
                solveLogRepository.upsertSolveLog(
                    solveLog.copy(
                        attemptCount = solveLog.attemptCount - 1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteSelectedQuestions() {
        viewModelScope.launch {
            val successState = uiState.value as? SolveLogViewerUiState.Success ?: return@launch
            val solveLog = successState.solveLog
            val indicesToDelete = _selectedQuestionIndices.value
            if (indicesToDelete.isEmpty()) return@launch

            database.withTransaction {
                indicesToDelete.forEach { index ->
                    questionRepository.deleteQuestionByIndex(solveLog.id, index)
                }

                val remainingIndices = (0 until solveLog.questionQuantity)
                    .filter { it !in indicesToDelete }
                
                val allQuestions = questionRepository.getQuestionListBy(solveLog.id)
                val updatedQuestions = allQuestions
                    .filter { it.questionIndex !in indicesToDelete }
                    .map { question ->
                        val newIndex = remainingIndices.indexOf(question.questionIndex)
                        question.copy(questionIndex = newIndex)
                    }
                
                updatedQuestions.forEach { 
                    questionRepository.upsertQuestion(it)
                }

                solveLogRepository.upsertSolveLog(
                    solveLog.copy(
                        questionQuantity = remainingIndices.size,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            toggleEditMode()
        }
    }




}