package com.soulhowling.solvelog_jetpackcompose.utils.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SingleChoiceSegmentedButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun singleChoiceSegmentedButton_rendersItems_andTriggersSelectionChange() {
        var selectedIndex = 0
        val items = listOf("Option A", "Option B", "Option C")

        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SingleChoiceSegmentedButton(
                    itemList = items,
                    selectedIndex = selectedIndex,
                    onSelectedIndexChange = { selectedIndex = it },
                    isEnabled = true
                )
            }
        }

        composeTestRule.onNodeWithText("Option A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Option B").assertIsDisplayed()

        composeTestRule.onNodeWithText("Option B").performClick()
        assertEquals(1, selectedIndex)
    }
}
