package com.wongyichen.fastcodescan.di

import android.content.Context
import androidx.room.Room
import com.wongyichen.fastcodescan.data.local.dao.CodeRecordDao
import com.wongyichen.fastcodescan.data.local.database.AppDatabase
import com.wongyichen.fastcodescan.data.repository.CodeRepositoryImpl
import com.wongyichen.fastcodescan.domain.repository.CodeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fastcodescan.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCodeRecordDao(database: AppDatabase): CodeRecordDao {
        return database.codeRecordDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCodeRepository(impl: CodeRepositoryImpl): CodeRepository
}
