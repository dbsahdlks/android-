package com.example.orderfood.activity.man

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
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.dao.BusinessDao
import com.example.orderfood.entity.Business
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.orderfood.util.FileImgUntil


class RegisterManActivity : AppCompatActivity() {
    private lateinit var imgText: ImageView
    private var uri: Uri? = null
    private lateinit var businessDao: BusinessDao
    // 在 RegisterManActivity 类中添加密码哈希方法
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }
    private val getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { selectedUri: Uri? ->
        selectedUri?.let {
            imgText.setImageURI(it)
            uri = it
        } ?: Toast.makeText(this, "未选择头像", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(com.example.orderfood.R.layout.activity_register_man)

        // 初始化数据库
        val db = AppDatabase.getDatabase(applicationContext)
        businessDao = db.businessDao()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.orderfood.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 工具栏设置
        val toolbar = findViewById<Toolbar>(com.example.orderfood.R.id.register_man_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 初始化视图
        imgText = findViewById(com.example.orderfood.R.id.register_man_tx)
        val idText = findViewById<EditText>(com.example.orderfood.R.id.register_man_id)
        val pwdText = findViewById<EditText>(com.example.orderfood.R.id.register_man_pwd)
        val nameText = findViewById<EditText>(com.example.orderfood.R.id.register_man_name)
        val desText = findViewById<EditText>(com.example.orderfood.R.id.register_man_des)
        val typeTest = findViewById<EditText>(com.example.orderfood.R.id.register_man_type)
        val reg = findViewById<Button>(com.example.orderfood.R.id.register_man_zcsj)

        val defaultDrawable = ContextCompat.getDrawable(this, com.example.orderfood.R.drawable.upimg)
        // 注册按钮点击事件
        reg.setOnClickListener {
            val id = idText.text.toString().trim()
            val pwd = pwdText.text.toString().trim()
            val name = nameText.text.toString().trim()
            val des = desText.text.toString().trim()
            val type = typeTest.text.toString().trim()

            val drawable = imgText.drawable
            if (drawable is BitmapDrawable && defaultDrawable is BitmapDrawable) {
                val currentBitmap = drawable.bitmap
                val bitmapDef = defaultDrawable.bitmap

                when {
                    currentBitmap.sameAs(bitmapDef) ->
                        Toast.makeText(this, "请点击图片添加头像", Toast.LENGTH_SHORT).show()
                    id.isEmpty() ->
                        Toast.makeText(this, "请输入店铺账号", Toast.LENGTH_SHORT).show()
                    pwd.isEmpty() ->
                        Toast.makeText(this, "请输入店铺密码", Toast.LENGTH_SHORT).show()
                    name.isEmpty() ->
                        Toast.makeText(this, "请输入店铺名称", Toast.LENGTH_SHORT).show()
                    des.isEmpty() ->
                        Toast.makeText(this, "请输入店铺描述", Toast.LENGTH_SHORT).show()
                    type.isEmpty() ->
                        Toast.makeText(this, "请输入店铺类型", Toast.LENGTH_SHORT).show()
                    !isPasswordValid(pwd) -> // 添加密码强度检查
                        Toast.makeText(this, "密码需包含字母、数字和特殊字符，长度至少8位", Toast.LENGTH_SHORT).show()
                    else -> registerBusiness(id, pwd, name, des, type)
                }
            }
        }

        imgText.setOnClickListener { openGallery() }

    }

    private fun registerBusiness(id: String, pwd: String, name: String, des: String, type: String) {
        lifecycleScope.launch {
            try {
                // 检查账号是否已存在
                val exists = withContext(Dispatchers.IO) {
                    businessDao.businessIdExists(id) > 0
                }

                if (exists) {
                    Toast.makeText(
                        this@RegisterManActivity,
                        "账号已存在",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // 添加密码强度验证
                if (!isPasswordValid(pwd)) {
                    Toast.makeText(
                        this@RegisterManActivity,
                        "密码需包含字母、数字和特殊字符，长度至少8位",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // 保存图片
                val path = FileImgUntil.getImgName()
                uri?.let { FileImgUntil.saveImageUriToFileimg(it, this@RegisterManActivity, path) }

                // 创建商家对象 - 使用哈希后的密码
                val business = Business(
                    businessId = id,
                    password = hashPassword(pwd), // 使用哈希后的密码
                    name = name,
                    description = des,
                    type = type,
                    imagePath = path
                )

                // 插入数据库
                withContext(Dispatchers.IO) {
                    businessDao.insert(business)
                }

                Toast.makeText(
                    this@RegisterManActivity,
                    "商家注册成功",
                    Toast.LENGTH_SHORT
                ).show()

                // 清空表单
                clearForm()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@RegisterManActivity,
                    "注册失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 添加密码验证方法
    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isDigit() }) return false
        if (!password.any { it.isLetter() }) return false
        return password.any { !it.isLetterOrDigit() }
    }

    private fun clearForm() {
        findViewById<EditText>(com.example.orderfood.R.id.register_man_id).text.clear()
        findViewById<EditText>(com.example.orderfood.R.id.register_man_pwd).text.clear()
        findViewById<EditText>(com.example.orderfood.R.id.register_man_name).text.clear()
        findViewById<EditText>(com.example.orderfood.R.id.register_man_des).text.clear()
        findViewById<EditText>(com.example.orderfood.R.id.register_man_type).text.clear()
        imgText.setImageResource(com.example.orderfood.R.drawable.upimg)
        uri = null
    }

    private fun openGallery() {
        getContentLauncher.launch("image/*")
    }
}