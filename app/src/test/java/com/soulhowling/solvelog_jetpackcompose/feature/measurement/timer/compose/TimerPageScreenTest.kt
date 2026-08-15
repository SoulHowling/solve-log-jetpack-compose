package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.ActiveTimer
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerPhase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class TimerPageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `TimerPageScreenContent should display correct initial work state`() {
        val timerState = TimerState(
            id = 1,
            title = "Test Workout",
            workSet = 4,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.WORK
        )
        val timer = ActiveTimer(
            state = timerState,
            initialSeconds = 60,
            remainingSeconds = 60,
            isRunning = false
        )

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                TimerPageScreenContent(
                    timer = timer,
                    totalSets = 4,
                    onStart = {},
                    onReset = {},
                    onRestart = {},
                    isVertical = true
                )
            }
        }

        // Verify title
        composeTestRule.onNodeWithText("Test Workout").assertIsDisplayed()
        
        // Verify time format (60s -> 01:00)
        composeTestRule.onNodeWithText("01:00").assertIsDisplayed()
        
        // Verify set text (Set 1/4)
        composeTestRule.onNodeWithText("1/4", substring = true).assertIsDisplayed()
    }

    @Test
    fun `TimerPageScreenContent should display correct break state`() {
        val timerState = TimerState(
            id = 1,
            title = "Test Workout",
            workSet = 2,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.BREAK
        )
        val timer = ActiveTimer(
            state = timerState,
            initialSeconds = 30,
            remainingSeconds = 15,
            isRunning = true
        )

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                TimerPageScreenContent(
                    timer = timer,
                    totalSets = 4,
                    onStart = {},
                    onReset = {},
                    onRestart = {},
                    isVertical = true
                )
            }
        }

        // Verify time format (15s -> 00:15)
        composeTestRule.onNodeWithText("00:15").assertIsDisplayed()
        
        // Verify set text (WORK 2/4 -> BREAK 2/4)
        // totalSets(4) - workSet(2) = 2
        composeTestRule.onNodeWithText("2/4", substring = true).assertIsDisplayed()
    }
}
