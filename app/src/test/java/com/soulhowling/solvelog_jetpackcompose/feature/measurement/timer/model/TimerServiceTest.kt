package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = SolveLogApplication::class)
class TimerServiceTest {

    private lateinit var application: SolveLogApplication
    private val testDispatcher = UnconfinedTestDispatcher()
    private val timerId = 1
    private lateinit var fakeDao: FakeTimerDao
    private lateinit var repository: TimerRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        
        fakeDao = FakeTimerDao()
        repository = TimerRepository(fakeDao)
        
        // Reset timer manager state
        application.timerManager.updateTimer(null)
        application.timerManager.updateInitialWorkSet(0)
        
        runBlocking {
            fakeDao.insert(
                TimerState(
                    id = timerId,
                    title = "Test Timer",
                    workSet = 4,
                    workSeconds = 60,
                    breakSeconds = 30
                )
            )
        }
    }

    @After
    fun tearDown() {
        application.timerManager.updateTimer(null)
        application.timerManager.updateInitialWorkSet(0)
        Dispatchers.resetMain()
    }

    @Test
    fun `ACTION_START should initialize timer state`() = runTest {
        val serviceController = Robolectric.buildService(TimerService::class.java)
        val service = serviceController.get()
        service.repositoryForTesting = repository
        serviceController.create()
        
        val intent = Intent(application, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_TIMER_ID, timerId)
        }
        
        service.onStartCommand(intent, 0, 1)
        
        val timer = application.timerManager.timer.value
        assertNotNull("Timer should not be null", timer)
        assertEquals("Test Timer", timer?.title)
        assertTrue(timer!!.isRunning)
        
        serviceController.destroy()
    }

    @Test
    fun `ACTION_PAUSE should set isRunning to false`() = runTest {
        val serviceController = Robolectric.buildService(TimerService::class.java)
        val service = serviceController.get()
        service.repositoryForTesting = repository
        serviceController.create()
        
        val startIntent = Intent(application, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_TIMER_ID, timerId)
        }
        service.onStartCommand(startIntent, 0, 1)
        
        val pauseIntent = Intent(application, TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        service.onStartCommand(pauseIntent, 0, 2)
        
        val timer = application.timerManager.timer.value
        assertNotNull(timer)
        assertFalse(timer!!.isRunning)
        
        serviceController.destroy()
    }

    @Test
    fun `ACTION_RESUME should set isRunning back to true`() = runTest {
        val serviceController = Robolectric.buildService(TimerService::class.java)
        val service = serviceController.get()
        service.repositoryForTesting = repository
        serviceController.create()
        
        val startIntent = Intent(application, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_TIMER_ID, timerId)
        }
        service.onStartCommand(startIntent, 0, 1)
        
        service.onStartCommand(Intent(TimerService.ACTION_PAUSE), 0, 2)
        
        val resumeIntent = Intent(application, TimerService::class.java).apply {
            action = TimerService.ACTION_RESUME
        }
        service.onStartCommand(resumeIntent, 0, 3)
        
        assertTrue(application.timerManager.timer.value!!.isRunning)
        
        serviceController.destroy()
    }

    @Test
    fun `ACTION_RESET should clear state and destroy service`() = runTest {
        val serviceController = Robolectric.buildService(TimerService::class.java)
        val service = serviceController.get()
        service.repositoryForTesting = repository
        serviceController.create()
        
        val startIntent = Intent(application, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_TIMER_ID, timerId)
        }
        service.onStartCommand(startIntent, 0, 1)
        
        val resetIntent = Intent(application, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET
        }
        service.onStartCommand(resetIntent, 0, 2)
        
        // In Robolectric, we manually destroy to simulate the effect of stopSelf() 
        // triggering lifecycle completion if it doesn't happen automatically.
        serviceController.destroy()
        
        val timer = application.timerManager.timer.value
        assertNull(timer)
    }
}
