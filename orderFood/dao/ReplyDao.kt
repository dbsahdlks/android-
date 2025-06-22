package com.example.orderfood.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.orderfood.entity.Reply
import kotlinx.coroutines.flow.Flow
@Dao
interface ReplyDao {
    @Insert
    suspend fun insertReply(reply: Reply): Long  // 返回插入的ID
    @Query("SELECT * FROM replies WHERE commentId = :commentId")
    fun getRepliesByCommentId(commentId: Long): Flow<List<Reply>>
}