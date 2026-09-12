package com.soulhowling.solvelog_jetpackcompose.core.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerDao
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao.FolderDao
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao.QuestionDao
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao.SolveLogDao
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Folder
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.Question
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TimerState::class, SolveLog::class, Folder::class, Question::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SolveLogDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun solveLogDao(): SolveLogDao
    abstract fun questionDao(): QuestionDao

    abstract fun timerDao(): TimerDao

    companion object {
        private const val DATABASE_NAME = "solve_log_database"
        private val SQL_DEFAULT_TIMER = "INSERT INTO timer_state (title, workSet, workSeconds, breakSeconds, phase, createdAt, updatedAt) VALUES ('タイマー', 4, 1500, 300, 'WORK', ${System.currentTimeMillis()}, ${System.currentTimeMillis()});"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS timer_state_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        workSet INTEGER NOT NULL,
                        workSeconds INTEGER NOT NULL,
                        breakSeconds INTEGER NOT NULL,
                        phase TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO timer_state_new (id, title, workSet, workSeconds, breakSeconds, phase, createdAt, updatedAt)
                    SELECT id, 'タイマー', 4, 1500, 300, 'WORK', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}
                    FROM timer_state
                """.trimIndent())

                db.execSQL("DROP TABLE timer_state")
                db.execSQL("ALTER TABLE timer_state_new RENAME TO timer_state")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ${DBKeys.TABLE_SOLVE_LOG} ADD COLUMN ${DBKeys.KEY_PRIORITY} INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE ${DBKeys.TABLE_FOLDER} ADD COLUMN ${DBKeys.KEY_PRIORITY} INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_solve_log_folderId ON ${DBKeys.TABLE_SOLVE_LOG}(${DBKeys.KEY_FOLDER_ID})")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_folder_parentFolderId ON ${DBKeys.TABLE_FOLDER}(${DBKeys.KEY_PARENT_FOLDER_ID})")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_question_solveLogId ON ${DBKeys.TABLE_QUESTION}(${DBKeys.KEY_SOLVE_LOG_ID})")
            }
        }

        @Volatile
        private var instance: SolveLogDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SolveLogDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(context, SolveLogDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object: Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                db.execSQL(SQL_DEFAULT_TIMER)
                            }
                        }
                    })
                    .build()
                instance = newInstance
                newInstance
            }
        }

        /**
         * For testing purposes to inject an in-memory database.
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun setTestInstance(db: SolveLogDatabase?) {
            instance = db
        }
    }
}
