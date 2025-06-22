package com.example.orderfood.activity.man.frament

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfood.R
import com.example.orderfood.activity.man.ManageManUpdateFoodActivity
import com.example.orderfood.activity.man.adapter.FoodListAdapter
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.dao.FoodDao
import com.example.orderfood.model.FoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageHomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: FoodListAdapter
    private lateinit var foodDao: FoodDao
    private var allFoodList = mutableListOf<FoodItem>() // 存储所有食物数据
    private var filteredFoodList = mutableListOf<FoodItem>() // 存储过滤后的食物数据
    private lateinit var sharedPreferences: SharedPreferences
    private var currentBusinessId: String? = null // 当前商家ID（账号）
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_manage_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化视图
        recyclerView = view.findViewById(R.id.man_home_food_listview)
        searchView = view.findViewById(R.id.searchView)
        sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentBusinessId = sharedPreferences.getString("user_id", null) // 获取商家ID

        // 检查商家ID是否存在（防止未登录或异常情况）
        if (currentBusinessId == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            return
        }
        // 初始化数据库访问对象
        val db = AppDatabase.getDatabase(requireContext())
        foodDao = db.foodDao()
        // 设置RecyclerView
        setupRecyclerView()

        // 设置搜索监听器
        setupSearchListener()

        // 加载食物数据
        loadFoodData(currentBusinessId!!)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FoodListAdapter(filteredFoodList) { selectedFood ->
            val intent = Intent(requireContext(), ManageManUpdateFoodActivity::class.java)
            intent.putExtra("food_id", selectedFood.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //根据食物名字来进行查找商品

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterFoods(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterFoods(query: String) {
        val businessId = currentBusinessId ?: run {
            Toast.makeText(requireContext(), "商家ID为空", Toast.LENGTH_SHORT).show()
            return // 空值时直接返回
        }
        lifecycleScope.launch{
            val foods = withContext(Dispatchers.IO){
                if(query.isEmpty())
                {
                    foodDao.getFoodsByBusinessId(businessId)
                }else
                {
                    foodDao.getFoodsByBusinessIdAndName(
                        businessId = businessId,
                        namePattern = "%$query%"
                    )
                }
            }
            allFoodList.clear()
            allFoodList.addAll(foods.map { entity ->
                FoodItem(
                    id = entity.foodId,
                    name = entity.name,
                    description = entity.description,
                    price = entity.price,
                    imageUrl = entity.imageUrl
                )
            })
            filteredFoodList.clear()
            filteredFoodList.addAll(allFoodList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadFoodData(businessId: String) {
        // 使用协程异步加载数据
        lifecycleScope.launch {
            // 检查数据库是否为空
            val isEmpty = withContext(Dispatchers.IO) {
                foodDao.getFoodCount() == 0
            }


            val foods = withContext(Dispatchers.IO) {
                // 从数据库获取所有食物
                foodDao.getFoodsByBusinessId(businessId)
            }

            // 更新数据
            allFoodList.clear()
            allFoodList.addAll(foods.map { entity ->
                // 将数据库实体转换为UI模型
                FoodItem(
                    id = entity.foodId,
                    name = entity.name,

                    description = entity.description,
                    price = entity.price,
                    imageUrl = entity.imageUrl
                )
            })

            // 初始化过滤列表为所有食物
            filteredFoodList.clear()
            filteredFoodList.addAll(allFoodList)

            // 更新UI
            adapter.notifyDataSetChanged()
        }
    }

}