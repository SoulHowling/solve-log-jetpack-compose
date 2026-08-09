package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerStateTest {

    @Test
    fun `tick should decrease remaining seconds`() {
        val state = TimerState(
            title = "Test",
            workSet = 4,
            workSeconds = 60,
            breakSeconds = 30,
            initialSeconds = 60,
            remainingSeconds = 60
        )
        
        val ticked = state.tick()
        
        assertEquals(59, ticked.remainingSeconds)
    }

    @Test
    fun `tick should not decrease remaining seconds when already 0`() {
        val state = TimerState(
            title = "Test",
            workSet = 4,
            workSeconds = 60,
            breakSeconds = 30,
            initialSeconds = 60,
            remainingSeconds = 0
        )
        
        val ticked = state.tick()
        
        assertEquals(0, ticked.remainingSeconds)
    }

    @Test
    fun `nextPhase from WORK should go to BREAK if sets remain`() {
        val state = TimerState(
            title = "Test",
            workSet = 4,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.WORK,
            initialSeconds = 60,
            remainingSeconds = 0
        )
        
        val next = state.nextPhase()
        
        assertEquals(TimerPhase.BREAK, next.phase)
        assertEquals(3, next.workSet)
        assertEquals(30, next.remainingSeconds)
        assertTrue(next.isRunning)
    }

    @Test
    fun `nextPhase from WORK should go to COMPLETE if no sets remain`() {
        val state = TimerState(
            title = "Test",
            workSet = 1,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.WORK,
            initialSeconds = 60,
            remainingSeconds = 0
        )
        
        val next = state.nextPhase()
        
        assertEquals(TimerPhase.COMPLETE, next.phase)
        assertEquals(0, next.workSet)
        assertEquals(0, next.remainingSeconds)
        assertFalse(next.isRunning)
    }

    @Test
    fun `nextPhase from BREAK should go to WORK`() {
        val state = TimerState(
            title = "Test",
            workSet = 3,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.BREAK,
            initialSeconds = 30,
            remainingSeconds = 0
        )
        
        val next = state.nextPhase()
        
        assertEquals(TimerPhase.WORK, next.phase)
        assertEquals(3, next.workSet)
        assertEquals(60, next.remainingSeconds)
        assertTrue(next.isRunning)
    }

    @Test
    fun `nextPhase from COMPLETE should stay COMPLETE`() {
        val state = TimerState(
            title = "Test",
            workSet = 0,
            workSeconds = 60,
            breakSeconds = 30,
            phase = TimerPhase.COMPLETE,
            initialSeconds = 0,
            remainingSeconds = 0
        )
        
        val next = state.nextPhase()
        
        assertEquals(TimerPhase.COMPLETE, next.phase)
    }
}
