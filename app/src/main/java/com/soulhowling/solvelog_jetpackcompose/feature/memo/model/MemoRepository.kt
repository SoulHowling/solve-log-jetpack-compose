package com.soulhowling.solvelog_jetpackcompose.feature.memo.model

import kotlinx.coroutines.flow.Flow

class MemoRepository(private val memoDao: MemoDao) {

    fun getAllMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    fun getMemo(id: Int): Flow<Memo?> = memoDao.getMemo(id)

    fun createMemo(
        name: String,
        content: String,
        folderId: Int? = null
    ): Memo {
        val createdAt = System.currentTimeMillis()
        return Memo(
            id = 0,
            name = name,
            content = content,
            createdAt = createdAt,
            updatedAt = createdAt,
            folderId = folderId
        )
    }

    suspend fun insertMemo(memo: Memo) {
        memoDao.insert(memo)
    }

    suspend fun upsertMemo(memo: Memo): Long {
        return memoDao.upsert(memo)
    }

    suspend fun deleteMemo(memo: Memo) {
        memoDao.delete(memo)
    }

    suspend fun deleteAll() {
        memoDao.deleteAll()
    }
}
