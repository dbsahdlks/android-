package com.example.orderfood.model

import com.example.orderfood.entity.Business

data class FoodItem(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String? = null,
)