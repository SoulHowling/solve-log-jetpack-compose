package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.AnswerFormat
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.Correctness
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
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
class SolveLogRouteEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSolveLog = SolveLog(
        id = 1,
        name = "Test Solve Log",
        questionQuantity = 3,
        answerFormat = AnswerFormat.MULTIPLE_CHOICE.index,
        selectionType = SelectionType.NUMBER.index,
        selectionQuantity = SelectionQuantity.FOUR.index,
        attemptCount = 1,
        createdAt = 0L,
        updatedAt = 0L,
        folderId = null
    )

    private val sampleQuestions = listOf(
        Question(id = 1, attempt = 1, questionIndex = 0, freeAnswerContent = null, selectedAnswer = 0, correctness = Correctness.CORRECT.index, solveLogId = 1),
        Question(id = 2, attempt = 1, questionIndex = 1, freeAnswerContent = null, selectedAnswer = 1, correctness = Correctness.INCORRECT.index, solveLogId = 1)
    )

    @Test
    fun `SolveLogEditorScreenContent should display header labels`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogEditorScreenContent(
                    solveLog = sampleSolveLog,
                    questions = emptyList(),
                    selectionTypeStringList = listOf("-", "1", "2", "3", "4"),
                    onUpdate = { _, _, _, _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("No.").assertIsDisplayed()
        composeTestRule.onNodeWithText("解答").assertIsDisplayed()
        composeTestRule.onNodeWithText("正誤").assertIsDisplayed()
        composeTestRule.onNodeWithText("フラグ").assertIsDisplayed()
    }

    @Test
    fun `SolveLogEditorScreenContent should display correct number of rows`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogEditorScreenContent(
                    solveLog = sampleSolveLog,
                    questions = emptyList(),
                    selectionTypeStringList = listOf("-", "1", "2", "3", "4"),
                    onUpdate = { _, _, _, _, _ -> }
                )
            }
        }

        // Check if rows are displayed via number text
        composeTestRule.onNodeWithText("1.").assertIsDisplayed()
        composeTestRule.onNodeWithText("2.").assertIsDisplayed()
        composeTestRule.onNodeWithText("3.").assertIsDisplayed()
    }

    @Test
    fun `CorrectIncorrectToggleButton should trigger onUpdate when clicked`() {
        var updatedCorrectness: Correctness? = null
        
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogEditorScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    selectionTypeStringList = listOf("-", "1", "2", "3", "4"),
                    onUpdate = { _, _, _, correctness, _ ->
                        updatedCorrectness = correctness
                    }
                )
            }
        }

        // Click "◯" in the second row (Number 2)
        composeTestRule.onNode(
            hasText(Correctness.CORRECT.symbol) and hasAnyAncestor(hasTestTag(SolveLogEditorTestTags.CORRECTNESS_TOGGLE + "2")),
            useUnmergedTree = true
        ).performClick()

        assertEquals(Correctness.CORRECT, updatedCorrectness)
    }

    @Test
    fun `Flag checkbox should trigger onUpdate when toggled`() {
        var updatedFlag: Boolean? = null
        
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogEditorScreenContent(
                    solveLog = sampleSolveLog,
                    questions = sampleQuestions,
                    selectionTypeStringList = listOf("-", "1", "2", "3", "4"),
                    onUpdate = { _, _, _, _, isFlag ->
                        updatedFlag = isFlag
                    }
                )
            }
        }

        // Toggle checkbox in the first row (Number 1)
        composeTestRule.onNodeWithTag(SolveLogEditorTestTags.FLAG_CHECKBOX + "1").performClick()

        assertTrue(updatedFlag == true)
    }

    @Test
    fun `Dropdown should show choices and trigger onUpdate when selected`() {
        var selectedAnswerIndex: Int? = null
        val selectionList = listOf("-", "Option A", "Option B", "Option C", "Option D")
        
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogEditorScreenContent(
                    solveLog = sampleSolveLog,
                    questions = emptyList(),
                    selectionTypeStringList = selectionList,
                    onUpdate = { _, answer, _, _, _ ->
                        selectedAnswerIndex = answer
                    }
                )
            }
        }

        // Open dropdown in first row (Number 1)
        composeTestRule.onNodeWithTag(SolveLogEditorTestTags.ANSWER_SELECTOR + "1").performClick()

        // Select an option
        composeTestRule.onNodeWithText("Option C").performClick()
        
        assertEquals(2, selectedAnswerIndex) // Index of "Option C" - 1 (offset)
    }

    @Test
    fun `CorrectnessArchive should be displayed when archives exist`() {
        val archives = mapOf(0 to listOf(Correctness.CORRECT.index, Correctness.INCORRECT.index))
        
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogEditorScreenContent(
                    solveLog = sampleSolveLog,
                    questions = emptyList(),
                    selectionTypeStringList = listOf("-", "1", "2", "3", "4"),
                    onUpdate = { _, _, _, _, _ -> },
                    correctnessArchives = archives
                )
            }
        }

        // Check if there are archive symbols
        composeTestRule.onAllNodesWithText(Correctness.CORRECT.symbol).onFirst().assertExists()
        composeTestRule.onAllNodesWithText(Correctness.INCORRECT.symbol).onFirst().assertExists()
    }
}
