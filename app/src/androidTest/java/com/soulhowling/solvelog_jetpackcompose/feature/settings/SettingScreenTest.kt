package com.soulhowling.solvelog_jetpackcompose.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun settingScreen_itemsDisplayed() {
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SettingScreenContent(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeChange = {},
                    onWipeData = {},
                    onAboutThisApp = {}
                )
            }
        }

        // 各項目が表示されているか確認
        composeTestRule.onNodeWithText(context.getString(R.string.setting_app_theme)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.setting_wipe_data)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.setting_about_this_app)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.setting_privacy_policy)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.setting_software_license)).assertIsDisplayed()
    }

    @Test
    fun settingScreen_wipeDataClicked() {
        var clicked = false
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SettingScreenContent(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeChange = {},
                    onWipeData = { clicked = true },
                    onAboutThisApp = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.setting_wipe_data)).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun settingScreen_aboutThisAppClicked() {
        var clicked = false
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SettingScreenContent(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeChange = {},
                    onWipeData = {},
                    onAboutThisApp = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.setting_about_this_app)).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun settingScreen_themeModeChanged() {
        var selectedMode: ThemeMode? = null
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SettingScreenContent(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeChange = { selectedMode = it },
                    onWipeData = {},
                    onAboutThisApp = {}
                )
            }
        }

        // 「ライト」モードを選択
        composeTestRule.onNodeWithText(context.getString(R.string.theme_light)).performClick()
        assertEquals(ThemeMode.LIGHT, selectedMode)

        // 「ダーク」モードを選択
        composeTestRule.onNodeWithText(context.getString(R.string.theme_dark)).performClick()
        assertEquals(ThemeMode.DARK, selectedMode)

        // 「システム設定」を選択
        composeTestRule.onNodeWithText(context.getString(R.string.theme_system)).performClick()
        assertEquals(ThemeMode.SYSTEM, selectedMode)
    }
}
