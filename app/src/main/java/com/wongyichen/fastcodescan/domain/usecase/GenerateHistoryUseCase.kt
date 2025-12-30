package com.wongyichen.fastcodescan.domain.usecase

import com.wongyichen.fastcodescan.domain.model.CodeRecord
import com.wongyichen.fastcodescan.domain.model.RecordType
import com.wongyichen.fastcodescan.domain.repository.CodeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGenerateHistoryUseCase @Inject constructor(
    private val repository: CodeRepository
) {
    operator fun invoke(): Flow<List<CodeRecord>> {
        return repository.getRecordsByType(RecordType.GENERATE)
    }
}

class SaveGenerateRecordUseCase @Inject constructor(
    private val repository: CodeRepository
) {
    suspend operator fun invoke(record: CodeRecord): Long {
        return repository.insertRecord(record.copy(recordType = RecordType.GENERATE))
    }
}

class DeleteGenerateRecordUseCase @Inject constructor(
    private val repository: CodeRepository
) {
    suspend operator fun invoke(record: CodeRecord) {
        repository.deleteRecord(record)
    }
}

class ClearGenerateHistoryUseCase @Inject constructor(
    private val repository: CodeRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllByType(RecordType.GENERATE)
    }
}
