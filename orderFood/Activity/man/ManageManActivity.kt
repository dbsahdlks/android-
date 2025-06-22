package com.example.orderfood.activity.man

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.orderfood.R
import com.example.orderfood.activity.man.frament.ManageHomeFragment
import com.example.orderfood.activity.man.frament.ManageMyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManageManActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var currentBusinessId: String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_man)

        // 设置窗口插入监听器
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 加载初始Fragment
        loadHomeFragment()
        sharedPreferences = this.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentBusinessId = sharedPreferences.getString("user_id", null) // 获取商家ID
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.man_manage_bottom_menu)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val transaction = supportFragmentManager.beginTransaction()

            when (menuItem.itemId) {
                R.id.man_manage_bottom_menu_home -> {
                    transaction.replace(R.id.man_manage_frame, ManageHomeFragment())
                }
                R.id.man_manage_bottom_menu_add -> {
                    startActivity(Intent(this, ManageManAddFoodActivity::class.java))
                }
                R.id.man_manage_bottom_menu_my -> {
                    val businessId = currentBusinessId ?: "default_id" // 处理 null 情况
                    transaction.replace(R.id.man_manage_frame, ManageMyFragment.newInstance(businessId))
                }
            }

            transaction.commit()
            true
        }
    }

    private fun loadHomeFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.man_manage_frame, ManageHomeFragment())
            .commit()
    }
}