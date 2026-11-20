package com.example.naepapp.core.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.naepapp.core.db.dao.*
import com.example.naepapp.core.db.entity.*

@Database(
    entities = [
        CategoryEntity::class,
        CreationEntity::class,
        CreationMediaEntity::class,
        ArtisanEntity::class,
        FeedEntryEntity::class,
        FeedRemoteKeyEntity::class,
        CurrentUserEntity::class,
        PendingOperationEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NaepDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun creationDao(): CreationDao
    abstract fun feedDao(): FeedDao
    abstract fun artisanDao(): ArtisanDao
    abstract fun userDao(): UserDao
    abstract fun pendingOperationDao(): PendingOperationDao
}
