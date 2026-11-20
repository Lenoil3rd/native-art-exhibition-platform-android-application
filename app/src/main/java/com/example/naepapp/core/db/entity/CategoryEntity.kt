package com.example.naepapp.core.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val slug: String,
    val name: String
)
