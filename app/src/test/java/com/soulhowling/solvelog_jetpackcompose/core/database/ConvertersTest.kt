package com.soulhowling.solvelog_jetpackcompose.core.database

import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    @Test
    fun fromTimerPhase_convertsAllEnumValuesToString() {
        assertEquals("WORK", converters.fromTimerPhase(TimerPhase.WORK))
        assertEquals("BREAK", converters.fromTimerPhase(TimerPhase.BREAK))
        assertEquals("COMPLETE", converters.fromTimerPhase(TimerPhase.COMPLETE))
    }

    @Test
    fun toTimerPhase_convertsValidStringsToEnumValues() {
        assertEquals(TimerPhase.WORK, converters.toTimerPhase("WORK"))
        assertEquals(TimerPhase.BREAK, converters.toTimerPhase("BREAK"))
        assertEquals(TimerPhase.COMPLETE, converters.toTimerPhase("COMPLETE"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun toTimerPhase_invalidString_throwsException() {
        converters.toTimerPhase("INVALID_PHASE")
    }
}
