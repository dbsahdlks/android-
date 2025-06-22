package com.example.orderfood.activity.man

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.bumptech.glide.Glide
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.entity.Food
import com.example.orderfood.util.FileImgUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.appcompat.app.AlertDialog

class ManageManUpdateFoodActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private var currentImageUri: Uri? = null
    private lateinit var defaultBitmap: Bitmap
    private var currentFoodId: Long = -1
    private var originalImageUrl: String? = null

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
        setContentView(com.example.orderfood.R.layout.activity_manage_man_update_food)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.orderfood.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar = findViewById<Toolbar>(com.example.orderfood.R.id.man_manage_updateFood_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        // 初始化默认图片的Bitmap
        val defaultDrawable = ContextCompat.getDrawable(this, com.example.orderfood.R.drawable.upimg)
        defaultBitmap = if (defaultDrawable is BitmapDrawable) {
            defaultDrawable.bitmap
        } else {
            // 创建默认Bitmap作为后备
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }

        // 初始化视图
        imgView = findViewById(com.example.orderfood.R.id.man_manage_updateFood_img)
        val nameText = findViewById<EditText>(com.example.orderfood.R.id.man_manage_updateFood_name)
        val priceText = findViewById<EditText>(com.example.orderfood.R.id.man_manage_updateFood_price)
        val bidText = findViewById<EditText>(com.example.orderfood.R.id.man_manage_updateFood_businessId)
        val desText = findViewById<EditText>(com.example.orderfood.R.id.man_manage_updateFood_des)
        val btn = findViewById<Button>(com.example.orderfood.R.id.man_manage_updateFood_btn)

        // 1. 接收传递的商品信息
        currentFoodId = intent.getLongExtra("food_id", -1)
        val foodName = intent.getStringExtra("food_name") ?: ""
        val foodPrice = intent.getDoubleExtra("food_price", 0.0)
        val foodDescription = intent.getStringExtra("food_description") ?: ""
        val businessId = intent.getStringExtra("food_businessId") ?: "root"
        originalImageUrl = intent.getStringExtra("food_imageUrl")

        // 2. 填充表单数据
        nameText.setText(foodName)
        priceText.setText(foodPrice.toString())
        desText.setText(foodDescription)
        bidText.setText(businessId)

        // 3. 加载商品图片
        loadFoodImage()

        // 设置图片点击事件
        imgView.setOnClickListener { openGallery() }

        // 修改按钮文本为"更新"
        btn.text = getString(com.example.orderfood.R.string.update)

        // 注册按钮点击事件
        btn.setOnClickListener {
            val name = nameText.text.toString().trim()
            val bid = bidText.text.toString().trim()
            val price = priceText.text.toString().trim()
            val des = desText.text.toString().trim()

            // 验证表单
            if (name.isEmpty() || bid.isEmpty() || price.isEmpty() || des.isEmpty()) {
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 执行更新
            updateFood(name, bid, price, des)
        }
    }

    private fun loadFoodImage() {
        if (!originalImageUrl.isNullOrEmpty()) {
            // 根据图片URL类型加载图片
            when {
                originalImageUrl!!.startsWith("http") -> {
                    // 网络图片
                    Glide.with(this)
                        .load(originalImageUrl)
                        .placeholder(com.example.orderfood.R.drawable.k)
                        .error(com.example.orderfood.R.drawable.cannottupain)
                        .into(imgView)
                }
                originalImageUrl!!.startsWith("content://") -> {
                    // Content URI
                    Glide.with(this)
                        .load(Uri.parse(originalImageUrl))
                        .placeholder(com.example.orderfood.R.drawable.k)
                        .error(com.example.orderfood.R.drawable.cannottupain)
                        .into(imgView)
                }
                else -> {
                    // 本地图片
                    val file = File(filesDir, originalImageUrl!!)
                    if (file.exists()) {
                        Glide.with(this)
                            .load(file)
                            .placeholder(com.example.orderfood.R.drawable.k)
                            .error(com.example.orderfood.R.drawable.cannottupain)
                            .into(imgView)
                    } else {
                        // 文件不存在，使用默认图片
                        imgView.setImageResource(com.example.orderfood.R.drawable.upimg)
                        Toast.makeText(this, "图片文件不存在", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // 没有图片URL，使用默认图片
            imgView.setImageResource(com.example.orderfood.R.drawable.upimg)
        }
    }

    private fun updateFood(name: String, businessId: String, priceStr: String, description: String) {
        lifecycleScope.launch {
            try {
                // 1. 验证价格格式
                val price = try {
                    priceStr.toDouble()
                } catch (e: NumberFormatException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ManageManUpdateFoodActivity,
                            "价格格式不正确",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // 2. 处理图片更新
                val imageUrl = if (currentImageUri != null) {
                    // 用户选择了新图片
                    val fileName = FileImgUntil.getImgName()
                    val saveSuccess = withContext(Dispatchers.IO) {
                        FileImgUntil.saveImageUriToFileimg(
                            currentImageUri!!,
                            this@ManageManUpdateFoodActivity,
                            fileName
                        )
                    }

                    if (!saveSuccess) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ManageManUpdateFoodActivity,
                                "保存图片失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }

                    // 返回新图片的文件名
                    fileName
                } else {
                    // 使用原图片
                    originalImageUrl ?: ""
                }

                // 3. 创建更新后的Food对象
                val updatedFood = Food(
                    foodId = currentFoodId, // 保留原ID
                    businessId = businessId,
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = imageUrl
                )

                // 4. 更新数据库
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(applicationContext).foodDao().update(updatedFood)
                }

                // 5. 显示成功消息
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ManageManUpdateFoodActivity,
                        "商品更新成功",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 结束当前Activity
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ManageManUpdateFoodActivity,
                        "更新失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun openGallery() {
        getContentLauncher.launch("image/*")
    }

    companion object {
        fun start(context: Context, food: Food) {
            val intent = Intent(context, ManageManUpdateFoodActivity::class.java).apply {
                putExtra("food_id", food.foodId)
                putExtra("food_name", food.name)
                putExtra("food_price", food.price)
                putExtra("food_description", food.description)
                putExtra("food_businessId", food.businessId)
                putExtra("food_imageUrl", food.imageUrl)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.example.orderfood.R.menu.man_manage_food_del_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == com.example.orderfood.R.id.man_manage_food_del) {
            showDeleteConfirmationDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showDeleteConfirmationDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("删除商品")
            .setMessage("确定要删除此商品吗？此操作不可撤销。")
            .setPositiveButton("删除") { dialog, which ->
                // 用户确认删除，执行删除操作
                deleteFood()
            }
            .setNegativeButton("取消") { dialog, which ->
                // 用户取消删除，关闭对话框
                dialog.dismiss()
            }
            .create()
            .show()
    }
    private fun deleteFood() {
        lifecycleScope.launch {
            try {
                // 1. 删除本地图片文件（如果有）
                if (!originalImageUrl.isNullOrEmpty() && !originalImageUrl!!.startsWith("http")) {
                    withContext(Dispatchers.IO) {
                        val file = File(filesDir, originalImageUrl!!)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }

                // 2. 从数据库删除商品
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(applicationContext).foodDao().deleteById(currentFoodId)
                }

                // 3. 返回成功消息并关闭页面
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageManUpdateFoodActivity, "商品已删除", Toast.LENGTH_SHORT).show()
                    finish() // 关闭当前页面
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageManUpdateFoodActivity, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}