package com.example.naepapp.core.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.example.naepapp.core.db.entity.FeedEntryEntity
import com.example.naepapp.core.db.entity.FeedRemoteKeyEntity

@Dao
interface FeedDao {

    @Query("SELECT * FROM feed_remote_keys WHERE queryKey = :queryKey")
    suspend fun getRemoteKey(queryKey: String): FeedRemoteKeyEntity?

    @Upsert
    suspend fun upsertRemoteKey(key: FeedRemoteKeyEntity)

    @Query("DELETE FROM feed_remote_keys WHERE queryKey = :queryKey")
    suspend fun deleteRemoteKey(queryKey: String)

    @Query("DELETE FROM feed_entries WHERE queryKey = :queryKey")
    suspend fun deleteFeedEntries(queryKey: String)

    @Upsert
    suspend fun insertFeedEntries(entries: List<FeedEntryEntity>)

    @Query("SELECT COUNT(*) FROM feed_entries WHERE queryKey = :queryKey")
    suspend fun getFeedEntriesCount(queryKey: String): Int

    @Query("DELETE FROM feed_entries WHERE queryKey = :queryKey AND position >= :maxEntries")
    suspend fun trimFeedEntries(queryKey: String, maxEntries: Int = 200)

    @Transaction
    suspend fun saveFeedPage(
        queryKey: String,
        isRefresh: Boolean,
        feedEntries: List<FeedEntryEntity>,
        remoteKey: FeedRemoteKeyEntity
    ) {
        if (isRefresh) {
            deleteFeedEntries(queryKey)
            deleteRemoteKey(queryKey)
        }
        insertFeedEntries(feedEntries)
        upsertRemoteKey(remoteKey)
        trimFeedEntries(queryKey, 200)
    }
}
