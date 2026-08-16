package com.soulhowling.solvelog_jetpackcompose.feature.measurement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = SolveLogApplication::class)
class MeasurementScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `MeasurementScreen should display tabs for Stopwatch and Repeat timer`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MeasurementScreen(onNavigateToTimerPageScreen = {})
            }
        }

        // Verify tabs are displayed
        composeTestRule.onNodeWithText("Stopwatch").assertIsDisplayed()
        composeTestRule.onNodeWithText("Repeat timer").assertIsDisplayed()
    }

    @Test
    fun `Stopwatch tab should be selected by default`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MeasurementScreen(onNavigateToTimerPageScreen = {})
            }
        }

        composeTestRule.onNodeWithText("Stopwatch").assertIsSelected()
        composeTestRule.onNodeWithText("Repeat timer").assertIsNotSelected()
    }

    @Test
    fun `clicking Repeat timer tab should switch the selected tab`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MeasurementScreen(onNavigateToTimerPageScreen = {})
            }
        }

        composeTestRule.onNodeWithText("Repeat timer").performClick()
        
        composeTestRule.onNodeWithText("Repeat timer").assertIsSelected()
        composeTestRule.onNodeWithText("Stopwatch").assertIsNotSelected()
    }

    @Test
    fun `MeasurementScreen should initially show Stopwatch content`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MeasurementScreen(onNavigateToTimerPageScreen = {})
            }
        }

        // Stopwatch screen should have the Start button
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
    }
}
