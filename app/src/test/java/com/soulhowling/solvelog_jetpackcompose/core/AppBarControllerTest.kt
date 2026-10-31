package com.soulhowling.solvelog_jetpackcompose.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class AppBarControllerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun localAppBarController_defaultImplementation_methodsDoNotThrow() {
        var defaultController: AppBarController? = null
        composeTestRule.setContent {
            defaultController = LocalAppBarController.current
        }

        assertNotNull(defaultController)
        defaultController?.setTitle("Title")
        defaultController?.setTitle(null)
        defaultController?.setActions(null)
        defaultController?.setActions {}
        defaultController?.setBottomBarVisible(true)
        defaultController?.setBottomBarVisible(false)
    }

    @Test
    fun customAppBarController_recordsInvocationsCorrectly() {
        var recordedTitle: String? = null
        var recordedActions: (@Composable () -> Unit)? = null
        var recordedVisibility: Boolean? = null

        val testController = object : AppBarController {
            override fun setTitle(title: String?) {
                recordedTitle = title
            }

            override fun setActions(actions: (@Composable () -> Unit)?) {
                recordedActions = actions
            }

            override fun setBottomBarVisible(visible: Boolean) {
                recordedVisibility = visible
            }
        }

        testController.setTitle("My Custom Title")
        assertEquals("My Custom Title", recordedTitle)

        testController.setBottomBarVisible(false)
        assertEquals(false, recordedVisibility)

        val dummyLambda: @Composable () -> Unit = {}
        testController.setActions(dummyLambda)
        assertNotNull(recordedActions)
    }
}
