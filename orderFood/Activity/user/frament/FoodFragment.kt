package com.example.orderfood.activity.user.frament

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfood.R
import com.example.orderfood.activity.user.adapter.FoodAdapter
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.dao.FoodDao
import com.example.orderfood.dao.OrderDao
import com.example.orderfood.dao.OrderDetailDao
import com.example.orderfood.dao.UserDao
import com.example.orderfood.entity.Food
import com.example.orderfood.entity.Order
import com.example.orderfood.entity.OrderDetail
import com.example.orderfood.model.CartItem
import com.example.orderfood.databinding.FragmentFoodBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var foodAdapter: FoodAdapter
    private lateinit var foodDao: FoodDao
    private lateinit var orderDao: OrderDao
    private lateinit var orderDetailDao: OrderDetailDao
    private lateinit var userDao: UserDao
    private var businessId: String? = null
    private var cartItems: MutableList<CartItem> = mutableListOf()

    // 用于通知购物车更新的回调
    private var onCartUpdate: (() -> Unit)? = null
    // 用于通知订单提交结果的回调
    private var onOrderSubmitted: ((Boolean) -> Unit)? = null

    // 工厂方法，传递参数
    companion object {
        fun newInstance(businessId: String, cartItems: MutableList<CartItem>): FoodFragment {
            val fragment = FoodFragment()
            val args = Bundle()
            args.putString("business_id", businessId)
            fragment.arguments = args
            fragment.cartItems = cartItems
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 安全的接口实现检查
        if (context is OnCartUpdateListener) {
            onCartUpdate = context::updateCart
        } else {
            onCartUpdate = { } // 提供默认空实现
        }

        if (context is OnOrderSubmittedListener) {
            onOrderSubmitted = context::onOrderResult
        } else {
            onOrderSubmitted = { result -> } // 提供默认空实现
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        businessId = arguments?.getString("business_id")

        // 初始化数据库操作对象
        val db = AppDatabase.getDatabase(requireContext())
        foodDao = db.foodDao()
        orderDao = db.orderDao()
        orderDetailDao = db.orderDetailDao()
        userDao = db.userDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置RecyclerView
        setupRecyclerView()

        // 检查businessId是否为空
        if (businessId == null) {
            showToast("商家ID不能为空")
            return
        }

        // 加载食物数据
        loadFoodData()
    }

    private fun setupRecyclerView() {
        // 初始化RecyclerView
        binding.foodRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 添加分割线
        binding.foodRecyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )

        // 设置固定大小以优化性能
        binding.foodRecyclerView.setHasFixedSize(true)

        // 初始化适配器并设置回调
        foodAdapter = FoodAdapter(mutableListOf()) { food ->
            addToCart(food)
            onCartUpdate?.invoke() // 安全调用
        }
        binding.foodRecyclerView.adapter = foodAdapter
    }

    private fun loadFoodData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val foods = withContext(Dispatchers.IO) {
                    foodDao.getFoodsByBusinessId(businessId!!)
                }
                foodAdapter.updateList(foods)
            } catch (e: Exception) {
                showToast("加载食物数据失败: ${e.message}")
            }
        }
    }

    private fun addToCart(food: Food) {
        val existingItem = cartItems.find { it.food.foodId == food.foodId }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(food, 1))
        }
        showToast("${food.name} 已添加到购物车")
    }

    // 结账方法 - 生成订单
    fun checkout() {
        if (cartItems.isEmpty()) {
            showToast("购物车为空")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val orderTime = dateFormat.format(Date())

        // 从SharedPreferences获取userId
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "") ?: run {
            showToast("用户信息错误")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. 查询用户信息
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserById(userId)
                } ?: run {
                    showToast("用户信息不存在")
                    return@launch
                }

                // 2. 创建订单主表记录
                val order = Order(
                    businessId = businessId!!,
                    userId = userId,
                    state = "1", // 1:未处理
                    orderTime = orderTime,
                    orderAddress = user.address ?: "",
                    customerName = user.name,
                    customerPhone = user.phone,
                    completionTime = ""
                )

                // 3. 插入订单并获取orderId
                val orderId = withContext(Dispatchers.IO) {
                    orderDao.insert(order)
                }

                // 4. 创建订单详情记录
                val orderDetails = cartItems.mapIndexed { index, cartItem ->
                    OrderDetail(
                        orderId = orderId,
                        foodId = cartItem.food.foodId,
                        foodName = cartItem.food.name,
                        foodDescription = cartItem.food.description,
                        foodPrice = cartItem.food.price,
                        quantity = cartItem.quantity,
                        foodImageUrl = cartItem.food.imageUrl ?: ""
                    )
                }

                // 5. 插入订单详情
                withContext(Dispatchers.IO) {
                    orderDetailDao.insertAll(orderDetails)
                }

                // 6. 订单提交成功处理
                showToast("订单提交成功")
                cartItems.clear()
                updateCart()
                onOrderSubmitted?.invoke(true)

            } catch (e: Exception) {
                e.printStackTrace()
                showToast("订单提交失败: ${e.message}")
                onOrderSubmitted?.invoke(false)
            }
        }
    }

    private fun updateCart() {
        activity?.runOnUiThread {
            onCartUpdate?.invoke()
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 购物车更新回调接口
    interface OnCartUpdateListener {
        fun updateCart()
    }

    // 订单提交结果回调接口
    interface OnOrderSubmittedListener {
        fun onOrderResult(success: Boolean)
    }
}