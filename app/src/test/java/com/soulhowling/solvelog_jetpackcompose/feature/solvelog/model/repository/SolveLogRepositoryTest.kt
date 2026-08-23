package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SolveLogRepositoryTest {

    private lateinit var database: SolveLogDatabase
    private lateinit var repository: SolveLogRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SolveLogDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SolveLogRepository(database.solveLogDao())

        // Insert dummy folders to satisfy FK constraints
        runTest {
            database.folderDao().insert(
                com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder(
                    id = 1, name = "Folder 1", createdAt = 0, updatedAt = 0, parentFolderId = null
                )
            )
            database.folderDao().insert(
                com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder(
                    id = 2, name = "Folder 2", createdAt = 0, updatedAt = 0, parentFolderId = null
                )
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `createSolveLog should return log with default attempt count 1`() {
        val log = repository.createSolveLog(
            name = "Test Log",
            questionQuantity = 10,
            selectionType = SelectionType.NUMBER,
            selectionQuantity = SelectionQuantity.FOUR,
            folderId = 5
        )
        
        assertEquals(0, log.id)
        assertEquals("Test Log", log.name)
        assertEquals(10, log.questionQuantity)
        assertEquals(1, log.attemptCount)
        assertEquals(5, log.folderId)
        assertNotNull(log.createdAt)
    }

    @Test
    fun `insertAndReturnId should save log and return new id`() = runTest {
        val log = repository.createSolveLog("New Log", 20, SelectionType.ALPHABET, SelectionQuantity.FIVE, null)
        val id = repository.insertAndReturnId(log)
        
        val savedLog = repository.getSolveLog(id).first()
        assertNotNull(savedLog)
        assertEquals("New Log", savedLog?.name)
        assertEquals(20, savedLog?.questionQuantity)
    }

    @Test
    fun `getSolveLogListBy should return logs in specific folder`() = runTest {
        val log1 = repository.createSolveLog("Log 1", 10, SelectionType.NUMBER, SelectionQuantity.FOUR, 1)
        val log2 = repository.createSolveLog("Log 2", 10, SelectionType.NUMBER, SelectionQuantity.FOUR, 2)
        repository.insertSolveLog(log1)
        repository.insertSolveLog(log2)
        
        val folder1Logs = repository.getSolveLogListBy(1)
        assertEquals(1, folder1Logs.size)
        assertEquals("Log 1", folder1Logs[0].name)
    }

    @Test
    fun `upsertSolveLog should update existing log`() = runTest {
        val log = repository.createSolveLog("Old", 10, SelectionType.NUMBER, SelectionQuantity.FOUR, null)
        val id = repository.insertAndReturnId(log)
        
        val updated = log.copy(id = id, name = "New")
        repository.upsertSolveLog(updated)
        
        val result = repository.getSolveLog(id).first()
        assertEquals("New", result?.name)
    }
}
