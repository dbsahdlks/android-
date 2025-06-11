package com.example.orderfood.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.orderfood.entity.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM user_table WHERE user_id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT COUNT(*) FROM user_table WHERE user_id = :userId")
    suspend fun userIdExists(userId: String): Int

}