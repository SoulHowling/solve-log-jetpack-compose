package com.soulhowling.solvelog_jetpackcompose.core

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.isActive
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SolveLogApplicationTest {

    private lateinit var application: SolveLogApplication

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun application_providesNonNullDatabase() {
        val database = application.database
        assertNotNull(database)
    }

    @Test
    fun application_providesNonNullTimerManager() {
        val timerManager = application.timerManager
        assertNotNull(timerManager)
    }

    @Test
    fun application_scopeIsActive() {
        assertTrue(application.applicationScope.isActive)
    }
}
