package com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.repository

import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionQuantity
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.SelectionType
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.dao.SolveLogDao
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.model.entity.SolveLog
import kotlinx.coroutines.flow.Flow

class SolveLogRepository(private val solveLogDao: SolveLogDao) {

    val allSolveLogs: Flow<List<SolveLog>> = solveLogDao.getAllSolveLogs()

    fun createSolveLog(
        name: String,
        questionQuantity: Int,
        selectionType: SelectionType,
        selectionQuantity: SelectionQuantity,
        folderId: Int?
    ): SolveLog {
        val createdAt = System.currentTimeMillis()
        return SolveLog(
            id = 0,
            name = name,
            questionQuantity = questionQuantity,
            selectionType = selectionType.index,
            selectionQuantity = selectionQuantity.index,
            attemptCount = 1,
            createdAt = createdAt,
            updatedAt = createdAt,
            folderId = folderId
        )
    }

    fun getSolveLog(id: Int): Flow<SolveLog?> {
        return solveLogDao.getSolveLog(id)
    }

    suspend fun getSolveLogListBy(folderId: Int): List<SolveLog> {
        return solveLogDao.getSolveLogListBy(folderId)
    }

    suspend fun insertSolveLog(solveLog: SolveLog) {
        solveLogDao.insert(solveLog)
    }

    suspend fun insertAndReturnId(solveLog: SolveLog): Int {
        return solveLogDao.insertAndReturnId(solveLog).toInt()
    }

    suspend fun upsertSolveLog(solveLog: SolveLog) {
        solveLogDao.upsert(solveLog)
    }

    suspend fun deleteSolveLog(solveLog: SolveLog) {
        solveLogDao.delete(solveLog)
    }
}