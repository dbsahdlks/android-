package com.example.orderfood.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.orderfood.entity.OrderDetail
import com.example.orderfood.entity.MonthlyFoodSale

@Dao
interface OrderDetailDao {
    @Insert
    suspend fun insert(detail: OrderDetail)

    @Query("SELECT * FROM order_details WHERE orderId = :orderId")
    suspend fun getDetailsByOrder(orderId: Long): List<OrderDetail>

    @Query("""
        SELECT 
            foodId,
            foodName,
            SUM(quantity) AS totalSold,
            foodImageUrl
        FROM order_details
        WHERE orderId IN (
            SELECT orderId 
            FROM orders 
            WHERE strftime('%Y-%m', orderTime) = :currentMonth
        )
        GROUP BY foodId
        ORDER BY totalSold DESC
    """)
    suspend fun getMonthlyFoodSales(currentMonth: String): List<MonthlyFoodSale>
}