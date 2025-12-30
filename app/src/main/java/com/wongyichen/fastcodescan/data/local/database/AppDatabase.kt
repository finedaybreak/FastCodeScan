package com.wongyichen.fastcodescan.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wongyichen.fastcodescan.data.local.dao.CodeRecordDao
import com.wongyichen.fastcodescan.data.local.entity.CodeRecordEntity

@Database(
    entities = [CodeRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun codeRecordDao(): CodeRecordDao
}
