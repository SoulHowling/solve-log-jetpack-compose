package com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.MemoRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.FolderType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.ViewType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository.FolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MemoListViewModelTest {

    private lateinit var viewModel: MemoListViewModel
    private lateinit var application: SolveLogApplication
    private lateinit var database: SolveLogDatabase
    private lateinit var folderRepository: FolderRepository
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

        folderRepository = FolderRepository(database.folderDao())
        memoRepository = MemoRepository(database.memoDao())
        viewModel = MemoListViewModel(application)
    }

    @After
    fun tearDown() {
        database.close()
        SolveLogDatabase.setTestInstance(null)
        Dispatchers.resetMain()
    }

    private fun TestScope.startCollectingFlows() {
        backgroundScope.launch { viewModel.folders.collect {} }
        backgroundScope.launch { viewModel.displayItems.collect {} }
        backgroundScope.launch { viewModel.currentFolderPath.collect {} }
    }

    @Test
    fun `initial state should have null currentFolderId and empty displayItems`() = runTest(testDispatcher) {
        startCollectingFlows()

        assertNull(viewModel.currentFolderId.value)
        assertEquals(0, viewModel.displayItems.value.size)
        assertNull(viewModel.currentFolderPath.value)
    }

    @Test
    fun `selectFolder updates currentFolderId`() = runTest(testDispatcher) {
        startCollectingFlows()

        viewModel.selectFolder(10)
        assertEquals(10, viewModel.currentFolderId.value)

        viewModel.selectFolder(null)
        assertNull(viewModel.currentFolderId.value)
    }

    @Test
    fun `insertFolder adds memo folder to current folder`() = runTest(testDispatcher) {
        startCollectingFlows()

        viewModel.insertFolder("Folder 1")

        val items = viewModel.displayItems.first { it.isNotEmpty() }
        assertEquals(1, items.size)
        assertTrue(items[0] is MemoListItem.FolderItem)

        val folderItem = items[0] as MemoListItem.FolderItem
        assertEquals("Folder 1", folderItem.folder.name)
        assertEquals(FolderType.MEMO.index, folderItem.folder.folderType)
    }

    @Test
    fun `updateFolder modifies folder name`() = runTest(testDispatcher) {
        startCollectingFlows()

        viewModel.insertFolder("Old Name")
        val items = viewModel.displayItems.first { it.isNotEmpty() }
        val folder = (items[0] as MemoListItem.FolderItem).folder

        viewModel.updateFolder(folder, "New Name")

        val updatedItems = viewModel.displayItems.first { list ->
            list.any { (it as? MemoListItem.FolderItem)?.folder?.name == "New Name" }
        }
        assertEquals("New Name", (updatedItems[0] as MemoListItem.FolderItem).folder.name)
    }

    @Test
    fun `deleteFolder removes folder and all descendant items`() = runTest(testDispatcher) {
        startCollectingFlows()

        // Create Parent Folder
        viewModel.insertFolder("Parent Folder")
        val parentFolder = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        // Create Child Folder inside Parent Folder
        viewModel.selectFolder(parentFolder.id)
        viewModel.displayItems.first { it.isEmpty() }
        viewModel.insertFolder("Child Folder")
        val childFolder = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        // Create Memo inside Child Folder
        memoRepository.insertMemo(
            Memo(id = 0, name = "Child Memo", content = "Content", createdAt = 0, updatedAt = 0, folderId = childFolder.id),
        )

        // Go back to root and delete Parent Folder
        viewModel.selectFolder(null)
        viewModel.deleteFolder(parentFolder)

        // Check database: all folders and memos deleted
        val remainingFolders = folderRepository.allFolders.first { it.isEmpty() }
        val remainingMemos = memoRepository.getAllMemos().first { it.isEmpty() }

        assertEquals(0, remainingFolders.size)
        assertEquals(0, remainingMemos.size)
    }

    @Test
    fun `navigateBack moves currentFolderId up to parent folder`() = runTest(testDispatcher) {
        startCollectingFlows()

        // Create Parent Folder
        viewModel.insertFolder("Parent")
        val parentFolder = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        // Create Child Folder
        viewModel.selectFolder(parentFolder.id)
        viewModel.displayItems.first { it.isEmpty() }
        viewModel.insertFolder("Child")
        val childFolder = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        // Select Child
        viewModel.selectFolder(childFolder.id)
        assertEquals(childFolder.id, viewModel.currentFolderId.value)

        // Navigate back -> Parent
        viewModel.navigateBack()
        val parentId = viewModel.currentFolderId.first { it == parentFolder.id }
        assertEquals(parentFolder.id, parentId)

        // Navigate back -> Root (null)
        viewModel.navigateBack()
        val rootId = viewModel.currentFolderId.first { it == null }
        assertNull(rootId)
    }

    @Test
    fun `currentFolderPath constructs breadcrumb path`() = runTest(testDispatcher) {
        startCollectingFlows()

        viewModel.insertFolder("Folder A")
        val folderA = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        viewModel.selectFolder(folderA.id)
        viewModel.displayItems.first { it.isEmpty() }
        viewModel.insertFolder("Folder B")
        val folderB = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        viewModel.selectFolder(folderB.id)

        val path = viewModel.currentFolderPath.first { it == "Folder A / Folder B" }
        assertEquals("Folder A / Folder B", path)
    }

    @Test
    fun `moveFolder moves folder to target parent`() = runTest(testDispatcher) {
        startCollectingFlows()

        viewModel.insertFolder("Folder A")
        viewModel.insertFolder("Folder B")

        val items = viewModel.displayItems.first { it.size == 2 }
        val folderA = (items.find { (it as MemoListItem.FolderItem).folder.name == "Folder A" } as MemoListItem.FolderItem).folder
        val folderB = (items.find { (it as MemoListItem.FolderItem).folder.name == "Folder B" } as MemoListItem.FolderItem).folder

        // Move Folder B into Folder A
        viewModel.moveFolder(folderB, folderA.id)

        // Root should now contain only Folder A
        val rootItems = viewModel.displayItems.first { list ->
            list.size == 1 && (list[0] as? MemoListItem.FolderItem)?.folder?.name == "Folder A"
        }
        assertEquals(1, rootItems.size)

        // Select Folder A and verify Folder B is inside
        viewModel.selectFolder(folderA.id)
        val itemsInA = viewModel.displayItems.first { list ->
            list.any { (it as? MemoListItem.FolderItem)?.folder?.name == "Folder B" }
        }
        assertEquals("Folder B", (itemsInA[0] as MemoListItem.FolderItem).folder.name)
    }

    @Test
    fun `moveFolder prevents cyclic hierarchy move`() = runTest(testDispatcher) {
        startCollectingFlows()

        viewModel.insertFolder("Parent")
        val parentFolder = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        viewModel.selectFolder(parentFolder.id)
        viewModel.displayItems.first { it.isEmpty() }
        viewModel.insertFolder("Child")
        val childFolder = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        // Try moving Parent into Child (cycle!)
        viewModel.moveFolder(parentFolder, childFolder.id)

        // Check parent folder still has parentFolderId == null
        val updatedParent = folderRepository.allFolders.first().first { it.id == parentFolder.id }
        assertNull(updatedParent.parentFolderId)
    }

    @Test
    fun `moveMemo updates memo folderId`() = runTest(testDispatcher) {
        startCollectingFlows()

        viewModel.insertFolder("Target Folder")
        val targetFolder = (viewModel.displayItems.first { it.isNotEmpty() }[0] as MemoListItem.FolderItem).folder

        val memo = Memo(id = 0, name = "Memo 1", content = "Content 1", createdAt = 0, updatedAt = 0, folderId = null)
        memoRepository.insertMemo(memo)

        val rootItems = viewModel.displayItems.first { it.size == 2 }
        val memoInDb = (rootItems.find { it is MemoListItem.MemoItem } as MemoListItem.MemoItem).memo

        viewModel.moveMemo(memoInDb, targetFolder.id)

        // Wait until root items reduced to 1
        viewModel.displayItems.first { it.size == 1 }

        viewModel.selectFolder(targetFolder.id)
        val itemsInFolder = viewModel.displayItems.first { list ->
            list.any { (it as? MemoListItem.MemoItem)?.memo?.name == "Memo 1" }
        }
        assertEquals(1, itemsInFolder.size)
        assertEquals("Memo 1", (itemsInFolder[0] as MemoListItem.MemoItem).memo.name)
    }

    @Test
    fun `deleteMemo removes memo from displayItems and database`() = runTest(testDispatcher) {
        startCollectingFlows()

        val memo = Memo(id = 0, name = "To Delete", content = "Content", createdAt = 0, updatedAt = 0, folderId = null)
        memoRepository.insertMemo(memo)

        val memoInDb = memoRepository.getAllMemos().first().first()
        viewModel.deleteMemo(memoInDb)

        val items = viewModel.displayItems.first { it.isEmpty() }
        assertEquals(0, items.size)
    }

    @Test
    fun `reorder updates item priorities in database`() = runTest(testDispatcher) {
        startCollectingFlows()

        memoRepository.insertMemo(Memo(id = 0, name = "M1", content = "C1", createdAt = 100, updatedAt = 100, priority = 0))
        memoRepository.insertMemo(Memo(id = 0, name = "M2", content = "C2", createdAt = 200, updatedAt = 200, priority = 1))

        val items = viewModel.displayItems.first { it.size == 2 }
        assertEquals("M1", (items[0] as MemoListItem.MemoItem).memo.name)
        assertEquals("M2", (items[1] as MemoListItem.MemoItem).memo.name)

        // Swap M1 and M2
        viewModel.reorder(0, 1)

        val reorderedMemos = memoRepository.getAllMemos().first { memos ->
            memos.find { it.name == "M1" }?.priority == 1
        }
        val m1 = reorderedMemos.first { it.name == "M1" }
        val m2 = reorderedMemos.first { it.name == "M2" }

        assertEquals(1, m1.priority)
        assertEquals(0, m2.priority)
    }

    @Test
    fun `exportMemo emits Single target`() = runTest(testDispatcher) {
        val memo = Memo(id = 42, name = "Export Memo", content = "C", createdAt = 0, updatedAt = 0)
        viewModel.exportMemo(memo)

        val target = viewModel.exportTarget.first()
        assertTrue(target is MemoExportTarget.Single)
        val single = target as MemoExportTarget.Single
        assertEquals(42, single.id)
        assertEquals("Export Memo", single.name)
        assertEquals("Export Memo.json", single.fileName)
    }

    @Test
    fun `exportFolder emits FolderTarget target`() = runTest(testDispatcher) {
        val folder = folderRepository.createFolder("Export Folder", null)

        viewModel.exportFolder(folder)

        val target = viewModel.exportTarget.first()
        assertTrue(target is MemoExportTarget.FolderTarget)
        val folderTarget = target as MemoExportTarget.FolderTarget
        assertEquals("Export Folder", folderTarget.name)
        assertEquals("Export Folder.json", folderTarget.fileName)
    }

    @Test
    fun `exportAllMemos emits All target`() = runTest(testDispatcher) {
        viewModel.exportAllMemos()

        val target = viewModel.exportTarget.first()
        assertTrue(target is MemoExportTarget.All)
        assertEquals("all_memos.json", target.fileName)
    }

    @Test
    fun `toggleViewMode toggles viewType`() = runTest(testDispatcher) {
        val initialViewType = viewModel.viewType.value
        viewModel.toggleViewMode()
        val toggledViewType = viewModel.viewType.value

        assertEquals(ViewType.toggle(initialViewType), toggledViewType)
    }
}
