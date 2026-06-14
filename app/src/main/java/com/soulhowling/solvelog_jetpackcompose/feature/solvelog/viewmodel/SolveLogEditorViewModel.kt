package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.core.Screens
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SolveLogEditorUiState {
    object Loading : SolveLogEditorUiState

    data class Success(
        val solveLog: SolveLog,
        val attempt: Int,
        val questions: List<Question>,
        val selectionTypeStringList: List<String>
    ) : SolveLogEditorUiState

    object Error : SolveLogEditorUiState
}

class SolveLogEditorViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val solveLogRepository: SolveLogRepository
    private val questionRepository: QuestionRepository

    private val solveLogId: Int = checkNotNull(savedStateHandle[Screens.ARG_SOLVE_LOG_ID])
    private val attempt: Int = checkNotNull(savedStateHandle[Screens.ARG_ATTEMPT])

    init {
        val database = (application as SolveLogApplication).database
        solveLogRepository = SolveLogRepository(database.solveLogDao())
        questionRepository = QuestionRepository(database.questionDao())
    }

    val uiState: StateFlow<SolveLogEditorUiState> = combine(
        solveLogRepository.getSolveLog(solveLogId),
        questionRepository.getQuestionBy(solveLogId, attempt)
    ) { solveLog, questions ->
        if (solveLog != null) {
            val selectionQuantity = SelectionQuantity.from(solveLog.selectionQuantity)
            val choices =
                SelectionType.getSelectionTypeStringList(solveLog.selectionType, selectionQuantity)

            SolveLogEditorUiState.Success(
                solveLog = solveLog,
                attempt = attempt,
                questions = questions,
                selectionTypeStringList = choices
            )
        } else {
            SolveLogEditorUiState.Error
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = SolveLogEditorUiState.Loading
    )

    fun updateQuestion(
        selectedAnswer: Int,
        questionIndex: Int,
        correctness: Correctness,
        isFlag: Boolean,
    ) {
        viewModelScope.launch {
            upsertQuestion(
                selectedAnswer = selectedAnswer,
                questionIndex = questionIndex,
                correctness = correctness,
                isFlag = isFlag
            )
        }
    }

    private suspend fun upsertQuestion(
        selectedAnswer: Int = -1,
        questionIndex: Int,
        correctness: Correctness = Correctness.NONE,
        isFlag: Boolean = false,
    ) {
        val successState = uiState.value as? SolveLogEditorUiState.Success
        val existingQuestion = successState?.questions?.find { it.questionIndex == questionIndex }

        val newQuestion = questionRepository.createQuestion(
            attempt = attempt,
            selectedAnswer = selectedAnswer,
            questionIndex = questionIndex,
            correctness = correctness.index,
            isFlag = isFlag,
            solveLogId = solveLogId
        ).let {
            if (existingQuestion != null) it.copy(id = existingQuestion.id) else it
        }
        questionRepository.upsertQuestion(newQuestion)
    }

}
