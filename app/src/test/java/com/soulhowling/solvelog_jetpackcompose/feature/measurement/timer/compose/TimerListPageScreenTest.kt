package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.ActiveTimer
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class TimerListPageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `TimerListPageScreenContent should display list of timers`() {
        val timers = listOf(
            ActiveTimer(
                state = TimerState(id = 1, title = "Timer 1", workSet = 4, workSeconds = 60, breakSeconds = 30),
                remainingSeconds = 60
            ),
            ActiveTimer(
                state = TimerState(id = 2, title = "Timer 2", workSet = 8, workSeconds = 20, breakSeconds = 10),
                remainingSeconds = 20
            )
        )

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                TimerListPageScreenContent(
                    timers = timers,
                    activeTimerId = null,
                    onAddTimer = { _, _, _, _, _, _ -> },
                    onUpdateTimer = { _, _, _, _, _, _, _ -> },
                    onDeleteTimer = {},
                    onClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Timer 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Timer 2").assertIsDisplayed()
    }

    @Test
    fun `TimerListPageScreenContent should call onClicked when item is clicked`() {
        var clickedId = -1
        val timers = listOf(
            ActiveTimer(
                state = TimerState(id = 1, title = "Timer 1", workSet = 4, workSeconds = 60, breakSeconds = 30),
                remainingSeconds = 60
            )
        )

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                TimerListPageScreenContent(
                    timers = timers,
                    activeTimerId = null,
                    onAddTimer = { _, _, _, _, _, _ -> },
                    onUpdateTimer = { _, _, _, _, _, _, _ -> },
                    onDeleteTimer = {},
                    onClicked = { clickedId = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Timer 1").performClick()
        assertEquals(1, clickedId)
    }

    @Test
    fun `TimerListPageScreenContent should show add dialog when FAB is clicked`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                TimerListPageScreenContent(
                    timers = emptyList(),
                    activeTimerId = null,
                    onAddTimer = { _, _, _, _, _, _ -> },
                    onUpdateTimer = { _, _, _, _, _, _, _ -> },
                    onDeleteTimer = {},
                    onClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Floating Action Button").performClick()
        
        // Verify dialog is shown (Add Timer title)
        composeTestRule.onNodeWithText("Add Timer", substring = true).assertIsDisplayed()
    }
}
