package com.soulhowling.solvelog_jetpackcompose.feature.memo.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MemoRepositoryTest {

    private lateinit var database: SolveLogDatabase
    private lateinit var repository: MemoRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SolveLogDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MemoRepository(database.memoDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `createMemo should return memo with id 0 and correct properties`() {
        val beforeTime = System.currentTimeMillis()
        val memo = repository.createMemo(
            name = "Test Title",
            content = "Test Content",
            folderId = 1,
        )
        val afterTime = System.currentTimeMillis()

        assertEquals(0, memo.id)
        assertEquals("Test Title", memo.name)
        assertEquals("Test Content", memo.content)
        assertEquals(1, memo.folderId)
        assertEquals(memo.createdAt, memo.updatedAt)
        assertTrue((memo.createdAt in beforeTime..afterTime))
    }

    @Test
    fun `insertMemo should insert memo into database`() = runTest {
        val memo = repository.createMemo("Memo 1", "Content 1")
        repository.insertMemo(memo)

        val allMemos = repository.getAllMemos().first()
        assertEquals(1, allMemos.size)
        assertEquals("Memo 1", allMemos[0].name)
        assertEquals("Content 1", allMemos[0].content)
    }

    @Test
    fun `getMemo should return specific memo by id or null if not found`() = runTest {
        val memo = repository.createMemo("Find Me", "Some Content")
        val generatedId = repository.upsertMemo(memo).toInt()

        val foundMemo = repository.getMemo(generatedId).first()
        assertNotNull(foundMemo)
        assertEquals("Find Me", foundMemo?.name)

        val notFoundMemo = repository.getMemo(9999).first()
        assertNull(notFoundMemo)
    }

    @Test
    fun `getAllMemos should return memos sorted by priority ASC then createdAt ASC`() = runTest {
        val memo1 = Memo(id = 0, name = "Priority 2", content = "C1", createdAt = 100, updatedAt = 100, priority = 2)
        val memo2 = Memo(id = 0, name = "Priority 1 - Early", content = "C2", createdAt = 100, updatedAt = 100, priority = 1)
        val memo3 = Memo(id = 0, name = "Priority 1 - Late", content = "C3", createdAt = 200, updatedAt = 200, priority = 1)

        repository.insertMemo(memo1)
        repository.insertMemo(memo2)
        repository.insertMemo(memo3)

        val memos = repository.getAllMemos().first()
        assertEquals(3, memos.size)
        assertEquals("Priority 1 - Early", memos[0].name)
        assertEquals("Priority 1 - Late", memos[1].name)
        assertEquals("Priority 2", memos[2].name)
    }

    @Test
    fun `upsertMemo should insert new memo and update existing memo`() = runTest {
        val memo = repository.createMemo("Original", "Original Content")
        val id = repository.upsertMemo(memo).toInt()

        val savedMemo = repository.getMemo(id).first()
        assertNotNull(savedMemo)

        val updatedMemo = savedMemo!!.copy(name = "Updated", content = "Updated Content")
        repository.upsertMemo(updatedMemo)

        val result = repository.getMemo(id).first()
        assertEquals("Updated", result?.name)
        assertEquals("Updated Content", result?.content)

        val allMemos = repository.getAllMemos().first()
        assertEquals(1, allMemos.size)
    }

    @Test
    fun `deleteMemo should remove memo from database`() = runTest {
        val memo = repository.createMemo("To Delete", "Delete Content")
        val id = repository.upsertMemo(memo).toInt()

        val insertedMemo = repository.getMemo(id).first()
        assertNotNull(insertedMemo)

        repository.deleteMemo(insertedMemo!!)

        val allMemos = repository.getAllMemos().first()
        assertEquals(0, allMemos.size)
        assertNull(repository.getMemo(id).first())
    }

    @Test
    fun `deleteAll should clear all memos from database`() = runTest {
        repository.insertMemo(repository.createMemo("M1", "C1"))
        repository.insertMemo(repository.createMemo("M2", "C2"))

        assertEquals(2, repository.getAllMemos().first().size)

        repository.deleteAll()

        assertEquals(0, repository.getAllMemos().first().size)
    }

    @Test
    fun `insertMemo with folderId should save memo referencing a valid folder`() = runTest {
        val folder = Folder(id = 10, name = "Memo Folder", createdAt = 0, updatedAt = 0, parentFolderId = null)
        database.folderDao().insert(folder)

        val memo = repository.createMemo("Folder Memo", "Content", folderId = 10)
        repository.insertMemo(memo)

        val allMemos = repository.getAllMemos().first()
        assertEquals(1, allMemos.size)
        assertEquals(10, allMemos[0].folderId)
    }
}
