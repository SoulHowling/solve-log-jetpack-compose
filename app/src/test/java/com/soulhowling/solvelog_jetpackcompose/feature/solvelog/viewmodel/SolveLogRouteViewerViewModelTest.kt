package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.Screens
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.AnswerFormat
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SolveLogRouteViewerViewModelTest {

    private lateinit var viewModel: SolveLogViewerViewModel
    private lateinit var application: SolveLogApplication
    private lateinit var database: SolveLogDatabase
    private lateinit var solveLogRepository: SolveLogRepository
    private lateinit var questionRepository: QuestionRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    private val solveLogId = 1

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        
        database = Room.inMemoryDatabaseBuilder(
            application,
            SolveLogDatabase::class.java
        ).allowMainThreadQueries().build()
        SolveLogDatabase.setTestInstance(database)

        solveLogRepository = SolveLogRepository(database.solveLogDao())
        questionRepository = QuestionRepository(database.questionDao())
    }

    @After
    fun tearDown() {
        database.close()
        SolveLogDatabase.setTestInstance(null)
        Dispatchers.resetMain()
    }

    private fun kotlinx.coroutines.test.TestScope.createViewModel(
        logId: Int = solveLogId
    ) {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                Screens.ARG_SOLVE_LOG_ID to logId
            )
        )
        viewModel = SolveLogViewerViewModel(application, savedStateHandle)
        // Subscribe to the flow to trigger WhileSubscribed
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun uiState_initialStateIsLoading() = runTest {
        createViewModel()
        assertEquals(SolveLogViewerUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_loadsDataSuccessfully() = runTest {
        val solveLog = SolveLog(
            id = solveLogId,
            name = "Test Log",
            questionQuantity = 2,
            answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
            selectionType = 0,
            selectionQuantity = 4,
            attemptCount = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            folderId = null
        )
        solveLogRepository.insertSolveLog(solveLog)

        val questions = listOf(
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, selectedAnswer = 0, correctness = Correctness.CORRECT.index, solveLogId = solveLogId),
            Question(attempt = 1, questionIndex = 1, freeAnswerContent = null, selectedAnswer = 1, correctness = Correctness.INCORRECT.index, solveLogId = solveLogId)
        )
        questionRepository.insertQuestions(questions)

        createViewModel()

        val state = viewModel.uiState.first { it !is SolveLogViewerUiState.Loading }
        assertTrue(state is SolveLogViewerUiState.Success)
        val successState = state as SolveLogViewerUiState.Success
        assertEquals("Test Log", successState.solveLog.name)
        assertEquals(2, successState.questions.size)
        assertEquals(1, successState.questions[0]?.size)
        assertEquals(1, successState.questions[1]?.size)
    }

    @Test
    fun toggleEditMode_changesState() = runTest {
        createViewModel()
        assertFalse(viewModel.isEditMode.value)

        viewModel.toggleEditMode()
        assertTrue(viewModel.isEditMode.value)

        viewModel.toggleEditMode()
        assertFalse(viewModel.isEditMode.value)
    }

    @Test
    fun toggleQuestionSelection_updatesSelectedIndices() = runTest {
        createViewModel()
        assertTrue(viewModel.selectedQuestionIndices.value.isEmpty())

        viewModel.toggleQuestionSelection(0)
        assertTrue(viewModel.selectedQuestionIndices.value.contains(0))

        viewModel.toggleQuestionSelection(1)
        assertTrue(viewModel.selectedQuestionIndices.value.contains(0))
        assertTrue(viewModel.selectedQuestionIndices.value.contains(1))

        viewModel.toggleQuestionSelection(0)
        assertFalse(viewModel.selectedQuestionIndices.value.contains(0))
        assertTrue(viewModel.selectedQuestionIndices.value.contains(1))
    }

    @Test
    fun startNewAttempt_incrementsAttemptCountAndCreatesQuestions() = runTest {
        val solveLog = SolveLog(
            id = solveLogId,
            name = "Test Log",
            questionQuantity = 2,
            answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
            selectionType = 0,
            selectionQuantity = 4,
            attemptCount = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            folderId = null
        )
        solveLogRepository.insertSolveLog(solveLog)
        questionRepository.insertQuestions(listOf(
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 1, questionIndex = 1, freeAnswerContent = null, solveLogId = solveLogId)
        ))

        createViewModel()
        
        // Wait for initial Success state and ensure uiState.value is updated
        viewModel.uiState.first { it is SolveLogViewerUiState.Success }
        assertTrue("ViewModel should be in Success state", viewModel.uiState.value is SolveLogViewerUiState.Success)

        var nextAttempt: Int? = null
        viewModel.startNewAttempt { nextAttempt = it }
        
        // Wait for attempt count to increase in the UI state
        viewModel.uiState.first { 
            (it as? SolveLogViewerUiState.Success)?.solveLog?.attemptCount == 2 
        }

        assertEquals("onComplete should be called with next attempt number", 2, nextAttempt)

        val updatedSolveLog = solveLogRepository.getSolveLog(solveLogId).first { it?.attemptCount == 2 }
        assertEquals(2, updatedSolveLog?.attemptCount)

        val allQuestions = questionRepository.getQuestionBy(solveLogId).first { it.size == 4 }
        assertEquals(4, allQuestions.size)
    }

    @Test
    fun addQuestion_incrementsQuestionQuantityAndCreatesQuestions() = runTest {
        val solveLog = SolveLog(
            id = solveLogId,
            name = "Test Log",
            questionQuantity = 2,
            answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
            selectionType = 0,
            selectionQuantity = 4,
            attemptCount = 2,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            folderId = null
        )
        solveLogRepository.insertSolveLog(solveLog)

        createViewModel()
        viewModel.uiState.first { it is SolveLogViewerUiState.Success }
        assertTrue(viewModel.uiState.value is SolveLogViewerUiState.Success)

        viewModel.addQuestion()
        
        viewModel.uiState.first { 
            (it as? SolveLogViewerUiState.Success)?.solveLog?.questionQuantity == 3
        }

        val updatedSolveLog = solveLogRepository.getSolveLog(solveLogId).first { it?.questionQuantity == 3 }
        assertEquals(3, updatedSolveLog?.questionQuantity)

        val allQuestions = questionRepository.getQuestionBy(solveLogId).first { it.size == 2 }
        assertEquals(2, allQuestions.size)
    }

    @Test
    fun deleteAttempt_decrementsAttemptCountAndDeletesQuestions() = runTest {
        val solveLog = SolveLog(
            id = solveLogId,
            name = "Test Log",
            questionQuantity = 1,
            answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
            selectionType = 0,
            selectionQuantity = 4,
            attemptCount = 2,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            folderId = null
        )
        solveLogRepository.insertSolveLog(solveLog)
        questionRepository.insertQuestions(listOf(
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 2, questionIndex = 0, freeAnswerContent = null, solveLogId = solveLogId)
        ))

        createViewModel()
        viewModel.uiState.first { it is SolveLogViewerUiState.Success }
        assertTrue(viewModel.uiState.value is SolveLogViewerUiState.Success)

        viewModel.deleteAttempt(2)
        
        viewModel.uiState.first { 
            (it as? SolveLogViewerUiState.Success)?.solveLog?.attemptCount == 1
        }

        val updatedSolveLog = solveLogRepository.getSolveLog(solveLogId).first { it?.attemptCount == 1 }
        assertEquals(1, updatedSolveLog?.attemptCount)

        val allQuestions = questionRepository.getQuestionBy(solveLogId).first { it.size == 1 }
        assertEquals(1, allQuestions.size)
    }

    @Test
    fun deleteSelectedQuestions_removesQuestionsAndReIndexes() = runTest {
        val solveLog = SolveLog(
            id = solveLogId,
            name = "Test Log",
            questionQuantity = 3,
            answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
            selectionType = 0,
            selectionQuantity = 4,
            attemptCount = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            folderId = null
        )
        solveLogRepository.insertSolveLog(solveLog)
        questionRepository.insertQuestions(listOf(
            Question(attempt = 1, questionIndex = 0, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 1, questionIndex = 1, freeAnswerContent = null, solveLogId = solveLogId),
            Question(attempt = 1, questionIndex = 2, freeAnswerContent = null, solveLogId = solveLogId)
        ))

        createViewModel()
        viewModel.uiState.first { it is SolveLogViewerUiState.Success }
        assertTrue(viewModel.uiState.value is SolveLogViewerUiState.Success)

        // Select index 1 to delete
        viewModel.toggleEditMode()
        viewModel.toggleQuestionSelection(1)
        viewModel.deleteSelectedQuestions()
        
        viewModel.uiState.first { 
            (it as? SolveLogViewerUiState.Success)?.solveLog?.questionQuantity == 2
        }

        val updatedSolveLog = solveLogRepository.getSolveLog(solveLogId).first { it?.questionQuantity == 2 }
        assertEquals(2, updatedSolveLog?.questionQuantity)

        val allQuestions = questionRepository.getQuestionBy(solveLogId).first { it.size == 2 }
        assertEquals(2, allQuestions.size)
        assertTrue(allQuestions.any { it.questionIndex == 0 })
        assertTrue(allQuestions.any { it.questionIndex == 1 })
    }

    @Test
    fun toggleAnswerVisibility_updatesStateAndPrefs() = runTest {
        createViewModel()
        val initialVisibility = viewModel.isAnswerVisible.value
        
        viewModel.toggleAnswerVisibility()
        assertEquals(!initialVisibility, viewModel.isAnswerVisible.value)
        
        // Verify SharedPref (indirectly by creating a new VM)
        val savedStateHandle = SavedStateHandle(mapOf(Screens.ARG_SOLVE_LOG_ID to solveLogId))
        val newViewModel = SolveLogViewerViewModel(application, savedStateHandle)
        assertEquals(!initialVisibility, newViewModel.isAnswerVisible.value)
    }
}
