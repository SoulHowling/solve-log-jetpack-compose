package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.viewmodel

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.FolderRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.QuestionRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.SolveLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SolveLogListViewModelTest {

    private lateinit var viewModel: SolveLogListViewModel
    private lateinit var application: SolveLogApplication
    private lateinit var database: SolveLogDatabase
    private lateinit var folderRepository: FolderRepository
    private lateinit var solveLogRepository: SolveLogRepository
    private lateinit var questionRepository: QuestionRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        
        database = Room.inMemoryDatabaseBuilder(
            application,
            SolveLogDatabase::class.java
        ).allowMainThreadQueries().build()
        SolveLogDatabase.setTestInstance(database)

        folderRepository = FolderRepository(database.folderDao())
        solveLogRepository = SolveLogRepository(database.solveLogDao())
        questionRepository = QuestionRepository(database.questionDao())

        viewModel = SolveLogListViewModel(application)
    }

    @After
    fun tearDown() {
        SolveLogDatabase.setTestInstance(null)
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty folders and items`() = runTest(testDispatcher) {
        assertEquals(0, viewModel.folders.value.size)
        assertEquals(0, viewModel.displayItems.value.size)
    }

    @Test
    fun `insertFolder should add a folder and update displayItems`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.displayItems.collect {} }

        viewModel.insertFolder("New Folder")
        
        val items = viewModel.displayItems.first { it.isNotEmpty() }
        assertEquals(1, items.size)
        assertTrue(items[0] is SolveLogListItem.FolderItem)
        assertEquals("New Folder", (items[0] as SolveLogListItem.FolderItem).folder.name)
    }

    @Test
    fun `insertSolveLog should add a log and create initial questions`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.displayItems.collect {} }

        viewModel.insertSolveLog("New Log", 10, SelectionType.NUMBER, SelectionQuantity.FOUR)
        
        val items = viewModel.displayItems.first { it.isNotEmpty() }
        assertEquals(1, items.size)
        assertTrue(items[0] is SolveLogListItem.LogItem)
        val log = (items[0] as SolveLogListItem.LogItem).log
        assertEquals("New Log", log.name)
        assertEquals(10, log.questionQuantity)

        val questions = questionRepository.getQuestionBy(log.id).first()
        assertEquals(10, questions.size)
        assertTrue(questions.all { it.attempt == 1 })
    }

    @Test
    fun `selectFolder should filter displayItems`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.displayItems.collect {} }

        // Create folder A
        viewModel.insertFolder("Folder A")
        val folderA = (viewModel.displayItems.first { it.isNotEmpty() }[0] as SolveLogListItem.FolderItem).folder
        
        // Create log inside folder A
        viewModel.selectFolder(folderA.id)
        viewModel.insertSolveLog("Log in A", 5, SelectionType.ALPHABET, SelectionQuantity.FOUR)
        
        // Items in folder A should have 1 log
        val itemsInA = viewModel.displayItems.first { it.any { item -> item is SolveLogListItem.LogItem } }
        assertEquals(1, itemsInA.size)
        assertEquals("Log in A", (itemsInA[0] as SolveLogListItem.LogItem).log.name)

        // Go back to root
        viewModel.selectFolder(null)
        val rootItems = viewModel.displayItems.first { it.any { item -> item is SolveLogListItem.FolderItem } }
        assertEquals(1, rootItems.size)
        assertTrue(rootItems[0] is SolveLogListItem.FolderItem)
    }

    @Test
    fun `deleteSolveLog should remove log from displayItems`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.displayItems.collect {} }

        viewModel.insertSolveLog("To Delete", 5, SelectionType.NUMBER, SelectionQuantity.FOUR)
        val log = (viewModel.displayItems.first { it.isNotEmpty() }[0] as SolveLogListItem.LogItem).log
        
        viewModel.deleteSolveLog(log)
        
        val items = viewModel.displayItems.first { it.isEmpty() }
        assertTrue(items.isEmpty())
    }

    @Test
    fun `updateSolveLog should modify log and adjust questions if quantity changed`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.displayItems.collect {} }

        viewModel.insertSolveLog("Original", 5, SelectionType.NUMBER, SelectionQuantity.FOUR)
        val log = (viewModel.displayItems.first { it.isNotEmpty() }[0] as SolveLogListItem.LogItem).log
        
        // Increase quantity from 5 to 8
        viewModel.updateSolveLog(log, "Updated", 8, SelectionType.ALPHABET, SelectionQuantity.FIVE)
        
        val updatedItems = viewModel.displayItems.first { 
            (it.getOrNull(0) as? SolveLogListItem.LogItem)?.log?.name == "Updated" 
        }
        val updatedLog = (updatedItems[0] as SolveLogListItem.LogItem).log
        assertEquals(8, updatedLog.questionQuantity)
        
        val questions = questionRepository.getQuestionBy(log.id).first()
        assertEquals(8, questions.size)
    }

    @Test
    fun `moveSolveLog should update folderId`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.displayItems.collect {} }

        viewModel.insertFolder("Target Folder")
        viewModel.insertSolveLog("Log to Move", 5, SelectionType.NUMBER, SelectionQuantity.FOUR)
        
        val items = viewModel.displayItems.first { it.size == 2 }
        val folder = (items.find { it is SolveLogListItem.FolderItem } as SolveLogListItem.FolderItem).folder
        val log = (items.find { it is SolveLogListItem.LogItem } as SolveLogListItem.LogItem).log
        
        viewModel.moveSolveLog(log, folder.id)
        
        // Wait for database update and flow emission
        viewModel.displayItems.first { it.size == 1 }
        
        // Go to target folder
        viewModel.selectFolder(folder.id)
        
        // Wait for log to appear in target folder
        val folderItems = viewModel.displayItems.first { it.any { item -> item is SolveLogListItem.LogItem } }
        assertEquals(1, folderItems.size)
        assertTrue(folderItems[0] is SolveLogListItem.LogItem)
        assertEquals(log.id, (folderItems[0] as SolveLogListItem.LogItem).log.id)
    }

    @Test
    fun `exportAll should emit All target to exportTarget flow`() = runTest(testDispatcher) {
        val targets = mutableListOf<ExportTarget>()
        backgroundScope.launch { viewModel.exportTarget.collect { targets.add(it) } }
        
        viewModel.exportAll()
        advanceUntilIdle()
        
        assertEquals(1, targets.size)
        assertTrue(targets[0] is ExportTarget.All)
    }
}
