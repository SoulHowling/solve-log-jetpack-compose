package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class StopwatchPageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `StopwatchPageScreenContent should display correct time`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                StopwatchPageScreenContent(
                    displayTime = 125430L,
                    isRunning = false,
                    isPaused = false,
                    isVertical = true,
                    onToggleStartStop = {},
                    onFormatTime = { "02:05.43" },
                    onReset = {}
                )
            }
        }

        composeTestRule.onNodeWithText("02:05.43").assertIsDisplayed()
    }

    @Test
    fun `StopwatchPageScreenContent should show Start button when not running and not paused`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                StopwatchPageScreenContent(
                    displayTime = 0L,
                    isRunning = false,
                    isPaused = false,
                    isVertical = true,
                    onToggleStartStop = {},
                    onFormatTime = { "00:00.00" },
                    onReset = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reset").assertDoesNotExist()
    }

    @Test
    fun `StopwatchPageScreenContent should show Stop and Reset buttons when running`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                StopwatchPageScreenContent(
                    displayTime = 1000L,
                    isRunning = true,
                    isPaused = false,
                    isVertical = true,
                    onToggleStartStop = {},
                    onFormatTime = { "00:01.00" },
                    onReset = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Stop").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reset").assertIsDisplayed()
    }

    @Test
    fun `StopwatchPageScreenContent should show Resume and Reset buttons when paused`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                StopwatchPageScreenContent(
                    displayTime = 1000L,
                    isRunning = false,
                    isPaused = true,
                    isVertical = true,
                    onToggleStartStop = {},
                    onFormatTime = { "00:01.00" },
                    onReset = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Resume").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reset").assertIsDisplayed()
    }

    @Test
    fun `Reset button click should trigger onReset`() {
        var resetClicked = false
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                StopwatchPageScreenContent(
                    displayTime = 1000L,
                    isRunning = false,
                    isPaused = true,
                    isVertical = true,
                    onToggleStartStop = {},
                    onFormatTime = { "00:01.00" },
                    onReset = { resetClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Reset").performClick()
        Assert.assertTrue(resetClicked)
    }
}