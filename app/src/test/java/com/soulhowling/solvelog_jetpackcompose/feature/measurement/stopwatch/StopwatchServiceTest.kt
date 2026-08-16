package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.utils.DefaultTimeProvider
import com.soulhowling.solvelog_jetpackcompose.utils.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
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

    private class FakeTimeProvider(var time: Long = 1000L) : TimeProvider {
        override fun currentTimeMillis(): Long = time
    }

    private lateinit var fakeTimeProvider: FakeTimeProvider

    @Before
    fun setup() {
        fakeTimeProvider = FakeTimeProvider()
        StopwatchService.timeProvider = fakeTimeProvider

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

    @After
    fun tearDown() {
        StopwatchService.timeProvider = DefaultTimeProvider
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
        assertEquals(1000L, StopwatchService.baseTime.value)
        assertEquals(0L, StopwatchService.accumulatedTime.value)

        serviceController.destroy()
    }

    @Test
    fun `ACTION_PAUSE should pause the stopwatch and update accumulated time`() = runTest {
        val serviceController = Robolectric.buildService(StopwatchService::class.java)
        val service = serviceController.create().get()

        // Start (at t=1000)
        service.onStartCommand(Intent(StopwatchService.ACTION_START), 0, 1)
        
        // Advance time by 100ms
        fakeTimeProvider.time += 100

        // Pause (at t=1100)
        val pauseIntent = Intent(ApplicationProvider.getApplicationContext(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_PAUSE
        }
        service.onStartCommand(pauseIntent, 0, 2)

        assertFalse(StopwatchService.isRunning.value)
        assertEquals(100L, StopwatchService.accumulatedTime.value)

        serviceController.destroy()
    }

    @Test
    fun `ACTION_RESUME should resume the stopwatch`() = runTest {
        val serviceController = Robolectric.buildService(StopwatchService::class.java)
        val service = serviceController.create().get()

        // Start (t=1000) -> Advance 50ms -> Pause (t=1050)
        service.onStartCommand(Intent(StopwatchService.ACTION_START), 0, 1)
        fakeTimeProvider.time += 50
        service.onStartCommand(Intent(StopwatchService.ACTION_PAUSE), 0, 2)
        
        val pausedAccumulatedTime = StopwatchService.accumulatedTime.value
        assertEquals(50L, pausedAccumulatedTime)

        // Advance time while paused
        fakeTimeProvider.time += 1000

        // Resume (t=2050)
        val resumeIntent = Intent(ApplicationProvider.getApplicationContext(), StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_RESUME
        }
        service.onStartCommand(resumeIntent, 0, 3)

        assertTrue(StopwatchService.isRunning.value)
        assertEquals(50L, StopwatchService.accumulatedTime.value)
        // baseTime = current(2050) - accumulated(50) = 2000
        assertEquals(2000L, StopwatchService.baseTime.value)
        
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
