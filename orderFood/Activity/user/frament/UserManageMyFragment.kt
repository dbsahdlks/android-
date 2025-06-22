package com.example.orderfood.activity.user.frament

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.orderfood.MainActivity
import com.example.orderfood.R
import com.example.orderfood.activity.man.frament.ManageMyFragment
import com.example.orderfood.activity.user.UserManageManCommentActivity
import com.example.orderfood.activity.user.UserManageManOrderNotFinishActivity
import com.example.orderfood.activity.user.UserManageManUpdateMesActivity
import com.example.orderfood.activity.user.UserManageManUpdatePwdActivity
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.entity.User
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.example.orderfood.viewModel.UserViewModel
import com.example.orderfood.viewModel.UserViewModelFactory

class UserManageMyFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var avatarImageView: ImageView
    private lateinit var accountTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var desTextView: TextView

    private val userId: String by lazy {
        arguments?.getString("user_id") ?: error("userId is required")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(requireContext())
        // 通过工厂创建 ViewModel
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(database)
        ).get(UserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_manage_my, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化视图组件
        avatarImageView = view.findViewById(R.id.user_manage_my_tx)
        accountTextView = view.findViewById(R.id.user_manage_my_account)
        nameTextView = view.findViewById(R.id.user_manage_my_name)
        desTextView = view.findViewById(R.id.user_manage_my_des)

        userViewModel.loadUser(userId)
        // 观察数据变化并更新 UI
        lifecycleScope.launch {
            userViewModel.user.collect { user ->
                user?.let { updateUI(it) }
            }
        }
        // 初始化修改信息的 TextView
        val changeMesTextView: TextView = view.findViewById(R.id.user_manage_my_changeMes)
        changeMesTextView.setOnClickListener {
            launchEditActivity()
        }
        val changePwdTextView: TextView = view.findViewById(R.id.user_manage_my_changePwd)
        changePwdTextView.setOnClickListener {
            launchChangePasswordActivity() // 调用跳转方法
        }
        // 注销账号点击事件
        val zhuXiaoTextView: TextView = view.findViewById(R.id.user_manage_my_zx)
        zhuXiaoTextView.setOnClickListener { showLogoutDialog() }

        // 退出账号点击事件
        val exitTextView: TextView = view.findViewById(R.id.user_manage_my_exit)
        exitTextView.setOnClickListener { exitAccount() }
        view.findViewById<Button>(R.id.user_order_management_button).setOnClickListener {
            openOrderManagementActivity()
        }
        view.findViewById<Button>(R.id.user_comment_manage_btn).setOnClickListener {
            openCommentManagementActivity()
        }
    }
    private fun openCommentManagementActivity() {
        val intent = Intent(requireContext(), UserManageManCommentActivity::class.java).apply {
            putExtra("user_id", userId) // 添加商家ID参数
        }
        startActivity(intent)
    }
    private fun openOrderManagementActivity() {
        val intent = Intent(requireContext(), UserManageManOrderNotFinishActivity::class.java).apply {
            putExtra("user_id", userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // 清除返回栈中的其他实例
        }
        startActivity(intent)
    }
    private val editResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            userViewModel.loadUser(userId) // 重新加载数据
        }
    }
    private fun launchEditActivity() {

        val intent = Intent(requireContext(), UserManageManUpdateMesActivity::class.java)
        intent.putExtra("user_id", userId) // 传递商家 ID 到编辑界面
        editResultLauncher.launch(intent)
    }
    private val passwordUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 刷新商家信息（如重新加载数据）
            userViewModel.loadUser(userId)
        }
    }

    // 修改跳转方法为使用发射器：
    private fun launchChangePasswordActivity() {
        val intent = Intent(requireContext(), UserManageManUpdatePwdActivity::class.java)
        intent.putExtra("user_id", userId)
        passwordUpdateLauncher.launch(intent) // 使用发射器启动 Activity
    }
    private fun updateUI(user: User) {
        // 更新账号
        accountTextView.text = user.userId

        // 更新名称
        nameTextView.text = user.name

        // 更新描述
        desTextView.text = "店铺简介：${user.phone}"

        // 更新头像
        if (user.imagePath.isNotEmpty()) {
            // 构建完整的文件路径
            val imageFile = File(requireContext().filesDir, user.imagePath)

            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile) // 直接加载File对象
                    .placeholder(R.drawable.busin)
                    .error(R.drawable.busin)
                    .into(avatarImageView)
            } else {
                avatarImageView.setImageResource(R.drawable.login)
            }
        } else {
            avatarImageView.setImageResource(R.drawable.login)
        }
    }
    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout_confirm, null)
        val tilPassword = dialogView.findViewById<TextInputLayout>(R.id.til_logout_password)
        val etPassword = dialogView.findViewById<EditText>(R.id.et_logout_password)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("注销账号")
            .setView(dialogView)
            .setPositiveButton("确认", null)
            .setNegativeButton("取消", null)
            .create()

        dialog.setOnShowListener {
            val confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirmButton.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val password = etPassword.text.toString().trim()
                        if (password.isEmpty()) {
                            tilPassword.error = "请输入密码"
                            etPassword.requestFocus()
                            return@launch
                        }

                        val isPasswordValid = withContext(Dispatchers.IO) {
                            userViewModel.checkPassword(userId, password)
                        }

                        if (!isPasswordValid) {
                            withContext(Dispatchers.Main) {
                                tilPassword.error = "密码错误"
                                etPassword.setText("")
                                etPassword.requestFocus()
                            }
                            return@launch
                        }

                        // 执行删除逻辑
                        withContext(Dispatchers.IO) {
                            val user = userViewModel.getUserById(userId)
                            user?.let {
                                val imageFile = File(requireContext().filesDir, it.imagePath)
                                imageFile.delete() // 删除头像文件
                                userViewModel.deleteUser(userId) // 删除数据库记录
                            }
                        }

                        // 跳转并清理栈
                        withContext(Dispatchers.Main) {
                            dialog.dismiss() // 先关闭对话框
                            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            requireActivity().finishAffinity()
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "注销失败：${e.message}", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    private suspend fun validatePassword(
        tilPassword: TextInputLayout,
        etPassword: EditText,
        dialog: AlertDialog
    ) {
        val password = etPassword.text.toString().trim()

        if (password.isEmpty()) {
            tilPassword.error = "请输入密码"
            etPassword.requestFocus()
            return
        }

        try {
            // 验证密码
            val isPasswordValid = withContext(Dispatchers.IO) {
                userViewModel.checkPassword(userId, password)
            }

            if (!isPasswordValid) {
                withContext(Dispatchers.Main) {
                    tilPassword.error = "密码错误"
                    etPassword.setText("")
                    etPassword.requestFocus()
                }
                return
            }

            // 执行注销逻辑（删除数据）
            withContext(Dispatchers.IO) {
                val user = userViewModel.getUserById(userId)
                user?.let {
                    deleteUserData(it) // 删除头像文件
                    userViewModel.deleteUser(userId) // 删除数据库记录
                }
            }

            // 跳转至 MainActivity 并清理所有任务栈
            withContext(Dispatchers.Main) {
                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 关键：清理栈
                }
                startActivity(intent)
                requireActivity().finishAffinity() // 结束当前所有关联 Activity
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "注销失败：${e.message}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }
    // 删除商家数据（包括头像文件）
    private suspend fun deleteUserData(user: User) {
        if (user.imagePath.isNotEmpty()) {
            val imageFile = File(requireContext().filesDir, user.imagePath)
            imageFile.delete()
        }
    }
    private fun exitAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("退出账号")
            .setMessage("确定要退出当前账号吗？")
            .setPositiveButton("确认") { _, _ ->
                // 将逻辑移至确认按钮回调内
                val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()

                // 跳转到登录页而非 MainActivity
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    companion object {
        @JvmStatic
        fun newInstance(userId: String) =
            ManageMyFragment().apply {
                arguments = Bundle().apply {
                    putString("user_id", userId)
                }
            }
    }
}