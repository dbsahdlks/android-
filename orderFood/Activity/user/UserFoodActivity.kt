package com.example.orderfood.activity.user

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.orderfood.R
import com.example.orderfood.activity.user.adapter.CartAdapter
import com.example.orderfood.activity.user.frament.FoodFragment
import com.example.orderfood.activity.user.frament.UserCommentFragment
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.dao.FoodDao
import com.example.orderfood.model.CartItem
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserFoodActivity : AppCompatActivity(), FoodFragment.OnCartUpdateListener {

    private lateinit var foodDao: FoodDao
    private var businessId: String? = null
    val cartItems = mutableListOf<CartItem>()
    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var checkoutButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_food)

        // 获取商家ID
        businessId = intent.getStringExtra("business_id") ?: run {
            Toast.makeText(this, "商家信息错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 初始化视图
        initCartView()

        // 初始化数据库
        val db = AppDatabase.getDatabase(this)
        foodDao = db.foodDao()

        // 设置ViewPager和选项卡
        setupViewPager()

        // 加载商家信息
        loadBusinessInfo()

        // 计算并显示初始总价
        updateTotalPrice()
    }

    private fun initCartView() {
        // 购物车容器
        val cartContainer = findViewById<View>(R.id.cart_container)

        // 购物车RecyclerView
        val cartRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.cart_recycler_view)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)

        // 总价显示
        totalPriceTextView = findViewById(R.id.total_price)

        // 结账按钮
        checkoutButton = findViewById(R.id.checkout_button)
        checkoutButton.setOnClickListener {
            // 调用FoodFragment的checkout方法
            val foodFragment = supportFragmentManager.fragments.firstOrNull {
                it is FoodFragment
            } as? FoodFragment
            foodFragment?.checkout()
        }

        // 初始化购物车适配器
        cartAdapter = CartAdapter(cartItems,
            onIncrease = { position -> increaseQuantity(position) },
            onDecrease = { position -> decreaseQuantity(position) }
        )
        cartRecyclerView.adapter = cartAdapter
    }

    private fun setupViewPager() {
        // 创建ViewPager适配器
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): androidx.fragment.app.Fragment {
                return when (position) {
                    0 -> FoodFragment.newInstance(businessId!!, cartItems) // 点餐页面
                    else -> UserCommentFragment.newInstance(businessId!!) // 评论页面
                }
            }

            override fun getItemCount(): Int = 2 // 点餐和评论两个页面
        }

        // 设置ViewPager
        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.dianPu_pager)
        viewPager.adapter = pagerAdapter

        // 设置选项卡
        val tabLayout = findViewById<com.google.android.material.tabs.TabLayout>(R.id.dianPu_tab)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "点餐"
                else -> "评论"
            }
        }.attach()

        // 监听ViewPager页面变化，控制购物车显示
        viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // 只有在点餐页面显示购物车
                findViewById<View>(R.id.cart_container).visibility = if (position == 0) View.VISIBLE else View.GONE
            }
        })
    }

    private fun loadBusinessInfo() {
        lifecycleScope.launch {
            // 假设Business实体和businessDao存在
            val business = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@UserFoodActivity).businessDao().getBusinessById(businessId!!)
            } ?: return@launch

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.dianPu_name).text = business.name
                findViewById<TextView>(R.id.dianPu_des).text = "简介：${business.description}"
            }
        }
    }

    // 增加商品数量
    private fun increaseQuantity(position: Int) {
        if (position < cartItems.size) {
            cartItems[position].quantity++
            cartAdapter.notifyItemChanged(position)
            updateTotalPrice()
        }
    }

    // 减少商品数量
    private fun decreaseQuantity(position: Int) {
        if (position < cartItems.size) {
            val item = cartItems[position]
            if (item.quantity > 1) {
                item.quantity--
                cartAdapter.notifyItemChanged(position)
            } else {
                cartItems.removeAt(position)
                cartAdapter.notifyItemRemoved(position)
            }
            updateTotalPrice()
        }
    }

    // 更新购物车总价
    private fun updateTotalPrice() {
        val totalPrice = cartItems.sumOf { it.food.price * it.quantity }
        totalPriceTextView.text = "¥${totalPrice}"
    }

    // 实现FoodFragment的购物车更新回调
    override fun updateCart() {
        cartAdapter.notifyDataSetChanged()
        updateTotalPrice()
    }
}