package com.example.orderfood.activity.user


import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.registerForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.orderfood.R
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.dao.UserDao
import com.example.orderfood.entity.User
import com.example.orderfood.util.FileImgUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterUserActivity : AppCompatActivity() {
    private lateinit var imgText: ImageView
    private var uri: Uri? = null
    private lateinit var userDao: UserDao

    private val getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { selectedUri: Uri? ->
        selectedUri?.let {
            imgText.setImageURI(it)
            uri = it
        } ?: Toast.makeText(this, "未选择头像", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(com.example.orderfood.R.layout.activity_register_user)

        // 初始化数据库
        val db = AppDatabase.getDatabase(applicationContext)
        userDao = db.userDao()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.orderfood.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 工具栏设置
        val toolbar = findViewById<Toolbar>(com.example.orderfood.R.id.register_user_toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 初始化视图
        imgText = findViewById(com.example.orderfood.R.id.register_user_tx)
        val idText = findViewById<EditText>(com.example.orderfood.R.id.register_user_id)
        val pwdText = findViewById<EditText>(com.example.orderfood.R.id.register_user_pwd)
        val nameText = findViewById<EditText>(com.example.orderfood.R.id.register_user_name)
        val sexGroup = findViewById<RadioGroup>(R.id.register_user_group)
        val addressText = findViewById<EditText>(com.example.orderfood.R.id.register_user_address)
        val phoneText = findViewById<EditText>(com.example.orderfood.R.id.register_user_phone)
        val reg = findViewById<Button>(com.example.orderfood.R.id.register_user_zcyh)

        val defaultDrawable = ContextCompat.getDrawable(this, com.example.orderfood.R.drawable.upimg)
        // 注册按钮点击事件
        reg.setOnClickListener {
            val id = idText.text.toString().trim()
            val pwd = pwdText.text.toString().trim()
            val name = nameText.text.toString().trim()

            // 获取性别选择
            val selectedSexId = sexGroup.checkedRadioButtonId
            val sex = when (selectedSexId) {
                R.id.register_user_nan -> "男"
                R.id.register_user_nv -> "女"
                else -> "未知"
            }

            val address = addressText.text.toString().trim()
            val phone = phoneText.text.toString().trim()

            val drawable = imgText.drawable
            if (drawable is BitmapDrawable && defaultDrawable is BitmapDrawable) {
                val currentBitmap = drawable.bitmap
                val bitmapDef = defaultDrawable.bitmap

                when {
                    currentBitmap.sameAs(bitmapDef) ->
                        Toast.makeText(this, "请点击图片添加头像", Toast.LENGTH_SHORT).show()
                    id.isEmpty() ->
                        Toast.makeText(this, "请输入用户账号", Toast.LENGTH_SHORT).show()
                    pwd.isEmpty() ->
                        Toast.makeText(this, "请输入用户密码", Toast.LENGTH_SHORT).show()
                    name.isEmpty() ->
                        Toast.makeText(this, "请输入用户昵称 ", Toast.LENGTH_SHORT).show()
                    address.isEmpty() ->
                        Toast.makeText(this, "请输入收货地址", Toast.LENGTH_SHORT).show()
                    phone.isEmpty() ->
                        Toast.makeText(this, "请输入联系方式", Toast.LENGTH_SHORT).show()
                    !isPasswordValid(pwd) -> // 添加密码强度检查
                        Toast.makeText(this, "密码需包含字母、数字和特殊字符，长度至少8位", Toast.LENGTH_SHORT).show()
                    else -> registerUser(id, pwd, name, sex, address, phone)
                }
            }
        }

        imgText.setOnClickListener { openGallery() }
    }

    private fun registerUser(id: String, pwd: String, name: String, sex: String, address: String, phone: String) {
        lifecycleScope.launch {
            try {
                // 检查账号是否已存在
                val exists = withContext(Dispatchers.IO) {
                    userDao.userIdExists(id) > 0
                }

                if (exists) {
                    Toast.makeText(
                        this@RegisterUserActivity,
                        "账号已存在",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // 保存图片
                val path = FileImgUntil.getImgName()
                uri?.let { FileImgUntil.saveImageUriToFileimg(it, this@RegisterUserActivity, path) }

                // 创建用户对象 - 使用哈希后的密码
                val user = User(
                    userId = id,
                    password = hashPassword(pwd), // 使用哈希后的密码
                    name = name,
                    sex = sex,
                    address = address,
                    phone = phone,
                    imagePath = path
                )

                // 插入数据库
                withContext(Dispatchers.IO) {
                    userDao.insert(user)
                }

                Toast.makeText(
                    this@RegisterUserActivity,
                    "用户注册成功",
                    Toast.LENGTH_SHORT
                ).show()

                // 清空表单
                clearForm()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@RegisterUserActivity,
                    "注册失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 密码哈希方法
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    // 密码验证方法
    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isDigit() }) return false
        if (!password.any { it.isLetter() }) return false
        return password.any { !it.isLetterOrDigit() }
    }

    private fun clearForm() {
        findViewById<EditText>(com.example.orderfood.R.id.register_user_id).text.clear()
        findViewById<EditText>(com.example.orderfood.R.id.register_user_pwd).text.clear()
        findViewById<EditText>(com.example.orderfood.R.id.register_user_name).text.clear()
        findViewById<EditText>(com.example.orderfood.R.id.register_user_address).text.clear()
        findViewById<EditText>(com.example.orderfood.R.id.register_user_phone).text.clear()

        // 重置性别选择
        val sexGroup = findViewById<RadioGroup>(R.id.register_user_group)
        sexGroup.clearCheck()

        imgText.setImageResource(com.example.orderfood.R.drawable.upimg)
        uri = null
    }

    private fun openGallery() {
        getContentLauncher.launch("image/*")
    }
}