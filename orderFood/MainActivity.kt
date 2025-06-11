package com.example.orderfood

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.orderfood.activity.man.ManageManActivity
import com.example.orderfood.activity.man.RegisterManActivity
import com.example.orderfood.activity.user.RegisterUserActivity
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.repository.BusinessRepository
import com.example.orderfood.repository.UserRepository
import com.example.orderfood.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 初始化Room数据库
        val db = AppDatabase.getDatabase(this)
        val businessRepository = BusinessRepository(db.businessDao())
        val userRepository = UserRepository(db.userDao())
        val sjRadio = findViewById<RadioButton>(R.id.login_sj)
        sjRadio.isChecked=true
        val zcsj = findViewById<Button>(R.id.login_zhuceshangjia)
        zcsj.setOnClickListener{
            val intent = Intent(this, RegisterManActivity::class.java)
            startActivity(intent);
        }
        val zcyh = findViewById<Button>(R.id.login_zhuceyonghu)
        zcyh.setOnClickListener{
            val intent = Intent(this, RegisterUserActivity::class.java)
            startActivity(intent);
        }

        val accountText = findViewById<EditText>(R.id.login_account)
        val passwordText = findViewById<EditText>(R.id.login_pwd)
        val denglu = findViewById<Button>(R.id.login_denglu)
        val sj = findViewById<RadioButton>(R.id.login_sj)
        val yh = findViewById<RadioButton>(R.id.login_yh)

        denglu.setOnClickListener{
            val account = accountText.text.toString()
            val pwd = passwordText.text.toString()
            if(account.isEmpty()){
                Toast.makeText(this,"请输入账号",Toast.LENGTH_SHORT).show()
            }
            if(pwd.isEmpty()) {
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = when {
                        sj.isChecked -> {
                            val business = db.businessDao().getBusinessById(account)
                            if (business != null && PasswordHasher.verifyPassword(pwd, business.password)) {
                                LoginResult.Success(business, "business")
                            } else {
                                LoginResult.Error("账号或密码错误")
                            }
                        }
                        yh.isChecked -> {
                            val user = db.userDao().getUserById(account)
                            if (user != null && PasswordHasher.verifyPassword(pwd, user.password)) {
                                LoginResult.Success(user, "user")
                            } else {
                                LoginResult.Error("账号或密码错误")
                            }
                        }
                        else -> LoginResult.Error("请选择登录角色")
                    }

                    withContext(Dispatchers.Main) {
                        when (result) {
                            is LoginResult.Success -> {
                                Toast.makeText(this@MainActivity, "${result.roleName}登录成功", Toast.LENGTH_SHORT).show()

                                // 保存登录状态
                                saveLoginState(this@MainActivity,account, result.roleType)

                                 //跳转到对应主页
                                val intent = when (result.roleType) {
                                    "business" -> {
                                            Intent(this@MainActivity, ManageManActivity::class.java)}
                                    //"user" -> Intent(this@MainActivity, UserHomeActivity::class.java)
                                    else -> null
                                }
                                intent?.let { startActivity(it) }
                                finish()
                            }
                            is LoginResult.Error -> {
                                Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "登录失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

    }
    private fun saveLoginState(context: Context, userId: String, role: String) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("user_id", userId)
            putString("user_role", role)
            apply() // 或 commit()，根据需求选择
        }
    }

    sealed class LoginResult {
        data class Success(val data: Any, val roleType: String) : LoginResult() {
            val roleName: String get() = if (roleType == "business") "商家" else "用户"
        }
        data class Error(val message: String) : LoginResult()
    }

}