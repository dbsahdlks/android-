package com.example.orderfood.activity.man
import com.example.orderfood.dao.AppDatabase
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.orderfood.R
import com.example.orderfood.activity.man.adapter.ManageOrderAdapter
import com.example.orderfood.entity.Order
import com.example.orderfood.entity.OrderDetail
import com.example.orderfood.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 订单的状态 1、未处理的订单 2、取消订单 3、完成订单
class ManageManOrderNotFinishActivity : AppCompatActivity() {
    private lateinit var businessId: String
    private lateinit var orderRepository: OrderRepository
    private lateinit var orderListView: ListView
    private lateinit var adapter: ManageOrderAdapter
    private val orders = mutableListOf<Order>()
    private val orderDetailsMap = mutableMapOf<Long, List<OrderDetail>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_man_order_not_finish)

        // 设置工具栏
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.man_my_order_no_finish_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // 初始化搜索视图
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.man_my_order_no_finish_searchView)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchOrders(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchOrders(newText)
                return true
            }
        })

        // 初始化ListView
        orderListView = findViewById(R.id.man_my_order_no_finish_listView)
        adapter = ManageOrderAdapter(this, orders, orderDetailsMap)
        orderListView.adapter = adapter

        // 设置订单点击事件
        orderListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val order = orders[position]
            showOrderDetails(order)
        }

        // 设置边距适配
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val db = AppDatabase.getDatabase(this)
        val orderDao = db.orderDao()
        orderRepository = OrderRepository(orderDao)
        businessId = intent.getStringExtra("business_id") ?: error("business_id is required")
        // 加载未处理订单
        loadUnprocessedOrders()
    }

    private fun searchOrders(query: String?) {
        // 实现搜索功能
        if (query.isNullOrEmpty()) {
            loadUnprocessedOrders()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val searchResults = orderRepository.searchUnprocessedOrders(query)
                withContext(Dispatchers.Main) {
                    updateOrdersUI(searchResults)
                }
            }
        }
    }

    private fun loadUnprocessedOrders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val unprocessedOrders = orderRepository.getUnprocessedOrdersByBusiness(businessId)
                val detailsMap = mutableMapOf<Long, List<OrderDetail>>()

                unprocessedOrders.forEach { order ->
                    orderRepository.getOrderDetails(order.orderId).also {
                        detailsMap[order.orderId] = it
                    }
                }

                withContext(Dispatchers.Main) {
                    orders.clear()
                    orders.addAll(unprocessedOrders)
                    orderDetailsMap.clear()
                    orderDetailsMap.putAll(detailsMap)
                    adapter.notifyDataSetChanged()

                    if (orders.isEmpty()) {
                        Toast.makeText(this@ManageManOrderNotFinishActivity,
                            "没有未处理的订单", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageManOrderNotFinishActivity,
                        "加载订单失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateOrdersUI(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        adapter.notifyDataSetChanged()

        if (orders.isEmpty()) {
            Toast.makeText(this, "没有未处理的订单", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOrderDetails(order: Order) {
        // 这里可以跳转到订单详情页
        Toast.makeText(this, "查看订单 ${order.orderId} 详情", Toast.LENGTH_SHORT).show()
        // 实际开发中添加跳转到订单详情页的代码
        /*
        val intent = Intent(this, OrderDetailActivity::class.java)
        intent.putExtra("ORDER_ID", order.orderId)
        startActivity(intent)
        */
    }
    // 修改接单方法
    fun acceptOrder(orderId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 更新为已接单状态（假设状态"3"表示已接单）
                orderRepository.updateOrderStatus(orderId, "3")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageManOrderNotFinishActivity, "订单已接单", Toast.LENGTH_SHORT).show()
                    loadUnprocessedOrders()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageManOrderNotFinishActivity, "接单失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}