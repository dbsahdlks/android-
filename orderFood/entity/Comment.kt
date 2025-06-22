package com.example.orderfood.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import android.graphics.Bitmap

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Business::class,
            parentColumns = ["business_id"],  // 关联Business表的business_id字段
            childColumns = ["business_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("user_id"),
        Index("business_id")
    ]
)
data class Comment(
    @PrimaryKey(autoGenerate = true)
    val commentId: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "business_id")
    val businessId: String,  // 修改为String类型，匹配Business.business_id

    val content: String,
    val rating: Int,
    val commentTime: Long = System.currentTimeMillis(),
    val imagePath: String? = null
) {
    @Ignore
    var userName: String? = null

    @Ignore
    var formattedTime: String? = null

    @Ignore
    var imageBitmap: Bitmap? = null
}