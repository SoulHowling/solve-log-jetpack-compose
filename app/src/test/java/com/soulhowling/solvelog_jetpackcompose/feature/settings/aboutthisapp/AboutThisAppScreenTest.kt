package com.soulhowling.solvelog_jetpackcompose.feature.settings.aboutthisapp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.soulhowling.solvelog_jetpackcompose.BuildConfig
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
class AboutThisAppScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun aboutThisAppScreen_contentDisplayed() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                AboutThisAppContent()
            }
        }

        // アプリ名が表示されているか確認
        composeTestRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()

        // バージョン情報が表示されているか確認
        val versionText = context.getString(R.string.about_this_app_version, BuildConfig.VERSION_NAME)
        composeTestRule.onNodeWithText(versionText).assertIsDisplayed()

        // ヘルプボタンが表示されているか確認
        composeTestRule.onNodeWithText(context.getString(R.string.about_this_app_help_button)).assertIsDisplayed()
    }

    @Test
    fun aboutThisAppScreen_setsAppBarTitle() {
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
                    AboutThisAppContent()
                }
            }
        }

        composeTestRule.waitForIdle()

        assertEquals(context.getString(R.string.setting_about_this_app), appBarTitle.value)
    }
}
