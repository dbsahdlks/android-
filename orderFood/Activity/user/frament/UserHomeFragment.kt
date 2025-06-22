package com.example.orderfood.activity.user.frament

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfood.R
import com.example.orderfood.activity.user.UserFoodActivity
import com.example.orderfood.activity.user.adapter.BusinessAdapter
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.dao.BusinessDao
import com.example.orderfood.entity.Business
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserHomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: BusinessAdapter
    private lateinit var businessDao: BusinessDao
    private var allBusinessList = mutableListOf<Business>()
    private var filteredBusinessList = mutableListOf<Business>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.user_home_business_recyclerView)
        searchView = view.findViewById(R.id.user_home_searchView)

        // 初始化数据库访问对象
        val db = AppDatabase.getDatabase(requireContext())
        businessDao = db.businessDao()

        // 设置RecyclerView
        setupRecyclerView()

        // 设置搜索监听器
        setupSearchListener()

        // 加载商家数据
        loadBusinessData()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BusinessAdapter(filteredBusinessList) { selectedBusiness ->
            val intent = Intent(requireContext(), UserFoodActivity::class.java)
            intent.putExtra("business_id", selectedBusiness.businessId)//点餐时把相应的商家ID传进去
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBusinesses(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterBusinesses(query: String) {
        filteredBusinessList.clear()
        if (query.isEmpty()) {
            filteredBusinessList.addAll(allBusinessList)
        } else {
            val lowerCaseQuery = query.lowercase()
            allBusinessList.forEach { business ->
                if (business.name.lowercase().contains(lowerCaseQuery) ||
                    business.type.lowercase().contains(lowerCaseQuery)) {
                    filteredBusinessList.add(business)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun loadBusinessData() {
        lifecycleScope.launch {
            val businesses = withContext(Dispatchers.IO) {
                businessDao.getAllBusinesses()
            }
            allBusinessList.clear()
            allBusinessList.addAll(businesses)
            filteredBusinessList.clear()
            filteredBusinessList.addAll(allBusinessList)
            adapter.notifyDataSetChanged()
        }
    }
}