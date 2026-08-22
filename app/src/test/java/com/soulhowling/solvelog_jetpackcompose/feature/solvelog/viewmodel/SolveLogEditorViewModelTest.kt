package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.Screens
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SolveLogEditorViewModelTest {

    private lateinit var viewModel: SolveLogEditorViewModel
    private lateinit var application: SolveLogApplication
    private lateinit var solveLogRepository: SolveLogRepository
    private lateinit var questionRepository: QuestionRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    private val solveLogId = 1
    private val attempt = 2

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        val database = application.database
        solveLogRepository = SolveLogRepository(database.solveLogDao())
        questionRepository = QuestionRepository(database.questionDao())

        runBlocking {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun kotlinx.coroutines.test.TestScope.createViewModel(
        logId: Int = solveLogId,
        att: Int = attempt
    ) {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                Screens.ARG_SOLVE_LOG_ID to logId,
                Screens.ARG_ATTEMPT to att
            )
        )
        viewModel = SolveLogEditorViewModel(application, savedStateHandle)
        // Subscribe to the flow to trigger WhileSubscribed
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun uiState_initialStateIsLoading() = runTest {
        createViewModel()
        assertEquals(SolveLogEditorUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_loadsDataSuccessfully() = runTest {
        // Prepare data
        val solveLog = SolveLog(
            id = solveLogId,
            name = "Test Log",
            questionQuantity = 10,
            selectionType = SelectionType.ALPHABET.index,
            selectionQuantity = SelectionQuantity.FOUR.index,
            attemptCount = 2,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            folderId = null
        )
        solveLogRepository.insertSolveLog(solveLog)

        val question = Question(
            attempt = attempt,
            questionIndex = 0,
            selectedAnswer = 0,
            correctness = Correctness.CORRECT.index,
            isFlag = true,
            solveLogId = solveLogId
        )
        questionRepository.insertQuestion(question)

        createViewModel()
        
        val state = viewModel.uiState.first { it !is SolveLogEditorUiState.Loading }
        assertTrue(state is SolveLogEditorUiState.Success)
        val successState = state as SolveLogEditorUiState.Success
        assertEquals("Test Log", successState.solveLog.name)
        assertEquals(1, successState.questions.size)
        assertEquals(attempt, successState.attempt)
        assertEquals(listOf("A", "B", "C", "D"), successState.selectionTypeStringList)
    }

    @Test
    fun uiState_showsErrorWhenSolveLogNotFound() = runTest {
        createViewModel(logId = 999)
        
        val state = viewModel.uiState.first { it !is SolveLogEditorUiState.Loading }
        assertEquals(SolveLogEditorUiState.Error, state)
    }

    @Test
    fun updateQuestion_upsertsCorrectly() = runTest {
        // Prepare SolveLog
        val solveLog = SolveLog(
            id = solveLogId,
            name = "Test Log",
            questionQuantity = 5,
            selectionType = SelectionType.NUMBER.index,
            selectionQuantity = SelectionQuantity.FIVE.index,
            attemptCount = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            folderId = null
        )
        solveLogRepository.insertSolveLog(solveLog)

        createViewModel(att = 1)
        
        // Wait for load
        viewModel.uiState.first { it is SolveLogEditorUiState.Success }

        // Update question
        viewModel.updateQuestion(
            selectedAnswer = 2,
            questionIndex = 0,
            correctness = Correctness.CORRECT,
            isFlag = true
        )

        // Verify database
        val questions = questionRepository.getQuestionBy(solveLogId, 1).first { it.isNotEmpty() }
        assertEquals(1, questions.size)
        assertEquals(2, questions[0].selectedAnswer)
        assertEquals(Correctness.CORRECT.index, questions[0].correctness)
        assertTrue(questions[0].isFlag)
    }

    @Test
    fun correctnessArchives_computedCorrectlyFromPastAttempts() = runTest {
        // Prepare SolveLog
        val solveLog = SolveLog(
            id = solveLogId,
            name = "Test Log",
            questionQuantity = 5,
            selectionType = SelectionType.NUMBER.index,
            selectionQuantity = SelectionQuantity.FIVE.index,
            attemptCount = 3,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            folderId = null
        )
        solveLogRepository.insertSolveLog(solveLog)

        // Attempt 1: Question 0 is Correct, Question 1 is Incorrect
        questionRepository.insertQuestions(listOf(
            Question(attempt = 1, questionIndex = 0, correctness = Correctness.CORRECT.index, solveLogId = solveLogId),
            Question(attempt = 1, questionIndex = 1, correctness = Correctness.INCORRECT.index, solveLogId = solveLogId, isFlag = true)
        ))

        // Attempt 2: Question 0 is Incorrect
        questionRepository.insertQuestions(listOf(
            Question(attempt = 2, questionIndex = 0, correctness = Correctness.INCORRECT.index, solveLogId = solveLogId)
        ))

        // Current Attempt is 3
        createViewModel(att = 3)
        
        val state = viewModel.uiState.first { it is SolveLogEditorUiState.Success } as SolveLogEditorUiState.Success
        
        // Question 0 archives: Correct (1), Incorrect (2) -> [1, 2]
        assertEquals(listOf(Correctness.CORRECT.index, Correctness.INCORRECT.index), state.correctnessArchives[0])
        
        // Question 1 archives: Incorrect (2) -> [2]
        assertEquals(listOf(Correctness.INCORRECT.index), state.correctnessArchives[1])

        // Question 1 hasCheckedFlag should be true because attempt 1 was flagged
        assertEquals(true, state.hasCheckedFlagMap[1])
        assertEquals(false, state.hasCheckedFlagMap[0])
    }
}
