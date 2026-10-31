package com.soulhowling.solvelog_jetpackcompose.utils.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class FolderHeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun currentFolderHeader_displaysFolderPath_andTriggersBackClick() {
        var backClicked = false

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                CurrentFolderHeader(
                    currentFolderPath = "Home / SubFolder",
                    onNavigateBack = { backClicked = true },
                    backButtonTestTag = "back_btn"
                )
            }
        }

        composeTestRule.onNodeWithText("Home / SubFolder").assertIsDisplayed()

        composeTestRule.onNodeWithTag("back_btn").performClick()
        assertTrue(backClicked)
    }
}
