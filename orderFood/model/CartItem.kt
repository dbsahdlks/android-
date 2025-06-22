package com.example.orderfood.model

import com.example.orderfood.entity.Food

data class CartItem(
    val food: Food,
    var quantity: Int
)