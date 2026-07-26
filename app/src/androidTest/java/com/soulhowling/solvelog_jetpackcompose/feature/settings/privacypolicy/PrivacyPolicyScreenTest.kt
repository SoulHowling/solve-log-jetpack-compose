package com.soulhowling.solvelog_jetpackcompose.feature.settings.privacypolicy

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.AppBarController
import com.soulhowling.solvelog_jetpackcompose.core.LocalAppBarController
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PrivacyPolicyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun privacyPolicyScreen_contentDisplayed() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                PrivacyPolicyScreenContent()
            }
        }

        // 本文が表示されているか確認
        composeTestRule.onNodeWithText(context.getString(R.string.privacy_policy_content)).assertIsDisplayed()
    }

    @Test
    fun privacyPolicyScreen_setsAppBarTitle() {
        val appBarTitle = mutableStateOf<String?>(null)
        val mockController = object : AppBarController {
            override fun setTitle(title: String?) {
                appBarTitle.value = title
            }
            override fun setActions(actions: (@androidx.compose.runtime.Composable () -> Unit)?) {}
            override fun setBottomBarVisible(visible: Boolean) {}
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppBarController provides mockController) {
                SolveLogJetpackComposeTheme {
                    PrivacyPolicyScreenContent()
                }
            }
        }

        composeTestRule.waitForIdle()

        assertEquals(context.getString(R.string.setting_privacy_policy), appBarTitle.value)
    }
}
