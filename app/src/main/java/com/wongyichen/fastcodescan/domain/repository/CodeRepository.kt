package com.wongyichen.fastcodescan.domain.repository

import com.wongyichen.fastcodescan.domain.model.CodeRecord
import com.wongyichen.fastcodescan.domain.model.RecordType
import kotlinx.coroutines.flow.Flow

interface CodeRepository {
    fun getRecordsByType(recordType: RecordType): Flow<List<CodeRecord>>
    suspend fun insertRecord(record: CodeRecord): Long
    suspend fun deleteRecord(record: CodeRecord)
    suspend fun deleteAllByType(recordType: RecordType)
}
