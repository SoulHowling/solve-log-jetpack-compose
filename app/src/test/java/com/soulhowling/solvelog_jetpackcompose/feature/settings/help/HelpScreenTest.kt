package com.soulhowling.solvelog_jetpackcompose.feature.settings.help

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.AppBarController
import com.soulhowling.solvelog_jetpackcompose.core.LocalAppBarController
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class HelpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun helpScreen_contentDisplayed() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                HelpScreenContent()
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.help_section_logging_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.help_section_measurement_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.help_section_settings_title)).assertIsDisplayed()

        composeTestRule.onNodeWithText(context.getString(R.string.help_section_logging_content)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.help_section_measurement_content)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.help_section_settings_content)).assertIsDisplayed()
    }

    @Test
    fun helpScreen_setsAppBarTitle() {
        val appBarTitle = mutableStateOf<String?>(null)
        val mockController = object : AppBarController {
            override fun setTitle(title: String?) {
                appBarTitle.value = title
            }
            override fun setActions(actions: (@Composable () -> Unit)?) {}
            override fun setBottomBarVisible(visible: Boolean) {}
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppBarController provides mockController) {
                SolveLogJetpackComposeTheme {
                    HelpScreenContent()
                }
            }
        }

        composeTestRule.waitForIdle()

        assertEquals(context.getString(R.string.about_this_app_help_button), appBarTitle.value)
    }
}
