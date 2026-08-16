package com.soulhowling.solvelog_jetpackcompose.utils

import android.Manifest
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class PermissionUtilsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(sdk = [32]) // Below Tiramisu
    fun `handler should call onPermissionGranted immediately on SDK below Tiramisu`() {
        var grantedCalled = false
        
        composeTestRule.setContent {
            val handler = rememberNotificationPermissionHandler {
                grantedCalled = true
            }
            // Trigger the handler
            handler()
        }
        
        assertTrue(grantedCalled)
    }

    @Test
    @Config(sdk = [36]) // Tiramisu or above
    fun `handler should call onPermissionGranted if permission is already granted on Tiramisu+`() {
        var grantedCalled = false
        
        // Grant permission in ShadowApplication
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        
        composeTestRule.setContent {
            val handler = rememberNotificationPermissionHandler {
                grantedCalled = true
            }
            handler()
        }
        
        assertTrue(grantedCalled)
    }
}
