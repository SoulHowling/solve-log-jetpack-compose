package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveTimerTest {

    @Test
    fun `nextPhase from WORK should go to BREAK if sets remain`() {
        val timerState = TimerState(
            title = "Test",
            workSet = 4,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.WORK
        )
        val activeTimer = ActiveTimer(
            state = timerState,
            initialSeconds = 60,
            remainingSeconds = 0
        )
        
        val next = activeTimer.nextPhase()
        
        assertEquals(TimerPhase.BREAK, next.phase)
        assertEquals(3, next.workSet)
        assertEquals(30, next.remainingSeconds)
        assertTrue(next.isRunning)
    }

    @Test
    fun `nextPhase from WORK should go to COMPLETE if no sets remain`() {
        val timerState = TimerState(
            title = "Test",
            workSet = 1,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.WORK
        )
        val activeTimer = ActiveTimer(
            state = timerState,
            initialSeconds = 60,
            remainingSeconds = 0
        )
        
        val next = activeTimer.nextPhase()
        
        assertEquals(TimerPhase.COMPLETE, next.phase)
        assertEquals(0, next.workSet)
        assertEquals(0, next.remainingSeconds)
        assertFalse(next.isRunning)
    }

    @Test
    fun `nextPhase from BREAK should go to WORK`() {
        val timerState = TimerState(
            title = "Test",
            workSet = 3,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.BREAK
        )
        val activeTimer = ActiveTimer(
            state = timerState,
            initialSeconds = 30,
            remainingSeconds = 0
        )
        
        val next = activeTimer.nextPhase()
        
        assertEquals(TimerPhase.WORK, next.phase)
        assertEquals(3, next.workSet)
        assertEquals(60, next.remainingSeconds)
        assertTrue(next.isRunning)
    }

    @Test
    fun `nextPhase from COMPLETE should stay COMPLETE`() {
        val timerState = TimerState(
            title = "Test",
            workSet = 0,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.COMPLETE
        )
        val activeTimer = ActiveTimer(
            state = timerState,
            initialSeconds = 0,
            remainingSeconds = 0
        )
        
        val next = activeTimer.nextPhase()
        
        assertEquals(TimerPhase.COMPLETE, next.phase)
    }

    @Test
    fun `nextPhase from WORK should skip BREAK if breakSeconds is 0`() {
        val timerState = TimerState(
            title = "Test",
            workSet = 4,
            workSeconds = 60,
            breakSeconds = 0,
            phase = TimerPhase.WORK
        )
        val activeTimer = ActiveTimer(
            state = timerState,
            initialSeconds = 60,
            remainingSeconds = 0
        )
        
        val next = activeTimer.nextPhase()
        
        assertEquals(TimerPhase.WORK, next.phase)
        assertEquals(3, next.workSet)
        assertEquals(60, next.remainingSeconds)
        assertTrue(next.isRunning)
    }
}
