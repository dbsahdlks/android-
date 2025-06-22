package com.example.orderfood.entity
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    var orderId: Long = 0,          // 订单编号（主键）
    val businessId: String,           // 商家ID
    val userId: String,                // 购买人ID
    val state: String,                // 订单状态（1:未处理，2:取消，3:完成）
    val orderTime: String,            // 订单时间（格式："2025-05-14 15:00:13"）
    val orderAddress: String,         // 订单地址
    val customerName:String,
    val customerPhone: String,
    val completionTime: String = "" // 完成时间（新增）
)