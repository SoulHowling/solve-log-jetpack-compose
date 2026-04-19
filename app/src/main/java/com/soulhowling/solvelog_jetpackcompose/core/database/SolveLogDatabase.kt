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
        @Volatile
        private var instance: SolveLogDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SolveLogDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, SolveLogDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object: Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            instance.let { database ->
                                scope.launch(Dispatchers.IO) {
                                    val timerDao = database?.timerDao()
                                    val defaultTimer = TimerState(
                                        id = 0,
                                        initialSeconds = 60,
                                        remainingSeconds = 60,
                                        isRunning = false
                                    )
                                    timerDao?.upsertTimerState(defaultTimer)
                                }
                            }
                        }
                    })
                    .build()
                    .also { instance = it }
            }
        }
    }
}