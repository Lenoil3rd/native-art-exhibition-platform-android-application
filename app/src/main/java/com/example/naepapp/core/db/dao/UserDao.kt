package com.example.naepapp.core.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.example.naepapp.core.db.entity.CurrentUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM current_user LIMIT 1")
    fun observeCurrentUser(): Flow<CurrentUserEntity?>

    @Query("SELECT * FROM current_user LIMIT 1")
    suspend fun getCurrentUser(): CurrentUserEntity?

    @Upsert
    suspend fun upsert(user: CurrentUserEntity)

    @Query("DELETE FROM current_user")
    suspend fun clear()
}
