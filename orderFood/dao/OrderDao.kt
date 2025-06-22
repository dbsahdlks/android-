package com.example.orderfood.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.orderfood.entity.Order
import com.example.orderfood.entity.OrderDetail

@Dao
interface OrderDao {

    // ====================== 插入操作 ======================
    @Insert
    suspend fun insert(order: Order): Long

    @Insert
    suspend fun insertAll(details: List<OrderDetail>)

    @Transaction
    suspend fun createOrderWithDetails(order: Order, details: List<OrderDetail>) {
        val orderId = insert(order)
        details.forEach { it.orderId = orderId }
        insertAll(details)
    }

    // ====================== 查询操作 ======================
    @Query("SELECT * FROM orders WHERE state = :status")
    suspend fun getOrdersByStatus(status: String): List<Order>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderTime DESC")
    suspend fun getOrdersByUser(userId: String): List<Order>

    @Query("SELECT * FROM orders WHERE businessId = :businessId ORDER BY orderTime DESC")
    suspend fun getAllOrdersByBusiness(businessId: String): List<Order>
    @Query("SELECT * FROM orders WHERE businessId = :businessId AND state = :status")
    suspend fun getOrdersByBusinessAndStatus(businessId: String, status: String): List<Order>
    @Query("SELECT * FROM orders WHERE businessId = :businessId AND state = :status LIMIT :limit OFFSET :offset")
    suspend fun getOrdersByBusinessAndStatusWithPaging(
        businessId: String,
        status: String,
        limit: Int,
        offset: Int
    ): List<Order>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: Long): Order?

    @Query("SELECT * FROM order_details WHERE orderId = :orderId")
    suspend fun getOrderDetails(orderId: Long): List<OrderDetail>

    // ====================== 搜索操作 ======================
    @Query("""
        SELECT * FROM orders 
        WHERE state = :status 
        AND (orderId = :query 
            OR customerName LIKE '%' || :query || '%'
            OR customerPhone LIKE '%' || :query || '%'
            OR orderAddress LIKE '%' || :query || '%')
    """)
    suspend fun searchUnprocessedOrders(query: String, status: String): List<Order>

    @Query("SELECT * FROM orders WHERE orderId = :orderId AND state = :status")
    suspend fun searchByIdAndStatus(orderId: Long, status: String): List<Order>

    // ====================== 更新操作 ======================
    @Query("UPDATE orders SET state = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE orders SET completionTime = :time WHERE orderId = :orderId")
    suspend fun updateCompletionTime(orderId: Long, time: String)

    @Query("UPDATE orders SET orderAddress = :address WHERE orderId = :orderId")
    suspend fun updateOrderAddress(orderId: Long, address: String)

    // ====================== 删除操作 ======================
    @Query("DELETE FROM orders WHERE orderId = :orderId")
    suspend fun deleteOrder(orderId: Long)
}