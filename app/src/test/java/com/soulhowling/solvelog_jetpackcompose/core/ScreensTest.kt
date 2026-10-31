package com.soulhowling.solvelog_jetpackcompose.core

import com.soulhowling.solvelog_jetpackcompose.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreensTest {

    @Test
    fun solveLogRoute_propertiesCorrect() {
        assertEquals("solve_log", Screens.SolveLogRoute.route)
        assertEquals(R.string.tab_editor, Screens.SolveLogRoute.title)
        assertNotNull(Screens.SolveLogRoute.icon)
    }

    @Test
    fun solveLogViewer_createRoute_formatsCorrectly() {
        assertEquals("solve_log_viewer/{solveLogId}", Screens.SolveLogViewer.route)
        assertEquals("solve_log_viewer/42", Screens.SolveLogViewer.createRoute(42))
        assertEquals(R.string.log_view, Screens.SolveLogViewer.title)
    }

    @Test
    fun solveLogEditor_createRoute_formatsCorrectly() {
        assertEquals("solve_log_editor/{solveLogId}/{attempt}", Screens.SolveLogEditor.route)
        assertEquals("solve_log_editor/10/2", Screens.SolveLogEditor.createRoute(10, 2))
        assertEquals(R.string.log_edit, Screens.SolveLogEditor.title)
    }

    @Test
    fun measurement_propertiesCorrect() {
        assertEquals("measurement", Screens.Measurement.route)
        assertEquals(R.string.tab_measurement, Screens.Measurement.title)
    }

    @Test
    fun timerPage_createRoute_formatsCorrectly() {
        assertEquals("timer/{timerStateId}", Screens.TimerPage.route)
        assertEquals("timer/5", Screens.TimerPage.createRoute(5))
        assertEquals(R.string.measurement_timer_title, Screens.TimerPage.title)
    }

    @Test
    fun memoList_propertiesCorrect() {
        assertEquals("memo_list", Screens.MemoList.route)
        assertEquals(R.string.tab_memo, Screens.MemoList.title)
    }

    @Test
    fun memoEditor_createRoute_withoutFolderId_formatsCorrectly() {
        assertEquals("memo_editor/{memoId}?folderId={folderId}", Screens.MemoEditor.route)
        assertEquals("memo_editor/1", Screens.MemoEditor.createRoute(1))
        assertEquals(R.string.memo_editor_title, Screens.MemoEditor.title)
    }

    @Test
    fun memoEditor_createRoute_withFolderId_formatsCorrectly() {
        assertEquals("memo_editor/1?folderId=3", Screens.MemoEditor.createRoute(1, 3))
    }

    @Test
    fun memoView_createRoute_formatsCorrectly() {
        assertEquals("memo_view/{memoId}", Screens.MemoView.route)
        assertEquals("memo_view/7", Screens.MemoView.createRoute(7))
        assertEquals(R.string.memo_title, Screens.MemoView.title)
    }

    @Test
    fun settingScreens_routesAndTitlesCorrect() {
        assertEquals("setting", Screens.Setting.route)
        assertEquals(R.string.tab_setting, Screens.Setting.title)

        assertEquals("setting/wipe_data", Screens.WipeData.route)
        assertEquals(R.string.wipe_data_title, Screens.WipeData.title)

        assertEquals("setting/about_this_app", Screens.AboutThisApp.route)
        assertEquals(R.string.setting_about_this_app, Screens.AboutThisApp.title)

        assertEquals("setting/help", Screens.Help.route)
        assertEquals(R.string.about_this_app_help_button, Screens.Help.title)

        assertEquals("setting/privacy_policy", Screens.PrivacyPolicy.route)
        assertEquals(R.string.setting_privacy_policy, Screens.PrivacyPolicy.title)

        assertEquals("setting/software_license", Screens.SoftwareLicense.route)
        assertEquals(R.string.setting_software_license, Screens.SoftwareLicense.title)
    }

    @Test
    fun getAllScreens_containsAll14Screens() {
        val screens = Screens.getAllScreens()
        assertEquals(14, screens.size)
        assertTrue(screens.contains(Screens.SolveLogRoute))
        assertTrue(screens.contains(Screens.SolveLogViewer))
        assertTrue(screens.contains(Screens.SolveLogEditor))
        assertTrue(screens.contains(Screens.Measurement))
        assertTrue(screens.contains(Screens.TimerPage))
        assertTrue(screens.contains(Screens.MemoList))
        assertTrue(screens.contains(Screens.MemoEditor))
        assertTrue(screens.contains(Screens.MemoView))
        assertTrue(screens.contains(Screens.Setting))
        assertTrue(screens.contains(Screens.WipeData))
        assertTrue(screens.contains(Screens.AboutThisApp))
        assertTrue(screens.contains(Screens.Help))
        assertTrue(screens.contains(Screens.PrivacyPolicy))
        assertTrue(screens.contains(Screens.SoftwareLicense))
    }

    @Test
    fun getBottomNaviScreens_contains4BottomNavScreens() {
        val bottomNavScreens = Screens.getBottomNaviScreens()
        assertEquals(4, bottomNavScreens.size)
        assertEquals(Screens.SolveLogRoute, bottomNavScreens[0])
        assertEquals(Screens.Measurement, bottomNavScreens[1])
        assertEquals(Screens.MemoList, bottomNavScreens[2])
        assertEquals(Screens.Setting, bottomNavScreens[3])
    }

    @Test
    fun argumentConstants_areCorrect() {
        assertEquals("solveLogId", Screens.ARG_SOLVE_LOG_ID)
        assertEquals("attempt", Screens.ARG_ATTEMPT)
        assertEquals("timerStateId", Screens.ARG_TIMER_STATE_ID)
        assertEquals("memoId", Screens.ARG_MEMO_ID)
        assertEquals("folderId", Screens.ARG_FOLDER_ID)
    }
}
