package com.example.orderfood.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_details",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Food::class,
            parentColumns = ["foodId"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("foodId"), Index("orderId")]
)
data class OrderDetail(
    @PrimaryKey(autoGenerate = true)
    val detailId: Long = 0,

    var orderId: Long,
    val foodId: Long?,

    val foodName: String = "",
    val foodDescription: String = "",
    val foodPrice: Double = 0.0,
    val quantity: Int = 0,
    val foodImageUrl: String = ""
)