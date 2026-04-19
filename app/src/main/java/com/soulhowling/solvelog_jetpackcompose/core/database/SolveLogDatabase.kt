package com.soulhowling.solvelog_jetpackcompose.core.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.TimerDao
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TimerState::class],
    version = 1
)
abstract class SolveLogDatabase : RoomDatabase() {
    abstract fun timerDao(): TimerDao

    companion object {
        private const val DATABASE_NAME = "solve_log_database"
        private const val SQL_DEFAULT_TIMER = "INSERT INTO timer_state (initialSeconds, remainingSeconds, isRunning) VALUES (60, 60, 0);"
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