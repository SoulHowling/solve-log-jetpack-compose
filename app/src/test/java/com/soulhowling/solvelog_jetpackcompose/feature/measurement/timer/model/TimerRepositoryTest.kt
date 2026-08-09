package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TimerRepositoryTest {

    private lateinit var repository: TimerRepository
    private lateinit var fakeDao: FakeTimerDao

    @Before
    fun setUp() {
        fakeDao = FakeTimerDao()
        repository = TimerRepository(fakeDao)
    }

    @Test
    fun `createTimer should return correct state`() {
        val timer = repository.createTimer("Test", 4, 1500, 300)
        
        assertEquals("Test", timer.title)
        assertEquals(4, timer.workSet)
        assertEquals(1500, timer.workSeconds)
        assertEquals(300, timer.breakSeconds)
        assertEquals(TimerPhase.WORK, timer.phase)
    }

    @Test
    fun `insertTimer should call dao`() = runBlocking {
        val timer = TimerState(title = "Test", workSet = 4, workSeconds = 60, breakSeconds = 30, initialSeconds = 60, remainingSeconds = 60)
        repository.insertTimer(timer)
        
        val all = repository.allTimers.first()
        assertEquals(1, all.size)
        assertEquals("Test", all[0].title)
    }

    @Test
    fun `deleteTimer should call dao`() = runBlocking {
        val timer = TimerState(id = 1, title = "Test", workSet = 4, workSeconds = 60, breakSeconds = 30, initialSeconds = 60, remainingSeconds = 60)
        fakeDao.insert(timer)
        
        repository.deleteTimer(1)
        
        val all = repository.allTimers.first()
        assertEquals(0, all.size)
    }
}

class FakeTimerDao : TimerDao {
    private val timers = mutableListOf<TimerState>()
    private val flow = MutableStateFlow<List<TimerState>>(emptyList())
    private var nextId = 1

    override fun getTimerState(id: Int): Flow<TimerState?> {
        return MutableStateFlow(timers.find { it.id == id })
    }

    override fun getAllTimerStates(): Flow<List<TimerState>> {
        return flow
    }

    override suspend fun deleteTimerState(id: Int) {
        timers.removeIf { it.id == id }
        flow.value = timers.toList()
    }

    override suspend fun deleteAll() {
        timers.clear()
        flow.value = emptyList()
    }

    override suspend fun insert(entity: TimerState) {
        val timerWithId = if (entity.id == 0) entity.copy(id = nextId++) else entity
        timers.add(timerWithId)
        flow.value = timers.toList()
    }

    override suspend fun insertAll(entities: List<TimerState>) {
        entities.forEach { insert(it) }
    }

    override suspend fun upsert(entity: TimerState) {
        val index = timers.indexOfFirst { it.id == entity.id && it.id != 0 }
        if (index != -1) {
            timers[index] = entity
        } else {
            insert(entity)
        }
        flow.value = timers.toList()
    }

    override suspend fun delete(entity: TimerState) {
        timers.removeIf { it.id == entity.id }
        flow.value = timers.toList()
    }

    override suspend fun deleteAll(entities: List<TimerState>) {
        timers.removeAll { t -> entities.any { it.id == t.id } }
        flow.value = timers.toList()
    }
}
