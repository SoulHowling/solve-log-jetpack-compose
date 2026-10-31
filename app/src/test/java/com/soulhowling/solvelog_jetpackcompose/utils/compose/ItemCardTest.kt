package com.soulhowling.solvelog_jetpackcompose.utils.compose

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class ItemCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun itemCard_rendersContent_andHandlesOnClick() {
        var cardClicked = false

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                ItemCard(
                    icon = Icons.Default.Folder,
                    onClick = { cardClicked = true }
                ) {
                    Text(text = "My Card Content")
                }
            }
        }

        composeTestRule.onNodeWithText("My Card Content").assertIsDisplayed()
        composeTestRule.onNodeWithText("My Card Content").performClick()
        assertTrue(cardClicked)
    }

    @Test
    fun itemOverflowMenu_opensMenuAndTriggersCallbacks() {
        var editClicked = false
        var moveClicked = false
        var deleteClicked = false
        var exportClicked = false

        val moreOptionsCd = context.getString(R.string.log_list_cd_more_options)
        val editLabel = context.getString(R.string.log_list_dropdown_menu_item_edit)
        val moveLabel = context.getString(R.string.log_list_dropdown_menu_item_move)
        val deleteLabel = context.getString(R.string.log_list_dropdown_menu_item_delete)
        val exportLabel = context.getString(R.string.log_list_dropdown_menu_item_export)

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                ItemOverflowMenu(
                    onEdit = { editClicked = true },
                    onMove = { moveClicked = true },
                    onDelete = { deleteClicked = true },
                    onExport = { exportClicked = true }
                )
            }
        }

        // Test Edit
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()
        composeTestRule.onNodeWithText(editLabel).performClick()
        assertTrue(editClicked)

        // Test Move
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()
        composeTestRule.onNodeWithText(moveLabel).performClick()
        assertTrue(moveClicked)

        // Test Delete
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()
        composeTestRule.onNodeWithText(deleteLabel).performClick()
        assertTrue(deleteClicked)

        // Test Export
        composeTestRule.onNodeWithContentDescription(moreOptionsCd).performClick()
        composeTestRule.onNodeWithText(exportLabel).performClick()
        assertTrue(exportClicked)
    }

    @Test
    fun itemDragIndicator_rendersWithoutCrashing() {
        val dragHandleCd = context.getString(R.string.log_list_cd_drag_handle)

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                ItemDragIndicator()
            }
        }

        composeTestRule.onNodeWithContentDescription(dragHandleCd).assertIsDisplayed()
    }
}
