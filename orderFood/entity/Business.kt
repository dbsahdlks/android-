package com.example.orderfood.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "businesses",
    indices = [
        Index(value = ["business_id"], unique = true) // 添加唯一索引
    ]
)
data class Business(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "image_path") val imagePath: String,
)