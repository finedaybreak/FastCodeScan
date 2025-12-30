package com.wongyichen.fastcodescan.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wongyichen.fastcodescan.data.local.entity.CodeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeRecordDao {
    @Query("SELECT * FROM code_records WHERE recordType = :recordType ORDER BY createdAt DESC")
    fun getRecordsByType(recordType: String): Flow<List<CodeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CodeRecordEntity): Long

    @Delete
    suspend fun deleteRecord(record: CodeRecordEntity)

    @Query("DELETE FROM code_records WHERE recordType = :recordType")
    suspend fun deleteAllByType(recordType: String)
}
