package com.example.naepapp.core.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "creation_media",
    foreignKeys = [
        ForeignKey(
            entity = CreationEntity::class,
            parentColumns = ["id"],
            childColumns = ["creationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("creationId")]
)
data class CreationMediaEntity(
    @PrimaryKey val id: String,
    val creationId: String,
    val url: String,
    val publicId: String,
    val position: Int,
    val width: Int?,
    val height: Int?,
    val localPath: String? = null
)
