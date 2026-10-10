package com.soulhowling.solvelog_jetpackcompose.feature.memo.compose

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel.MemoListItem
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.FolderAddDialogTestTags
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.MoveToFolderDialogTestTags
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MemoListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val sampleFolder = Folder(id = 1, name = "Sample Folder", createdAt = 0, updatedAt = 0, parentFolderId = null)
    private val targetFolder = Folder(id = 2, name = "Target Folder", createdAt = 0, updatedAt = 0, parentFolderId = null)
    private val sampleMemo = Memo(id = 10, name = "Sample Memo Title", content = "Sample Memo Content", createdAt = 0, updatedAt = 0, folderId = null)

    @Test
    fun `displays empty message when items list is empty`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = emptyList(),
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> },
                )
            }
        }

        val emptyMsg = context.getString(R.string.memo_list_empty_message)
        composeTestRule.onNodeWithText(emptyMsg).assertIsDisplayed()
    }

    @Test
    fun `displays folder and memo items in Card view`() {
        val items = listOf(
            MemoListItem.FolderItem(sampleFolder),
            MemoListItem.MemoItem(sampleMemo)
        )

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = items,
                    folders = listOf(sampleFolder),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Sample Folder").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sample Memo Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sample Memo Content").assertIsDisplayed()
    }

    @Test
    fun `displays folder and memo items in List view`() {
        val items = listOf(
            MemoListItem.FolderItem(sampleFolder),
            MemoListItem.MemoItem(sampleMemo)
        )

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = items,
                    folders = listOf(sampleFolder),
                    currentFolderPath = null,
                    isCardView = false,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Sample Folder").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sample Memo Title").assertIsDisplayed()
    }

    @Test
    fun `clicking folder item triggers onSelectFolder`() {
        var selectedFolderId: Int? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = listOf(MemoListItem.FolderItem(sampleFolder)),
                    folders = listOf(sampleFolder),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = { selectedFolderId = it },
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Sample Folder").performClick()
        assertEquals(1, selectedFolderId)
    }

    @Test
    fun `clicking memo item triggers onMemoClick`() {
        var clickedMemo: Memo? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = listOf(MemoListItem.MemoItem(sampleMemo)),
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = { clickedMemo = it },
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Sample Memo Title").performClick()
        assertNotNull(clickedMemo)
        assertEquals(10, clickedMemo?.id)
    }

    @Test
    fun `displays currentFolderPath header and triggers onNavigateBack`() {
        var navigateBackCalled = false

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = emptyList(),
                    folders = listOf(sampleFolder),
                    currentFolderPath = "Folder A / Folder B",
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = { navigateBackCalled = true },
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Folder A / Folder B").assertIsDisplayed()

        val backCd = context.getString(R.string.log_list_cd_back)
        composeTestRule.onNodeWithContentDescription(backCd).performClick()
        assertTrue(navigateBackCalled)
    }

    @Test
    fun `expanding FAB speed dial and clicking New Folder opens dialog and triggers onAddFolder`() {
        var addedFolderName: String? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = emptyList(),
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = { addedFolderName = it },
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        // Click main FAB
        val openMenuCd = context.getString(R.string.log_list_cd_open_menu)
        composeTestRule.onNodeWithContentDescription(openMenuCd).performClick()

        // Click Add Folder speed dial FAB
        val addFolderCd = context.getString(R.string.log_list_cd_add_folder)
        composeTestRule.onNodeWithContentDescription(addFolderCd).performClick()

        // Type new folder name in dialog
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.TEXT_FIELD).performTextInput("New Folder Name")
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.CONFIRM_BUTTON).performClick()

        assertEquals("New Folder Name", addedFolderName)
    }

    @Test
    fun `expanding FAB speed dial and clicking New Memo triggers onAddMemoClick`() {
        var addMemoClicked = false

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = emptyList(),
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = { addMemoClicked = true },
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        // Click main FAB
        val openMenuCd = context.getString(R.string.log_list_cd_open_menu)
        composeTestRule.onNodeWithContentDescription(openMenuCd).performClick()

        // Click Add Memo speed dial FAB
        val addMemoCd = context.getString(R.string.talkback_new_memo_button)
        composeTestRule.onNodeWithContentDescription(addMemoCd).performClick()

        assertTrue(addMemoClicked)
    }

    @Test
    fun `overflow menu on memo item triggers onDeleteMemo and onExportMemo`() {
        var deletedMemo: Memo? = null
        var exportedMemo: Memo? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = listOf(MemoListItem.MemoItem(sampleMemo)),
                    folders = emptyList(),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = { deletedMemo = it },
                    onDeleteFolder = {},
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = { exportedMemo = it },
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        // Open overflow menu
        val moreOptionsCd = context.getString(R.string.log_list_cd_more_options)
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()

        // Click Export
        val exportText = context.getString(R.string.log_list_dropdown_menu_item_export)
        composeTestRule.onNode(hasText(exportText), useUnmergedTree = true).performClick()
        assertEquals(sampleMemo.id, exportedMemo?.id)

        // Open overflow menu again and click Delete
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()
        val deleteText = context.getString(R.string.log_list_dropdown_menu_item_delete)
        composeTestRule.onNode(hasText(deleteText), useUnmergedTree = true).performClick()
        assertEquals(sampleMemo.id, deletedMemo?.id)
    }

    @Test
    fun `overflow menu Move on memo item opens MoveToFolderDialog and triggers onMoveMemo`() {
        var movedMemo: Memo? = null
        var moveTargetFolderId: Int? = -1

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = listOf(MemoListItem.MemoItem(sampleMemo)),
                    folders = listOf(targetFolder),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = {},
                    onMoveMemo = { memo, targetId ->
                        movedMemo = memo
                        moveTargetFolderId = targetId
                    },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { _, _ -> },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        // Open overflow menu
        val moreOptionsCd = context.getString(R.string.log_list_cd_more_options)
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()

        // Click Move
        val moveText = context.getString(R.string.log_list_dropdown_menu_item_move)
        composeTestRule.onNode(hasText(moveText), useUnmergedTree = true).performClick()

        // Select target folder in dialog
        composeTestRule.onNodeWithTag(MoveToFolderDialogTestTags.FOLDER_ITEM + "2", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithTag(MoveToFolderDialogTestTags.CONFIRM_BUTTON, useUnmergedTree = true).performClick()

        assertEquals(sampleMemo.id, movedMemo?.id)
        assertEquals(2, moveTargetFolderId)
    }

    @Test
    fun `overflow menu on folder item triggers onUpdateFolder and onDeleteFolder`() {
        var updatedFolder: Folder? = null
        var newFolderName: String? = null
        var deletedFolder: Folder? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                MemoListScreenContent(
                    displayItems = listOf(MemoListItem.FolderItem(sampleFolder)),
                    folders = listOf(sampleFolder),
                    currentFolderPath = null,
                    isCardView = true,
                    isSortMode = false,
                    onMemoClick = {},
                    onSelectFolder = {},
                    onNavigateBack = {},
                    onAddMemoClick = {},
                    onAddFolder = {},
                    onDeleteMemo = {},
                    onDeleteFolder = { deletedFolder = it },
                    onMoveMemo = { _, _ -> },
                    onMoveFolder = { _, _ -> },
                    onUpdateFolder = { folder, name ->
                        updatedFolder = folder
                        newFolderName = name
                    },
                    onExportMemo = {},
                    onExportFolder = {},
                    onReorder = { _, _ -> }
                )
            }
        }

        // Open overflow menu
        val moreOptionsCd = context.getString(R.string.log_list_cd_more_options)
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()

        // Click Edit
        val editText = context.getString(R.string.log_list_dropdown_menu_item_edit)
        composeTestRule.onNode(hasText(editText), useUnmergedTree = true).performClick()

        // Type new name in FolderAddDialog and confirm
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.TEXT_FIELD).performTextReplacement("Updated Folder")
        composeTestRule.onNodeWithTag(FolderAddDialogTestTags.CONFIRM_BUTTON).performClick()

        assertEquals(sampleFolder.id, updatedFolder?.id)
        assertEquals("Updated Folder", newFolderName)

        // Open overflow menu again and click Delete
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()
        val deleteText = context.getString(R.string.log_list_dropdown_menu_item_delete)
        composeTestRule.onNode(hasText(deleteText), useUnmergedTree = true).performClick()
        assertEquals(sampleFolder.id, deletedFolder?.id)
    }
}
