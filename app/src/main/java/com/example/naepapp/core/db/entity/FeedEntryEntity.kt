package com.example.naepapp.core.db.entity

import androidx.room3.Entity
import androidx.room3.Index

@Entity(
    tableName = "feed_entries",
    primaryKeys = ["queryKey", "creationId"],
    indices = [Index("queryKey"), Index("creationId")]
)
data class FeedEntryEntity(
    val queryKey: String,
    val position: Int,
    val creationId: String
)
