package com.soulhowling.solvelog_jetpackcompose.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.TimerDao
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.TimerState
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
    version = 1
)
abstract class SolveLogDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun solveLogDao(): SolveLogDao
    abstract fun questionDao(): QuestionDao

    abstract fun timerDao(): TimerDao

    companion object {
        private const val DATABASE_NAME = "solve_log_database"
        private const val SQL_DEFAULT_TIMER = "INSERT INTO timer_state (initialSeconds, remainingSeconds, isRunning, lastModified) VALUES (60, 60, 0, 0);"
        @Volatile
        private var instance: SolveLogDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SolveLogDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, SolveLogDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object: Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                db.execSQL(SQL_DEFAULT_TIMER)
                            }
                        }
                    })
                    .build()
                    .also { instance = it }
            }
        }
    }
}