package com.example.orderfood.dao

import androidx.room.*
import com.example.orderfood.entity.User

@Dao
interface UserDao {
    // 基础 CRUD 操作
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("DELETE FROM user_table WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: String)

    // 用户查询
    @Query("SELECT * FROM user_table WHERE user_id = :userId")
    suspend fun getUserById(userId: String): User?
    @Query("SELECT * FROM user_table WHERE phone = :phone")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers(): List<User>

    // 存在性检查
    @Query("SELECT COUNT(*) FROM user_table WHERE user_id = :userId")
    suspend fun userIdExists(userId: String): Int

    @Query("SELECT COUNT(*) FROM user_table WHERE phone = :phone")
    suspend fun phoneExists(phone: String): Int

    // 认证支持
    @Query("SELECT password FROM user_table WHERE user_id = :userId LIMIT 1")
    suspend fun getPasswordHash(userId: String): String?

    // 用户统计
    @Query("SELECT COUNT(*) FROM user_table")
    suspend fun getTotalUserCount(): Int

}