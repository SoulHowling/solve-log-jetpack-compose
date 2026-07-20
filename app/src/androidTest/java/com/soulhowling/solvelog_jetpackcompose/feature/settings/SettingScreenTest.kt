package com.soulhowling.solvelog_jetpackcompose.feature.settings

import androidx.compose.runtime.mutableStateOf
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
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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

    @OptIn(ExperimentalAtomicApi::class)
    @Test
    fun settingScreen_wipeDataClicked() {
        var clicked = AtomicBoolean(false)
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SettingScreenContent(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeChange = {},
                    onWipeData = { clicked = AtomicBoolean(true) },
                    onAboutThisApp = {}
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.setting_wipe_data)).performClick()
        assertEquals(true, clicked.load())
    }

    @OptIn(ExperimentalAtomicApi::class)
    @Test
    fun settingScreen_aboutThisAppClicked() {
        var clicked = AtomicBoolean(false)
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SettingScreenContent(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeChange = {},
                    onWipeData = {},
                    onAboutThisApp = { clicked = AtomicBoolean(true) }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.setting_about_this_app)).performClick()
        assertEquals(true, clicked.load())
    }

    @Test
    fun settingScreen_themeModeChanged() {
        val selectedMode = mutableStateOf(ThemeMode.SYSTEM)
        composeTestRule.setContent {
            SolveLogJetpackComposeTheme {
                SettingScreenContent(
                    themeMode = selectedMode.value,
                    onThemeModeChange = { selectedMode.value = it },
                    onWipeData = {},
                    onAboutThisApp = {}
                )
            }
        }

        // 「ライト」モードを選択
        composeTestRule.onNodeWithText(context.getString(R.string.theme_light)).performClick()
        assertEquals(ThemeMode.LIGHT, selectedMode.value)

        // 「ダーク」モードを選択
        composeTestRule.onNodeWithText(context.getString(R.string.theme_dark)).performClick()
        assertEquals(ThemeMode.DARK, selectedMode.value)

        // 「システム設定」を選択
        composeTestRule.onNodeWithText(context.getString(R.string.theme_system)).performClick()
        assertEquals(ThemeMode.SYSTEM, selectedMode.value)
    }
}
