package com.example.orderfood.activity.man

import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.orderfood.R
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.util.FileImgUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import android.Manifest
import android.os.Environment

class ManageManUpdateMesActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var avatarImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var desEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var updateButton: Button

    private var currentImageUri: Uri? = null
    private var originalImagePath: String? = null
    private var currentBusinessId: String = ""
    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentImageUri = it
            Glide.with(this).load(uri).into(avatarImageView)
        }
    }

    // 相机拍照
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentImageUri != null) {
            Glide.with(this).load(currentImageUri).into(avatarImageView)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_man_update_mes)
        // 初始化视图
        initViews()

        // 初始化 Toolbar
        initToolbar()

        // 加载商家当前信息
        loadBusinessInfo()

        // 设置头像点击事件（支持相册和相机）
        setupAvatarClick()
        updateButton.setOnClickListener { updateBusinessInfo() }
    }
    private fun initViews() {
        toolbar = findViewById(R.id.man_manage_updateMes_toolbar)
        avatarImageView = findViewById(R.id.man_manage_updateMes_tx)
        nameEditText = findViewById(R.id.man_manage_updateMes_name)
        desEditText = findViewById(R.id.man_manage_updateMes_des)
        typeEditText = findViewById(R.id.man_manage_updateMes_type)
        updateButton = findViewById(R.id.man_manage_updateMes_btn)
    }
    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
    private fun loadBusinessInfo() {
        // 从 Intent 获取商家 ID（假设从登录状态或上个页面传递）
        currentBusinessId = intent.getStringExtra("business_id") ?: "root"

        lifecycleScope.launch(Dispatchers.IO) {
            val business = AppDatabase.getDatabase(this@ManageManUpdateMesActivity)
                .businessDao()
                .getBusinessById(currentBusinessId)

            withContext(Dispatchers.Main) {
                business?.let {
                    nameEditText.setText(it.name)
                    desEditText.setText(it.description)
                    typeEditText.setText(it.type)
                    originalImagePath = it.imagePath

                    // 加载头像
                    if (!it.imagePath.isNullOrEmpty()) {
                        val file = File(filesDir, it.imagePath)
                        if (file.exists()) {
                            Glide.with(this@ManageManUpdateMesActivity)
                                .load(file)
                                .into(avatarImageView)
                        } else {
                            avatarImageView.setImageResource(R.drawable.upimg)
                        }
                    }
                }
            }
        }
    }
    private fun setupAvatarClick() {
        avatarImageView.setOnClickListener {
            checkCameraAndStoragePermissions() // 先检查权限
        }
    }
    private fun showImagePickerDialog() {
        val options = arrayOf("从相册选择", "拍照")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("选择头像来源")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*") // 相册
                    1 -> takePhoto() // 拍照
                }
            }
            .show()
    }
    private fun takePhoto() {
        // 直接使用 MediaStore.Images.Media 的静态字段
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "business_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            // 指定图片保存路径（内部存储的 App 专属目录）
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/OrderFood/Avatars")
        }

        currentImageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        currentImageUri?.let { uri ->
            takePhotoLauncher.launch(uri)
        } ?: run {
            Toast.makeText(this, "无法获取相机资源", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkCameraAndStoragePermissions() {
        val requiredPermissions = mutableListOf<String>().apply {
            add(Manifest.permission.CAMERA) // 相机权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES) // Android 13+ 媒体权限
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE) // Android 12- 存储权限
            }
        }.toTypedArray()

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest, REQUEST_CODE_PERMISSION)
        } else {
            showImagePickerDialog()
        }
    }

    private val REQUEST_CODE_PERMISSION = 100
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            // 检查所有权限是否都被授予
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allPermissionsGranted) {
                showImagePickerDialog()
            } else {
                Toast.makeText(this, "需要相机和存储权限才能使用此功能", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateBusinessInfo() {
        val name = nameEditText.text.toString().trim()
        val description = desEditText.text.toString().trim()
        val type = typeEditText.text.toString().trim()

        if (name.isEmpty() || description.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            var newImagePath: String? = null

            // 处理新头像
            if (currentImageUri != null) {
                val fileName = FileImgUntil.getImgName()
                val saveSuccess = withContext(Dispatchers.IO) {
                    FileImgUntil.saveImageUriToFileimg(currentImageUri!!, this@ManageManUpdateMesActivity, fileName)
                }
                if (!saveSuccess) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ManageManUpdateMesActivity, "保存图片失败", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                newImagePath = fileName
            } else {
                newImagePath = originalImagePath // 使用原图片路径
            }
            val originalBusiness = AppDatabase.getDatabase(this@ManageManUpdateMesActivity)
                .businessDao()
                .getBusinessById(currentBusinessId) ?: return@launch
            // 创建更新后的 Business 对象
            val updatedBusiness = originalBusiness.copy(
                name = name,
                description = description,
                type = type,
                imagePath = newImagePath ?: originalBusiness.imagePath
            )

            // 更新数据库
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@ManageManUpdateMesActivity)
                    .businessDao()
                    .update(updatedBusiness)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ManageManUpdateMesActivity, "信息更新成功", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}