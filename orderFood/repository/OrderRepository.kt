package com.example.orderfood.repository

import com.example.orderfood.dao.OrderDao
import com.example.orderfood.entity.Order
import com.example.orderfood.entity.OrderDetail

class OrderRepository(private val orderDao: OrderDao) {

    // 获取所有未处理订单（状态为"1"）
    suspend fun getUnprocessedOrdersByBusiness(businessId: String): List<Order> {
        return orderDao.getOrdersByBusinessAndStatus(businessId, "1")
    }

    // 搜索未处理订单 - 优化搜索逻辑
    suspend fun searchUnprocessedOrders(query: String): List<Order> {
        return if (query.matches(Regex("\\d+"))) {
            // 如果查询是纯数字，尝试按订单ID搜索
            val orderId = query.toLongOrNull()
            if (orderId != null) {
                orderDao.searchByIdAndStatus(orderId, "1")
            } else {
                // 如果不是有效ID，按其他字段搜索
                orderDao.searchUnprocessedOrders("%$query%", "1")
            }
        } else {
            // 非数字查询，按名称、地址等搜索
            orderDao.searchUnprocessedOrders("%$query%", "1")
        }
    }

    // 获取订单详情
    suspend fun getOrderDetails(orderId: Long): List<OrderDetail> {
        return orderDao.getOrderDetails(orderId)
    }

    // 更新订单状态 - 添加事务处理
    suspend fun updateOrderStatus(orderId: Long, status: String) {
        orderDao.updateOrderStatus(orderId, status)

        // 如果订单完成，更新完成时间
        if (status == "3") {
            orderDao.updateCompletionTime(orderId, System.currentTimeMillis().toString())
        }
    }
}