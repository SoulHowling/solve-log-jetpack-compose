package com.soulhowling.solvelog_jetpackcompose.feature.settings.softwarelicense

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class SoftwareLicenseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun softwareLicenseScreen_setsAppBarTitle() {
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
                    SoftwareLicenseScreenContent()
                }
            }
        }

        composeTestRule.waitForIdle()

        assertEquals(context.getString(R.string.setting_software_license), appBarTitle.value)
    }

    @Test
    fun softwareLicenseScreen_contentDoesNotCrash() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SoftwareLicenseScreenContent()
            }
        }

        // 描画が完了するまで待機
        composeTestRule.waitForIdle()
    }
}
