package com.example.orderfood.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "replies",
    indices = [
        Index(value = ["replyId"], unique = true) // 添加唯一索引
    ]
)
data class Reply(
    @PrimaryKey(autoGenerate = true)
    val replyId: Long? =null,          // 回复ID
    val commentId: Long,            // 关联的评论ID
    val content: String,            // 回复内容
)