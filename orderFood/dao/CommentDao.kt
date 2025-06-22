package com.example.orderfood.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.orderfood.entity.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    // 插入单条评论
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long

    // 插入多条评论
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>): List<Long>

    // 根据评论ID查询单条评论
    @Query("SELECT * FROM comments WHERE commentId = :commentId")
    suspend fun getCommentById(commentId: Long): Comment?

    // 根据商家ID查询所有评论
    @Query("SELECT * FROM comments WHERE business_id = :businessId ORDER BY commentTime DESC")
    fun getCommentsByBusinessId(businessId: String): Flow<List<Comment>>

    // 根据用户ID查询评论
    @Query("SELECT * FROM comments WHERE user_id = :userId ORDER BY commentTime DESC")
    fun getCommentsByUserId(userId: String): Flow<List<Comment>>

    // 更新评论内容
    @Update
    suspend fun updateComment(comment: Comment): Int

    // 删除单条评论
    @Delete
    suspend fun deleteComment(comment: Comment): Int

    // 根据评论ID删除评论
    @Query("DELETE FROM comments WHERE commentId = :commentId")
    suspend fun deleteCommentById(commentId: Long): Int

    // 删除商家的所有评论
    @Query("DELETE FROM comments WHERE business_id = :businessId")
    suspend fun deleteCommentsByBusinessId(businessId: String): Int

    // 删除用户的所有评论
    @Query("DELETE FROM comments WHERE user_id = :userId")
    suspend fun deleteCommentsByUserId(userId: String): Int

    // 获取商家的平均评分
    @Query("SELECT AVG(rating) FROM comments WHERE business_id = :businessId")
    suspend fun getAverageRatingByBusinessId(businessId: String): Double?

    // 获取评论数量
    @Query("SELECT COUNT(*) FROM comments WHERE business_id = :businessId")
    suspend fun getCommentCountByBusinessId(businessId: String): Int
}