package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SolveLogViewerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSolveLog = SolveLog(
        id = 1,
        name = "Test Solve Log",
        questionQuantity = 3,
        selectionType = 0,
        selectionQuantity = 4,
        attemptCount = 2,
        createdAt = 0L,
        updatedAt = 0L,
        folderId = null
    )

    private val sampleQuestions = mapOf(
        0 to listOf(
            Question(id = 1, attempt = 1, questionIndex = 0, selectedAnswer = 0, correctness = Correctness.CORRECT.index, solveLogId = 1),
            Question(id = 2, attempt = 2, questionIndex = 0, selectedAnswer = 1, correctness = Correctness.INCORRECT.index, solveLogId = 1)
        ),
        1 to listOf(
            Question(id = 3, attempt = 1, questionIndex = 1, selectedAnswer = 1, correctness = Correctness.INCORRECT.index, solveLogId = 1)
        )
    )

    @Test
    fun `SolveLogViewerScreenContent should display attempt headers`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogViewerScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    isEditMode = false,
                    selectedIndices = emptySet(),
                    isAnswerVisible = true,
                    onNavigateToEditor = {},
                    onStartNewAttempt = {},
                    onAddQuestion = {},
                    onToggleSelection = {},
                    onDeleteAttempt = {},
                    onAnswerVisibility = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.ATTEMPT_HEADER + "1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.ATTEMPT_HEADER + "2").assertIsDisplayed()
    }

    @Test
    fun `SolveLogViewerScreenContent should display correct number of rows and question numbers`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogViewerScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    isEditMode = false,
                    selectedIndices = emptySet(),
                    isAnswerVisible = true,
                    onNavigateToEditor = {},
                    onStartNewAttempt = {},
                    onAddQuestion = {},
                    onToggleSelection = {},
                    onDeleteAttempt = {},
                    onAnswerVisibility = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.QUESTION_LABEL + "1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.QUESTION_LABEL + "2").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.QUESTION_LABEL + "3").assertIsDisplayed()
    }

    @Test
    fun `Visibility toggle should trigger callback`() {
        var toggleCalled = false
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogViewerScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    isEditMode = false,
                    selectedIndices = emptySet(),
                    isAnswerVisible = true,
                    onNavigateToEditor = {},
                    onStartNewAttempt = {},
                    onAddQuestion = {},
                    onToggleSelection = {},
                    onDeleteAttempt = {},
                    onAnswerVisibility = { toggleCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.VISIBILITY_TOGGLE).performClick()
        assertTrue(toggleCalled)
    }

    @Test
    fun `FAB should trigger startNewAttempt callback`() {
        var startAttemptCalled = false
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogViewerScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    isEditMode = false,
                    selectedIndices = emptySet(),
                    isAnswerVisible = true,
                    onNavigateToEditor = {},
                    onStartNewAttempt = { startAttemptCalled = true },
                    onAddQuestion = {},
                    onToggleSelection = {},
                    onDeleteAttempt = {},
                    onAnswerVisibility = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.FAB_NEW_ATTEMPT).performClick()
        assertTrue(startAttemptCalled)
    }

    @Test
    fun `Add question row should trigger callback`() {
        var addQuestionCalled = false
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogViewerScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    isEditMode = false,
                    selectedIndices = emptySet(),
                    isAnswerVisible = true,
                    onNavigateToEditor = {},
                    onStartNewAttempt = {},
                    onAddQuestion = { addQuestionCalled = true },
                    onToggleSelection = {},
                    onDeleteAttempt = {},
                    onAnswerVisibility = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.ADD_QUESTION_ROW).performClick()
        assertTrue(addQuestionCalled)
    }

    @Test
    fun `Edit mode should show checkboxes and delete attempt buttons`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogViewerScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    isEditMode = true,
                    selectedIndices = setOf(0),
                    isAnswerVisible = true,
                    onNavigateToEditor = {},
                    onStartNewAttempt = {},
                    onAddQuestion = {},
                    onToggleSelection = {},
                    onDeleteAttempt = {},
                    onAnswerVisibility = {}
                )
            }
        }

        // Check for checkboxes (QuestionSelection tags)
        // qIndex 0 -> number 1. selectedIndices contains 0, so it should be On.
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.QUESTION_SELECTION + "1").assertIsDisplayed().assertIsOn()
        // qIndex 1 -> number 2. not selected, so it should be Off.
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.QUESTION_SELECTION + "2").assertIsOff()

        // Check for delete attempt buttons
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.DELETE_ATTEMPT + "1").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.DELETE_ATTEMPT + "2").assertIsDisplayed()
    }

    @Test
    fun `Clicking cell should navigate to editor when not in edit mode`() {
        var navigatedAttempt: Int? = null
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogViewerScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    isEditMode = false,
                    selectedIndices = emptySet(),
                    isAnswerVisible = true,
                    onNavigateToEditor = { navigatedAttempt = it },
                    onStartNewAttempt = {},
                    onAddQuestion = {},
                    onToggleSelection = {},
                    onDeleteAttempt = {},
                    onAnswerVisibility = {}
                )
            }
        }

        // Click cell for Question 1, Attempt 2
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.ITEM_CELL + "0_2").performClick()
        assertEquals(2, navigatedAttempt)
    }

    @Test
    fun `Score footer should display correct scores`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogViewerScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    isEditMode = false,
                    selectedIndices = emptySet(),
                    isAnswerVisible = true,
                    onNavigateToEditor = {},
                    onStartNewAttempt = {},
                    onAddQuestion = {},
                    onToggleSelection = {},
                    onDeleteAttempt = {},
                    onAnswerVisibility = {}
                )
            }
        }

        // Attempt 1: Question 0 is Correct, Question 1 is Incorrect. Total 3 questions.
        // Score should be 1/3.
        // Attempt 2: Question 0 is Incorrect. Total 3 questions.
        // Score should be 0/3.
        
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.SCORE_FOOTER + "1", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(
            hasTestTag(SolveLogViewerTestTags.SCORE_FOOTER + "1") and hasAnyDescendant(hasText("1/3")),
            useUnmergedTree = true
        ).assertExists()
        
        composeTestRule.onNodeWithTag(SolveLogViewerTestTags.SCORE_FOOTER + "2", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNode(
            hasTestTag(SolveLogViewerTestTags.SCORE_FOOTER + "2") and hasAnyDescendant(hasText("0/3")),
            useUnmergedTree = true
        ).assertExists()
    }
}
