package com.soulhowling.solvelog_jetpackcompose.utils.compose

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class LifecycleTouchGuardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lifecycleTouchGuard_targetScreen_rendersContent() {
        composeTestRule.setContent {
            LifecycleTouchGuard(isTargetScreen = true) {
                Text("Target Screen Content")
            }
        }

        composeTestRule.onNodeWithText("Target Screen Content").assertIsDisplayed()
    }

    @Test
    fun lifecycleTouchGuard_nonTargetScreen_rendersContent() {
        composeTestRule.setContent {
            LifecycleTouchGuard(isTargetScreen = false) {
                Text("Non-Target Screen Content")
            }
        }

        composeTestRule.onNodeWithText("Non-Target Screen Content").assertIsDisplayed()
    }
}
