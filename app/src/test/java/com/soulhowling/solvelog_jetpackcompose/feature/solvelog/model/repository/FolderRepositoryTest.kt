package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class FolderRepositoryTest {

    private lateinit var database: SolveLogDatabase
    private lateinit var repository: FolderRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SolveLogDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FolderRepository(database.folderDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `createFolder should return folder with current timestamp`() {
        val folder = repository.createFolder("New Folder", null)
        
        assertEquals(0, folder.id)
        assertEquals("New Folder", folder.name)
        assertEquals(folder.createdAt, folder.updatedAt)
        assertEquals(null, folder.parentFolderId)
    }

    @Test
    fun `insertAndReturnId should save folder and return new id`() = runTest {
        val folder = repository.createFolder("Test Folder", null)
        val id = repository.insertAndReturnId(folder)
        
        val allFolders = repository.allFolders.first()
        assertEquals(1, allFolders.size)
        assertEquals(id, allFolders[0].id.toLong())
        assertEquals("Test Folder", allFolders[0].name)
    }

    @Test
    fun `upsertFolder should update existing folder`() = runTest {
        val folder = repository.createFolder("Original", null)
        val id = repository.insertAndReturnId(folder).toInt()
        
        val updatedFolder = folder.copy(id = id, name = "Updated")
        repository.upsertFolder(updatedFolder)
        
        val allFolders = repository.allFolders.first()
        assertEquals(1, allFolders.size)
        assertEquals("Updated", allFolders[0].name)
    }

    @Test
    fun `deleteFolder should remove folder`() = runTest {
        val folder = repository.createFolder("To Delete", null)
        repository.insertFolder(folder)
        
        var allFolders = repository.allFolders.first()
        assertEquals(1, allFolders.size)
        
        repository.deleteFolder(allFolders[0])
        
        allFolders = repository.allFolders.first()
        assertEquals(0, allFolders.size)
    }
}
