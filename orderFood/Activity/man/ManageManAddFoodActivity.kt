package com.example.orderfood.activity.man
import android.content.Context // 确保导入正确的 Context 包
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.orderfood.R
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.entity.Food
import com.example.orderfood.util.FileImgUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageManAddFoodActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private var currentImageUri: Uri? = null
    private lateinit var defaultBitmap: Bitmap
    private lateinit var sharedPreferences: SharedPreferences
    private var currentBusinessId: String? =null
    // 图片选择器
    private val getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { selectedUri: Uri? ->
        selectedUri?.let {
            imgView.setImageURI(it)
            currentImageUri = it
        } ?: Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_man_add_food)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar = findViewById<Toolbar>(R.id.man_manage_addFood_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        // 初始化默认图片的Bitmap
        val defaultDrawable = ContextCompat.getDrawable(this, R.drawable.upimg)
        defaultBitmap = if (defaultDrawable is BitmapDrawable) {
            defaultDrawable.bitmap
        } else {
            // 创建默认Bitmap作为后备
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
        sharedPreferences = this.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentBusinessId = sharedPreferences.getString("user_id", null) // 获取商家ID

        // 初始化视图
        imgView = findViewById(R.id.man_manage_addFood_img)
        val nameText = findViewById<EditText>(R.id.man_manage_addFood_name)

        val priceText = findViewById<EditText>(R.id.man_manage_addFood_price)
        val desText = findViewById<EditText>(R.id.man_manage_addFood_des)
        val btn = findViewById<Button>(R.id.man_manage_addFood_addBut)

        // 设置图片点击事件
        imgView.setOnClickListener { openGallery() }

        // 注册按钮点击事件
        btn.setOnClickListener {
            val name = nameText.text.toString().trim()
            val price = priceText.text.toString().trim()
            val des = desText.text.toString().trim()
            // 验证图片是否已选择
            val drawable = imgView.drawable
            if (drawable is BitmapDrawable) {
                val currentBitmap = drawable.bitmap

                when {
                    currentBitmap.sameAs(defaultBitmap) ->
                        Toast.makeText(this, "请点击图片添加食物图片", Toast.LENGTH_SHORT).show()
                    name.isEmpty() ->
                        Toast.makeText(this, "请输入食物名称", Toast.LENGTH_SHORT).show()
                    price.isEmpty() ->
                        Toast.makeText(this, "请输入价格", Toast.LENGTH_SHORT).show()
                    des.isEmpty() ->
                        Toast.makeText(this, "请输入食物描述", Toast.LENGTH_SHORT).show()
                    else -> registerFood(name,price, des)
                }
            } else {
                Toast.makeText(this, "图片格式错误", Toast.LENGTH_SHORT).show()
            }
        }
    }





    private fun registerFood(name: String, priceStr: String, description: String) {
        lifecycleScope.launch {
            try {
                // 1. 验证价格格式
                val price = try {
                    priceStr.toDouble()
                } catch (e: NumberFormatException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ManageManAddFoodActivity,
                            "价格格式不正确",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // 2. 检查图片是否已选择
                if (currentImageUri == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ManageManAddFoodActivity,
                            "请选择食物图片",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // 3. 保存图片
                val fileName = FileImgUntil.getImgName()
                val saveSuccess = withContext(Dispatchers.IO) {
                    currentImageUri?.let { uri ->
                        FileImgUntil.saveImageUriToFileimg(uri, this@ManageManAddFoodActivity, fileName)
                    } ?: false
                }

                if (!saveSuccess) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ManageManAddFoodActivity,
                            "保存图片失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // 4. 创建Food对象
                val food = Food(
                    businessId = currentBusinessId!!,
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = fileName
                )

                // 5. 插入数据库
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(applicationContext).foodDao().insert(food)
                }

                // 6. 显示成功消息
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ManageManAddFoodActivity,
                        "食物添加成功",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 清空表单
                    clearForm()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ManageManAddFoodActivity,
                        "添加失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun clearForm() {
        findViewById<EditText>(R.id.man_manage_addFood_name).text.clear()
        findViewById<EditText>(R.id.man_manage_addFood_price).text.clear()
        findViewById<EditText>(R.id.man_manage_addFood_des).text.clear()

        imgView.setImageResource(R.drawable.upimg)
        currentImageUri = null
    }

    private fun openGallery() {
        getContentLauncher.launch("image/*")
    }
}