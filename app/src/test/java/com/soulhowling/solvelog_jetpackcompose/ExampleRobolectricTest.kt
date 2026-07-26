package com.soulhowling.solvelog_jetpackcompose

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

import org.junit.Assert.*

/**
 * Robolectric test, which will execute on the local JVM.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class ExampleRobolectricTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.soulhowling.solvelog_jetpackcompose", appContext.packageName)
    }
}
