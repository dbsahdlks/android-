package com.example.orderfood.entity

data class MonthlyFoodSale(
    val foodId: Long,
    val foodName: String,
    val totalSold: Int,
    val foodImageUrl: String
)