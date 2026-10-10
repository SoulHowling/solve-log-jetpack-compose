package com.soulhowling.solvelog_jetpackcompose.feature.memo.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.Memo
import com.soulhowling.solvelog_jetpackcompose.feature.memo.model.MemoRepository
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MemoEditorViewModelTest {

    private lateinit var viewModel: MemoEditorViewModel
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
        viewModel = MemoEditorViewModel(application)
    }

    @After
    fun tearDown() {
        database.close()
        SolveLogDatabase.setTestInstance(null)
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMemo with -1 resets memoName, memoContent, and sets targetFolderId`() = runTest(testDispatcher) {
        viewModel.onNameChange("Temporary Name")
        viewModel.onContentChange(TextFieldValue("Temporary Content"))

        viewModel.loadMemo(-1, targetFolderId = 5)

        assertEquals("", viewModel.memoName.value)
        assertEquals("", viewModel.memoContent.value.text)
    }

    @Test
    fun `loadMemo with valid id populates memoName and memoContent`() = runTest(testDispatcher) {
        val folder = Folder(id = 2, name = "Folder 2", createdAt = 0, updatedAt = 0, parentFolderId = null)
        database.folderDao().insert(folder)

        val memo = Memo(
            id = 0,
            name = "Existing Memo",
            content = "Existing Content",
            createdAt = 100,
            updatedAt = 100,
            priority = 0,
            folderId = 2,
        )
        val memoId = memoRepository.upsertMemo(memo).toInt()

        viewModel.loadMemo(memoId)

        val loadedName = viewModel.memoName.first { it.isNotEmpty() }
        assertEquals("Existing Memo", loadedName)
        assertEquals("Existing Content", viewModel.memoContent.value.text)
    }

    @Test
    fun `onNameChange updates memoName`() = runTest(testDispatcher) {
        viewModel.onNameChange("New Memo Title")
        assertEquals("New Memo Title", viewModel.memoName.value)
    }

    @Test
    fun `onContentChange standard text update`() = runTest(testDispatcher) {
        val newContent = TextFieldValue("Hello World")
        viewModel.onContentChange(newContent)
        assertEquals("Hello World", viewModel.memoContent.value.text)
    }

    @Test
    fun `onContentChange continues bullet list on enter`() = runTest(testDispatcher) {
        viewModel.onContentChange(TextFieldValue(text = "- Item 1", selection = TextRange(8)))

        val newContentWithEnter = TextFieldValue(text = "- Item 1\n", selection = TextRange(9))
        viewModel.onContentChange(newContentWithEnter)

        assertEquals("- Item 1\n- ", viewModel.memoContent.value.text)
        assertEquals(TextRange(11), viewModel.memoContent.value.selection)
    }

    @Test
    fun `onContentChange removes empty bullet item on enter`() = runTest(testDispatcher) {
        viewModel.onContentChange(TextFieldValue(text = "- ", selection = TextRange(2)))

        val newContentWithEnter = TextFieldValue(text = "- \n", selection = TextRange(3))
        viewModel.onContentChange(newContentWithEnter)

        assertEquals("", viewModel.memoContent.value.text)
        assertEquals(TextRange(0), viewModel.memoContent.value.selection)
    }

    @Test
    fun `onContentChange continues numbered list on enter`() = runTest(testDispatcher) {
        viewModel.onContentChange(TextFieldValue(text = "1. Item 1", selection = TextRange(9)))

        val newContentWithEnter = TextFieldValue(text = "1. Item 1\n", selection = TextRange(10))
        viewModel.onContentChange(newContentWithEnter)

        assertEquals("1. Item 1\n2. ", viewModel.memoContent.value.text)
        assertEquals(TextRange(13), viewModel.memoContent.value.selection)
    }

    @Test
    fun `onContentChange removes empty numbered item on enter`() = runTest(testDispatcher) {
        viewModel.onContentChange(TextFieldValue(text = "1. ", selection = TextRange(3)))

        val newContentWithEnter = TextFieldValue(text = "1. \n", selection = TextRange(4))
        viewModel.onContentChange(newContentWithEnter)

        assertEquals("", viewModel.memoContent.value.text)
        assertEquals(TextRange(0), viewModel.memoContent.value.selection)
    }

    @Test
    fun `onContentChange continues checklist on enter`() = runTest(testDispatcher) {
        viewModel.onContentChange(TextFieldValue(text = "- [ ] Task 1", selection = TextRange(12)))

        val newContentWithEnter = TextFieldValue(text = "- [ ] Task 1\n", selection = TextRange(13))
        viewModel.onContentChange(newContentWithEnter)

        assertEquals("- [ ] Task 1\n- [ ] ", viewModel.memoContent.value.text)
        assertEquals(TextRange(19), viewModel.memoContent.value.selection)
    }

    @Test
    fun `onContentChange continues completed checklist as uncompleted on enter`() = runTest(testDispatcher) {
        viewModel.onContentChange(TextFieldValue(text = "- [x] Done Task", selection = TextRange(15)))

        val newContentWithEnter = TextFieldValue(text = "- [x] Done Task\n", selection = TextRange(16))
        viewModel.onContentChange(newContentWithEnter)

        assertEquals("- [x] Done Task\n- [ ] ", viewModel.memoContent.value.text)
        assertEquals(TextRange(22), viewModel.memoContent.value.selection)
    }

    @Test
    fun `onContentChange removes empty checklist item on enter`() = runTest(testDispatcher) {
        viewModel.onContentChange(TextFieldValue(text = "- [ ] ", selection = TextRange(6)))

        val newContentWithEnter = TextFieldValue(text = "- [ ] \n", selection = TextRange(7))
        viewModel.onContentChange(newContentWithEnter)

        assertEquals("", viewModel.memoContent.value.text)
        assertEquals(TextRange(0), viewModel.memoContent.value.selection)
    }

    @Test
    fun `insertTemplate inserts template text at cursor position`() = runTest(testDispatcher) {
        viewModel.onContentChange(TextFieldValue(text = "Hello ", selection = TextRange(6)))

        viewModel.insertTemplate("[Template]")

        assertEquals("Hello [Template]", viewModel.memoContent.value.text)
        assertEquals(TextRange(16), viewModel.memoContent.value.selection)
    }

    @Test
    fun `triggerAutoSave automatically saves memo after 300ms delay`() = runTest(testDispatcher) {
        viewModel.onNameChange("Auto Saved Memo")
        viewModel.onContentChange(TextFieldValue("Auto Saved Content"))

        advanceTimeBy(350.milliseconds)

        val memos = memoRepository.getAllMemos().first { it.isNotEmpty() }
        assertEquals(1, memos.size)
        assertEquals("Auto Saved Memo", memos[0].name)
        assertEquals("Auto Saved Content", memos[0].content)
    }

    @Test
    fun `forceSave saves new memo to database with correct priority and target folder`() = runTest(testDispatcher) {
        val folder = Folder(id = 10, name = "Memo Folder", createdAt = 0, updatedAt = 0, parentFolderId = null)
        database.folderDao().insert(folder)

        viewModel.loadMemo(-1, targetFolderId = 10)
        viewModel.onNameChange("Folder Memo")
        viewModel.onContentChange(TextFieldValue("Folder Memo Content"))

        viewModel.forceSave()

        val memos = memoRepository.getAllMemos().first { it.isNotEmpty() }
        assertEquals(1, memos.size)
        assertEquals("Folder Memo", memos[0].name)
        assertEquals("Folder Memo Content", memos[0].content)
        assertEquals(10, memos[0].folderId)
        assertEquals(0, memos[0].priority)
    }

    @Test
    fun `forceSave existing memo updates memo in database`() = runTest(testDispatcher) {
        val originalMemo = Memo(
            id = 0,
            name = "Original Name",
            content = "Original Content",
            createdAt = 100,
            updatedAt = 100,
            priority = 0
        )
        val memoId = memoRepository.upsertMemo(originalMemo).toInt()

        viewModel.loadMemo(memoId)
        viewModel.memoName.first { it.isNotEmpty() }

        viewModel.onNameChange("Updated Name")
        viewModel.onContentChange(TextFieldValue("Updated Content"))

        var savedId = -1
        viewModel.forceSave { id -> savedId = id }

        val updatedMemos = memoRepository.getAllMemos().first { list ->
            list.any { it.name == "Updated Name" }
        }
        val updatedMemo = updatedMemos.first { it.name == "Updated Name" }

        assertEquals(memoId, updatedMemo.id)
        assertEquals("Updated Name", updatedMemo.name)
        assertEquals("Updated Content", updatedMemo.content)
        assertEquals(memoId, savedId)
    }

    @Test
    fun `forceSave with blank name and content does not save to database`() = runTest(testDispatcher) {
        var callbackInvoked = false
        viewModel.forceSave { callbackInvoked = true }

        assertTrue(callbackInvoked)
        val memos = memoRepository.getAllMemos().first()
        assertEquals(0, memos.size)
    }

    @Test
    fun `forceSave with unchanged memo does not re-save`() = runTest(testDispatcher) {
        val originalMemo = Memo(
            id = 0,
            name = "Same Name",
            content = "Same Content",
            createdAt = 1000,
            updatedAt = 1000,
            priority = 0
        )
        val memoId = memoRepository.upsertMemo(originalMemo).toInt()

        viewModel.loadMemo(memoId)
        viewModel.memoName.first { it.isNotEmpty() }

        var savedId = -1
        viewModel.forceSave { id -> savedId = id }

        val memoInDb = memoRepository.getMemo(memoId).first()
        assertEquals(memoId, savedId)
        assertNotNull(memoInDb)
        assertEquals(1000L, memoInDb?.updatedAt)
    }
}
