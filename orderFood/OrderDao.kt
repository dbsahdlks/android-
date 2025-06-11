package com.example.orderfood.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.orderfood.entity.Order

@Dao
interface OrderDao {
    @Insert
    suspend fun insert(order: Order): Long

    @Query("SELECT * FROM orders WHERE userId = :userId")
    suspend fun getOrdersByUser(userId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE businessId = :businessId")
    suspend fun getOrdersByBusiness(businessId: Long): List<Order>
}