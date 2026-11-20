package com.example.naepapp.core.di

import android.content.Context
import androidx.room3.Room
import com.example.naepapp.core.db.NaepDatabase
import com.example.naepapp.core.db.dao.*
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
    fun provideDatabase(@ApplicationContext context: Context): NaepDatabase {
        return Room.databaseBuilder(
            context,
            NaepDatabase::class.java,
            "naep.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCategoryDao(database: NaepDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideCreationDao(database: NaepDatabase): CreationDao = database.creationDao()

    @Provides
    fun provideFeedDao(database: NaepDatabase): FeedDao = database.feedDao()

    @Provides
    fun provideArtisanDao(database: NaepDatabase): ArtisanDao = database.artisanDao()

    @Provides
    fun provideUserDao(database: NaepDatabase): UserDao = database.userDao()

    @Provides
    fun providePendingOperationDao(database: NaepDatabase): PendingOperationDao = database.pendingOperationDao()
}
