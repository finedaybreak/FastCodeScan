package com.wongyichen.fastcodescan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wongyichen.fastcodescan.domain.model.CodeFormat
import com.wongyichen.fastcodescan.domain.model.CodeRecord
import com.wongyichen.fastcodescan.domain.model.CodeType
import com.wongyichen.fastcodescan.domain.model.RecordType
import java.time.LocalDateTime

@Entity(tableName = "code_records")
data class CodeRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val type: String,
    val format: String,
    val recordType: String,
    val createdAt: Long
) {
    fun toDomain(): CodeRecord {
        return CodeRecord(
            id = id,
            content = content,
            type = CodeType.valueOf(type),
            format = CodeFormat.valueOf(format),
            recordType = RecordType.valueOf(recordType),
            createdAt = LocalDateTime.ofEpochSecond(createdAt, 0, java.time.ZoneOffset.UTC)
        )
    }

    companion object {
        fun fromDomain(record: CodeRecord): CodeRecordEntity {
            return CodeRecordEntity(
                id = record.id,
                content = record.content,
                type = record.type.name,
                format = record.format.name,
                recordType = record.recordType.name,
                createdAt = record.createdAt.toEpochSecond(java.time.ZoneOffset.UTC)
            )
        }
    }
}
