package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.AnswerFormat
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = "w1000dp-h2000dp")
class SolveLogRouteAddBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `SolveLogAddContent should trigger onConfirm with correct values`() {
        var confirmedName: String? = null
        var confirmedQuantity: Int? = null
        var confirmedAnswerFormat: AnswerFormat? = null
        var confirmedType: SelectionType? = null
        var confirmedSelQuantity: SelectionQuantity? = null

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogAddContent(
                    onDismiss = {},
                    onConfirm = { name, qty, format, type, selQty ->
                        confirmedName = name
                        confirmedQuantity = qty
                        confirmedAnswerFormat = format
                        confirmedType = type
                        confirmedSelQuantity = selQty
                    }
                )
            }
        }

        // Input title
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.TITLE_FIELD).performTextInput("New Log")
        
        // Input quantity
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.QUANTITY_FIELD).performTextInput("50")

        // Select Type: Alphabet
        composeTestRule.onNode(hasText("ABC") and hasAnyAncestor(hasTestTag(SolveLogAddBottomSheetTestTags.TYPE_SELECTOR))).performClick()

        // Select Selection Quantity: 5
        composeTestRule.onNode(hasText("5") and hasAnyAncestor(hasTestTag(SolveLogAddBottomSheetTestTags.SELECTION_QUANTITY_SELECTOR))).performClick()

        // Click confirm
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.CONFIRM_BUTTON).performScrollTo().performClick()

        assertEquals("New Log", confirmedName)
        assertEquals(50, confirmedQuantity)
        assertEquals(AnswerFormat.MULTIPLE_CHOICE, confirmedAnswerFormat)
        assertEquals(SelectionType.ALPHABET, confirmedType)
        assertEquals(SelectionQuantity.FIVE, confirmedSelQuantity)
    }

    @Test
    fun `Confirm button should be disabled for invalid inputs`() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogAddContent(
                    onDismiss = {},
                    onConfirm = { _, _, _, _, _ -> }
                )
            }
        }

        // Initially disabled
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.CONFIRM_BUTTON).assertIsNotEnabled()

        // Only title -> disabled
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.TITLE_FIELD).performTextInput("Log")
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.CONFIRM_BUTTON).assertIsNotEnabled()

        // Quantity 0 -> disabled
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.QUANTITY_FIELD).performTextInput("0")
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.CONFIRM_BUTTON).assertIsNotEnabled()

        // Quantity 1 -> enabled
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.QUANTITY_FIELD).performTextReplacement("1")
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.CONFIRM_BUTTON).assertIsEnabled()

        // Quantity 1001 -> disabled
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.QUANTITY_FIELD).performTextReplacement("1001")
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.CONFIRM_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun `Selecting Correctness type should lock selection quantity to TWO`() {
        var confirmedSelQuantity: SelectionQuantity? = null
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SolveLogAddContent(
                    onDismiss = {},
                    onConfirm = { _, _, _, _, selQty -> confirmedSelQuantity = selQty }
                )
            }
        }

        // Select Correctness (◯ ×)
        composeTestRule.onNode(hasText("◯ ×") and hasAnyAncestor(hasTestTag(SolveLogAddBottomSheetTestTags.TYPE_SELECTOR)))
            .performClick()

        // Selection quantity selector's items should be disabled
        composeTestRule.onAllNodes(hasAnyAncestor(hasTestTag(SolveLogAddBottomSheetTestTags.SELECTION_QUANTITY_SELECTOR)) and hasClickAction())
            .assertAll(isNotEnabled())

        // Confirm
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.TITLE_FIELD).performTextInput("Test")
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.QUANTITY_FIELD).performTextInput("10")
        composeTestRule.onNodeWithTag(SolveLogAddBottomSheetTestTags.CONFIRM_BUTTON).performScrollTo().performClick()

        assertEquals(SelectionQuantity.TWO, confirmedSelQuantity)
    }
}
