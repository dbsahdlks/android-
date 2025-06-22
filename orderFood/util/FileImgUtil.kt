package com.example.orderfood.util
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

// FileImgUntil.kt
object FileImgUntil {
    // 生成唯一的图片文件名
    fun getImgName(): String = "img_${System.currentTimeMillis()}.jpg"

    // 保存图片到应用私有目录
    fun saveImageUriToFileimg(uri: Uri, context: Context, fileName: String): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val outputFile = File(context.filesDir, fileName)
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 从文件加载图片
    fun loadImage(context: Context, fileName: String): Bitmap? {
        return try {
            val file = File(context.filesDir, fileName)
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }
}