package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel.SolveLogListItem
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SolveLogListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleFolder = Folder(id = 1, name = "Test Folder", parentFolderId = null, createdAt = 0, updatedAt = 0)
    private val sampleLog = SolveLog(
        id = 2,
        name = "Test Log",
        questionQuantity = 10,
        selectionType = SelectionType.NUMBER.index,
        selectionQuantity = SelectionQuantity.FOUR.index,
        attemptCount = 1,
        createdAt = 0,
        updatedAt = 0,
        folderId = null
    )

    @Test
    fun `SolveLogListScreenContent should display folder and log items`() {
        val items = listOf(
            SolveLogListItem.FolderItem(sampleFolder),
            SolveLogListItem.LogItem(sampleLog)
        )

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogListScreenContent(
                    displayItems = items,
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onNavigateToViewer = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddSolveLog = { _, _, _, _ -> },
                    onAddFolder = {},
                    onDeleteSolveLog = {},
                    onDeleteFolder = {},
                    onMoveSolveLog = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateSolveLog = { _, _, _, _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportLog = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogListTestTags.FOLDER_ITEM + "1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Folder").assertIsDisplayed()
        
        composeTestRule.onNodeWithTag(SolveLogListTestTags.LOG_ITEM + "2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Log").assertIsDisplayed()
    }

    @Test
    fun `Clicking folder should trigger onSelectFolder`() {
        var selectedFolderId: Int? = null
        val items = listOf(SolveLogListItem.FolderItem(sampleFolder))

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogListScreenContent(
                    displayItems = items,
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onNavigateToViewer = {},
                    onSelectFolder = { selectedFolderId = it },
                    onNavigateBack = {},
                    onAddSolveLog = { _, _, _, _ -> },
                    onAddFolder = {},
                    onDeleteSolveLog = {},
                    onDeleteFolder = {},
                    onMoveSolveLog = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateSolveLog = { _, _, _, _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportLog = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogListTestTags.FOLDER_ITEM + "1").performClick()
        assertEquals(1, selectedFolderId)
    }

    @Test
    fun `Clicking log should trigger onNavigateToViewer`() {
        var navigatedLogId: Int? = null
        val items = listOf(SolveLogListItem.LogItem(sampleLog))

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogListScreenContent(
                    displayItems = items,
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onNavigateToViewer = { navigatedLogId = it },
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddSolveLog = { _, _, _, _ -> },
                    onAddFolder = {},
                    onDeleteSolveLog = {},
                    onDeleteFolder = {},
                    onMoveSolveLog = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateSolveLog = { _, _, _, _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportLog = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogListTestTags.LOG_ITEM + "2").performClick()
        assertEquals(2, navigatedLogId)
    }

    @Test
    fun `FAB expansion should show add options`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogListScreenContent(
                    displayItems = emptyList(),
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onNavigateToViewer = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddSolveLog = { _, _, _, _ -> },
                    onAddFolder = {},
                    onDeleteSolveLog = {},
                    onDeleteFolder = {},
                    onMoveSolveLog = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateSolveLog = { _, _, _, _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportLog = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithTag(SolveLogListTestTags.ADD_FAB).performClick()
        
        composeTestRule.onNodeWithTag(SolveLogListTestTags.ADD_FOLDER_FAB).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SolveLogListTestTags.ADD_LOG_FAB).assertIsDisplayed()
    }

    @Test
    fun `CurrentFolderHeader should be displayed when currentFolderPath is not null`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogListScreenContent(
                    displayItems = emptyList(),
                    folders = emptyList(),
                    currentFolderPath = "Root / Parent",
                    isCardView = true,
                    isSortMode = false,
                    onNavigateToViewer = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddSolveLog = { _, _, _, _ -> },
                    onAddFolder = {},
                    onDeleteSolveLog = {},
                    onDeleteFolder = {},
                    onMoveSolveLog = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateSolveLog = { _, _, _, _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportLog = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Root / Parent").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SolveLogListTestTags.BACK_BUTTON).assertIsDisplayed()
    }
}
