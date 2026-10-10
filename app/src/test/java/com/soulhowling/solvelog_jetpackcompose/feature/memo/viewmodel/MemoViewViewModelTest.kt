package com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MemoViewViewModelTest {

    private lateinit var viewModel: MemoViewViewModel
    private lateinit var application: SolveLogApplication
    private lateinit var database: SolveLogDatabase
    private lateinit var memoRepository: MemoRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()

        database = Room.inMemoryDatabaseBuilder(
            application,
            SolveLogDatabase::class.java,
        ).allowMainThreadQueries().build()
        SolveLogDatabase.setTestInstance(database)

        memoRepository = MemoRepository(database.memoDao())
        viewModel = MemoViewViewModel(application)
    }

    @After
    fun tearDown() {
        database.close()
        SolveLogDatabase.setTestInstance(null)
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should emit null memo`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.memo.collect {} }
        assertNull(viewModel.memo.value)
    }

    @Test
    fun `loadMemo with -1 emits null memo`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.memo.collect {} }

        viewModel.loadMemo(-1)
        assertNull(viewModel.memo.value)
    }

    @Test
    fun `loadMemo with valid id emits memo from database`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.memo.collect {} }

        val memo = Memo(
            id = 0,
            name = "Test Memo",
            content = "Test Content",
            createdAt = 100,
            updatedAt = 100,
            priority = 0,
        )
        val memoId = memoRepository.upsertMemo(memo).toInt()

        viewModel.loadMemo(memoId)

        val loadedMemo = viewModel.memo.first { it != null }
        assertNotNull(loadedMemo)
        assertEquals("Test Memo", loadedMemo?.name)
        assertEquals("Test Content", loadedMemo?.content)
    }

    @Test
    fun `loadMemo with non-existent id emits null memo`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.memo.collect {} }

        viewModel.loadMemo(9999)
        assertNull(viewModel.memo.value)
    }

    @Test
    fun `database updates automatically reflect in memo StateFlow`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.memo.collect {} }

        val initialMemo = Memo(
            id = 0,
            name = "Initial Name",
            content = "Initial Content",
            createdAt = 100,
            updatedAt = 100,
            priority = 0,
        )
        val memoId = memoRepository.upsertMemo(initialMemo).toInt()

        viewModel.loadMemo(memoId)
        val firstEmittedMemo = viewModel.memo.first { it != null }
        assertEquals("Initial Name", firstEmittedMemo?.name)

        // Update memo in DB
        val updatedMemo = initialMemo.copy(id = memoId, name = "Updated Name", content = "Updated Content")
        memoRepository.upsertMemo(updatedMemo)

        val secondEmittedMemo = viewModel.memo.first { it?.name == "Updated Name" }
        assertNotNull(secondEmittedMemo)
        assertEquals("Updated Name", secondEmittedMemo?.name)
        assertEquals("Updated Content", secondEmittedMemo?.content)
    }
}
