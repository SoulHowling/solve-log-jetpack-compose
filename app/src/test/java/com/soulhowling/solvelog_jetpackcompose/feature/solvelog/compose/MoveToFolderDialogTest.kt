package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MoveToFolderDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testFolders = listOf(
        Folder(id = 1, name = "Folder A", parentFolderId = null, createdAt = 0, updatedAt = 0),
        Folder(id = 2, name = "Folder B", parentFolderId = null, createdAt = 0, updatedAt = 0),
        Folder(id = 3, name = "Sub A-1", parentFolderId = 1, createdAt = 0, updatedAt = 0)
    )

    @Test
    fun `MoveToFolderContent should show root folders initially`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MoveToFolderContent(
                    allFolders = testFolders,
                    excludeFolderId = null,
                    currentViewedFolderId = null,
                    onViewFolder = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Folder A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Folder B").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sub A-1").assertDoesNotExist()
    }

    @Test
    fun `Clicking folder item should trigger onViewFolder`() {
        var viewedFolderId: Int? = -1
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MoveToFolderContent(
                    allFolders = testFolders,
                    excludeFolderId = null,
                    currentViewedFolderId = null,
                    onViewFolder = { viewedFolderId = it }
                )
            }
        }

        composeTestRule.onNodeWithTag(MoveToFolderDialogTestTags.FOLDER_ITEM + "1").performClick()
        assertEquals(1, viewedFolderId)
    }

    @Test
    fun `Back button should show when in subfolder and trigger onViewFolder`() {
        var viewedFolderId: Int? = 1
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MoveToFolderContent(
                    allFolders = testFolders,
                    excludeFolderId = null,
                    currentViewedFolderId = 1,
                    onViewFolder = { viewedFolderId = it }
                )
            }
        }

        // Should show Sub A-1
        composeTestRule.onNodeWithText("Sub A-1").assertIsDisplayed()
        
        // Click back
        composeTestRule.onNodeWithTag(MoveToFolderDialogTestTags.BACK_BUTTON).performClick()
        assertEquals(null, viewedFolderId)
    }

    @Test
    fun `excludeFolderId and its children should be hidden`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MoveToFolderContent(
                    allFolders = testFolders,
                    excludeFolderId = 1, // Exclude Folder A and its child Sub A-1
                    currentViewedFolderId = null,
                    onViewFolder = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Folder B").assertIsDisplayed()
        composeTestRule.onNodeWithText("Folder A").assertDoesNotExist()
    }

    @Test
    fun `Confirm button in dialog should trigger onConfirm with current folder id`() {
        var confirmedFolderId: Int? = -1
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MoveToFolderDialog(
                    allFolders = testFolders,
                    onDismiss = {},
                    onConfirm = { confirmedFolderId = it }
                )
            }
        }

        // Navigate to Folder A
        composeTestRule.onNodeWithTag(MoveToFolderDialogTestTags.FOLDER_ITEM + "1").performClick()
        
        // Wait, the dialog state 'currentViewedFolderId' is internal to the dialog.
        // My test setup above for content works for verify callbacks, 
        // but for the Dialog wrapper I need to check if confirm uses that internal state.
        
        // Note: The confirmedFolderId will be null if I don't navigate, but my test tag click logic happens within the Dialog's content.
        
        // Click Move Here
        composeTestRule.onNodeWithTag(MoveToFolderDialogTestTags.CONFIRM_BUTTON).performClick()
        
        // Since we clicked Folder A, internal state should be 1.
        assertEquals(1, confirmedFolderId)
    }
}
