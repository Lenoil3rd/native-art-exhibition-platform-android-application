package com.example.naepapp.core.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "feed_remote_keys")
data class FeedRemoteKeyEntity(
    @PrimaryKey val queryKey: String,
    val nextPage: Int?,
    val fetchedAt: Long
)
