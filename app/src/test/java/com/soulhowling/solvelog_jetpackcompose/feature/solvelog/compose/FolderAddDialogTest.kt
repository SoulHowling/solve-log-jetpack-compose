package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class FolderAddDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `FolderAddDialog should show initial name and trigger onConfirm`() {
        var confirmedName: String? = null
        
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                FolderAddDialog(
                    initialName = "Initial Folder",
                    onDismiss = {},
                    onConfirm = { confirmedName = it }
                )
            }
        }

        // Check if initial name is displayed
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.TEXT_FIELD).assertTextContains("Initial Folder")

        // Click confirm
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.CONFIRM_BUTTON).performClick()

        assertEquals("Initial Folder", confirmedName)
    }

    @Test
    fun `Confirm button should be disabled when text is blank`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                FolderAddDialog(
                    initialName = "",
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        // Initially empty, should be disabled
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.CONFIRM_BUTTON).assertIsNotEnabled()

        // Type spaces
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.TEXT_FIELD).performTextInput("   ")
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.CONFIRM_BUTTON).assertIsNotEnabled()

        // Type text
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.TEXT_FIELD).performTextInput("New Folder")
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.CONFIRM_BUTTON).assertIsEnabled()
    }

    @Test
    fun `Dismiss button should trigger onDismiss`() {
        var dismissed = false
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                FolderAddDialog(
                    onDismiss = { dismissed = true },
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.CANCEL_BUTTON).performClick()
        assertTrue(dismissed)
    }
}
