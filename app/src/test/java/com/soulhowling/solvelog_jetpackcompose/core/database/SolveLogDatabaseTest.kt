package com.soulhowling.solvelog_jetpackcompose.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SolveLogDatabaseTest {

    private lateinit var application: SolveLogApplication
    private lateinit var database: SolveLogDatabase

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            application,
            SolveLogDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
        SolveLogDatabase.setTestInstance(null)
    }

    @Test
    fun daos_areAccessibleAndNotNull() {
        assertNotNull(database.folderDao())
        assertNotNull(database.solveLogDao())
        assertNotNull(database.questionDao())
        assertNotNull(database.memoDao())
        assertNotNull(database.timerDao())
    }

    @Test
    fun setTestInstance_overridesGetDatabase() {
        SolveLogDatabase.setTestInstance(database)
        val scope = TestScope(UnconfinedTestDispatcher())
        val retrievedDb = SolveLogDatabase.getDatabase(application, scope)

        assertSame(database, retrievedDb)
    }

    @Test
    fun getDatabase_returnsNonNullDatabaseInstance() {
        SolveLogDatabase.setTestInstance(null)
        val scope = TestScope(UnconfinedTestDispatcher())
        val db1 = SolveLogDatabase.getDatabase(application, scope)
        val db2 = SolveLogDatabase.getDatabase(application, scope)

        assertNotNull(db1)
        assertSame(db1, db2)
        db1.close()
    }
}
