package com.example.orderfood.activity.man

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.orderfood.R
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.dao.BusinessDao
import com.example.orderfood.entity.Business // 确保导入实体类
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import at.favre.lib.crypto.bcrypt.BCrypt // 添加 BCrypt 导入
import com.google.android.material.textfield.TextInputLayout

class ManageManUpdatePwdActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var businessDao: BusinessDao
    private lateinit var currentBusinessId: String
    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_man_update_pwd)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = AppDatabase.getDatabase(applicationContext)
        businessDao = db.businessDao()
        currentBusinessId = intent.getStringExtra("business_id") ?: "" // 从 Intent 获取 BusinessId
        if (currentBusinessId.isEmpty()) {
            Toast.makeText(this, "商家信息异常", Toast.LENGTH_SHORT).show()
            finish() // 关闭界面
        }
        initToolbar()
        val pwdBtn = findViewById<Button>(R.id.man_manage_updatePwd_btn)
        pwdBtn.setOnClickListener { updatePassword() }
        tilNewPassword = findViewById(R.id.til_new_password)
        tilConfirmPassword = findViewById(R.id.til_confirm_password)
        setupTextWatchers()
    }
    private fun setupTextWatchers() {
        // 监听新密码输入
        findViewById<EditText>(R.id.man_manage_updatePwd_newPwd).addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validatePasswords()
            }
        })

        // 监听确认密码输入
        findViewById<EditText>(R.id.man_manage_updatePwd_cPwd).addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validatePasswords()
            }
        })
    }
    private fun validatePasswords(): Boolean {
        val newPassword = findViewById<EditText>(R.id.man_manage_updatePwd_newPwd).text.toString().trim()
        val confirmPassword = findViewById<EditText>(R.id.man_manage_updatePwd_cPwd).text.toString().trim()

        // 重置错误提示
        tilNewPassword.error = null
        tilConfirmPassword.error = null

        // 校验新密码：非空 + 强度
        if (newPassword.isEmpty()) {
            tilNewPassword.error = "请输入新密码"
            return false // 不通过
        } else if (!isPasswordValid(newPassword)) {
            tilNewPassword.error = "密码需包含字母、数字和特殊字符，长度至少8位"
            return false // 不通过
        }

        // 校验确认密码：非空 + 一致性
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "请输入确认密码"
            return false // 不通过
        } else if (newPassword != confirmPassword) {
            tilConfirmPassword.error = "两次密码输入不一致"
            return false // 不通过
        }

        // 所有校验通过
        return true
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.man_manage_updatePwd_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun updatePassword() {
        // 调用统一校验方法，获取是否通过
        if (!validatePasswords()) {
            return // 校验不通过，直接返回
        }

        // 校验通过，执行密码更新逻辑
        val newPassword = findViewById<EditText>(R.id.man_manage_updatePwd_newPwd).text.toString().trim()
        launchUpdatePassword(newPassword)
    }

    private fun launchUpdatePassword(newPassword: String) {
        lifecycleScope.launch {
            // 获取商家信息（在 IO 线程执行）
            val business = withContext(Dispatchers.IO) {
                businessDao.getBusinessById(currentBusinessId)
            }

            // 检查商家是否存在（在主线程处理 UI 提示）
            if (business == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ManageManUpdatePwdActivity, "商家信息不存在", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // 生成哈希密码
            val hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray())

            // 更新数据库（在 IO 线程执行）
            withContext(Dispatchers.IO) {
                businessDao.update(business.copy(password = hashedPassword))
            }

            // 提示成功并关闭页面
            withContext(Dispatchers.Main) {
                setResult(Activity.RESULT_OK) // 设置结果码
                Toast.makeText(this@ManageManUpdatePwdActivity, "密码修改成功", Toast.LENGTH_SHORT).show()
                finish() // 关闭 Activity
            }
        }
    }

    // 密码强度校验（与注册逻辑一致）
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isDigit() } &&
                password.any { it.isLetter() } &&
                password.any { !it.isLetterOrDigit() }
    }
}