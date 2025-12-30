package com.wongyichen.fastcodescan.data.repository

import com.wongyichen.fastcodescan.data.local.dao.CodeRecordDao
import com.wongyichen.fastcodescan.data.local.entity.CodeRecordEntity
import com.wongyichen.fastcodescan.domain.model.CodeRecord
import com.wongyichen.fastcodescan.domain.model.RecordType
import com.wongyichen.fastcodescan.domain.repository.CodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeRepositoryImpl @Inject constructor(
    private val codeRecordDao: CodeRecordDao
) : CodeRepository {

    override fun getRecordsByType(recordType: RecordType): Flow<List<CodeRecord>> {
        return codeRecordDao.getRecordsByType(recordType.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertRecord(record: CodeRecord): Long {
        return codeRecordDao.insertRecord(CodeRecordEntity.fromDomain(record))
    }

    override suspend fun deleteRecord(record: CodeRecord) {
        codeRecordDao.deleteRecord(CodeRecordEntity.fromDomain(record))
    }

    override suspend fun deleteAllByType(recordType: RecordType) {
        codeRecordDao.deleteAllByType(recordType.name)
    }
}
