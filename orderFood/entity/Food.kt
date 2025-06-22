package com.example.orderfood.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_table")
data class Food(
    @PrimaryKey(autoGenerate = true) val foodId: Long = 0,
    val businessId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String? = null // 食物图片URL
)