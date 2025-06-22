package com.example.orderfood.activity.man.adapter

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfood.R
import com.example.orderfood.activity.man.ManageManOrderNotFinishActivity
import com.example.orderfood.entity.Order
import com.example.orderfood.entity.OrderDetail
import java.util.Locale

class ManageOrderAdapter(
    context: Context,
    private val orders: List<Order>,
    private val orderDetailsMap: Map<Long, List<OrderDetail>>
) : ArrayAdapter<Order>(context, R.layout.item_manage_order, orders) {

    private val statusMap = mapOf(
        "1" to "未处理",
        "2" to "已取消",
        "3" to "已完成"
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_manage_order, parent, false)

        val order = orders[position]
        val details = orderDetailsMap[order.orderId] ?: emptyList()

        view.findViewById<TextView>(R.id.order_id).text = "订单号: ${order.orderId}"
        view.findViewById<TextView>(R.id.order_time).text = formatDate(order.orderTime)
        view.findViewById<TextView>(R.id.order_address).text = order.orderAddress
        view.findViewById<TextView>(R.id.order_phone).text = order.customerPhone
        view.findViewById<TextView>(R.id.order_status).text = statusMap[order.state] ?: "未知状态"
        view.findViewById<TextView>(R.id.order_sum_price).text =
            "¥${calculateTotal(details)}"

        // 设置详情RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.order_recycleview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = OrderDetailAdapter(details)

        // 添加接单按钮
        val acceptButton = Button(context).apply {
            text = "接单"
            setOnClickListener {
                (context as ManageManOrderNotFinishActivity).acceptOrder(order.orderId)
            }
        }

        view.findViewById<LinearLayout>(R.id.button_container).addView(acceptButton)

        return view
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun calculateTotal(details: List<OrderDetail>): Double {
        return details.sumOf { it.foodPrice * it.quantity }
    }
}