package com.soulhowling.solvelog_jetpackcompose.feature.settings.wipedata

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class WipeDataScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun wipeDataContent_itemsDisplayed() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                WipeDataContent(
                    wipeStatus = WipeDataViewModel.WipeStatus.Idle,
                    onWipeConfirm = {},
                    onResetWipeStatus = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.wipe_data_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.wipe_data_logs)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.wipe_data_timers)).assertIsDisplayed()
    }

    @Test
    fun wipeDataContent_clickItemShowsDialog() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                WipeDataContent(
                    wipeStatus = WipeDataViewModel.WipeStatus.Idle,
                    onWipeConfirm = {},
                    onResetWipeStatus = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.wipe_data_all)).performClick()

        // Check if dialog is displayed by checking for the OK button
        composeTestRule.onNodeWithText(context.getString(R.string.common_dialog_ok_button)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.common_dialog_cancel_button)).assertIsDisplayed()
    }

    @Test
    fun wipeDataContent_clickCancelDismissesDialog() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                WipeDataContent(
                    wipeStatus = WipeDataViewModel.WipeStatus.Idle,
                    onWipeConfirm = {},
                    onResetWipeStatus = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.wipe_data_all)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.common_dialog_cancel_button)).performClick()

        // Dialog should be dismissed. Check if "OK" button is gone.
        composeTestRule.onNodeWithText(context.getString(R.string.common_dialog_ok_button)).assertDoesNotExist()
    }

    @Test
    fun wipeDataContent_clickOkCallsConfirm() {
        var confirmedItem: WipeDataItem? = null
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                WipeDataContent(
                    wipeStatus = WipeDataViewModel.WipeStatus.Idle,
                    onWipeConfirm = { confirmedItem = it },
                    onResetWipeStatus = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.wipe_data_logs)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.common_dialog_ok_button)).performClick()

        assertEquals(WipeDataItem.WipeAllLogs, confirmedItem)
    }

    @Test
    fun wipeDataContent_successStatusCallsReset() {
        var resetCalled = false
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                WipeDataContent(
                    wipeStatus = WipeDataViewModel.WipeStatus.Success,
                    onWipeConfirm = {},
                    onResetWipeStatus = { resetCalled = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(resetCalled)
    }
}
