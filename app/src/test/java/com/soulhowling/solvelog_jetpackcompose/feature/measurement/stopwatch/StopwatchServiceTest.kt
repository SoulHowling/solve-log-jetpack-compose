package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = SolveLogApplication::class)
class StopwatchServiceTest {

    @Before
    fun setup() {
        // Reset the static state of StopwatchService before each test
        // Since the state flows are private, we send a RESET intent to a temporary service instance
        val serviceController = Robolectric.buildService(StopwatchService::class.java)
        serviceController.create()
        val intent = Intent(ApplicationProvider.getApplicationContext(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_RESET
        }
        serviceController.get().onStartCommand(intent, 0, 0)
        serviceController.destroy()
    }

    @Test
    fun `ACTION_START should start the stopwatch`() = runTest {
        val serviceController = Robolectric.buildService(StopwatchService::class.java)
        val service = serviceController.create().get()

        val intent = Intent(ApplicationProvider.getApplicationContext(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_START
        }
        service.onStartCommand(intent, 0, 1)

        assertTrue(StopwatchService.isRunning.value)
        assertTrue(StopwatchService.baseTime.value > 0)
        assertEquals(0L, StopwatchService.accumulatedTime.value)

        serviceController.destroy()
    }

    @Test
    fun `ACTION_PAUSE should pause the stopwatch and update accumulated time`() = runTest {
        val serviceController = Robolectric.buildService(StopwatchService::class.java)
        val service = serviceController.create().get()

        // Start
        service.onStartCommand(Intent(StopwatchService.ACTION_START), 0, 1)
        
        // Wait a bit to simulate time passing (in unit tests we might need to mock time if we want exact values,
        // but here we just check if it's greater than 0)
        Thread.sleep(100) 

        // Pause
        val pauseIntent = Intent(ApplicationProvider.getApplicationContext(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_PAUSE
        }
        service.onStartCommand(pauseIntent, 0, 2)

        assertFalse(StopwatchService.isRunning.value)
        assertTrue(StopwatchService.accumulatedTime.value >= 100)

        serviceController.destroy()
    }

    @Test
    fun `ACTION_RESUME should resume the stopwatch`() = runTest {
        val serviceController = Robolectric.buildService(StopwatchService::class.java)
        val service = serviceController.create().get()

        // Start -> Pause
        service.onStartCommand(Intent(StopwatchService.ACTION_START), 0, 1)
        Thread.sleep(50)
        service.onStartCommand(Intent(StopwatchService.ACTION_PAUSE), 0, 2)
        
        val pausedAccumulatedTime = StopwatchService.accumulatedTime.value

        // Resume
        val resumeIntent = Intent(ApplicationProvider.getApplicationContext(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_RESUME
        }
        service.onStartCommand(resumeIntent, 0, 3)

        assertTrue(StopwatchService.isRunning.value)
        assertEquals(pausedAccumulatedTime, StopwatchService.accumulatedTime.value)
        
        serviceController.destroy()
    }

    @Test
    fun `ACTION_RESET should reset all states`() = runTest {
        val serviceController = Robolectric.buildService(StopwatchService::class.java)
        val service = serviceController.create().get()

        // Start
        service.onStartCommand(Intent(StopwatchService.ACTION_START), 0, 1)
        
        // Reset
        val resetIntent = Intent(ApplicationProvider.getApplicationContext(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_RESET
        }
        service.onStartCommand(resetIntent, 0, 2)

        assertFalse(StopwatchService.isRunning.value)
        assertEquals(0L, StopwatchService.accumulatedTime.value)
        assertEquals(0L, StopwatchService.baseTime.value)

        serviceController.destroy()
    }
}
