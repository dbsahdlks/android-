package com.example.orderfood.activity.man

import kotlinx.coroutines.flow.first
import android.os.Bundle
import android.view.LayoutInflater

import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.orderfood.R
import com.example.orderfood.dao.CommentDao
import com.example.orderfood.dao.UserDao
import com.example.orderfood.entity.Comment
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.activity.man.adapter.CommentAdapter
import com.example.orderfood.dao.ReplyDao
import com.example.orderfood.entity.Reply
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManageManCommentActivity : AppCompatActivity() {

    private lateinit var commentRecyclerview: RecyclerView
    private lateinit var adapter: CommentAdapter
    private lateinit var userDao: UserDao
    private lateinit var commentDao: CommentDao
    private lateinit var replyDao: ReplyDao
    private var businessId: String = ""
    private var shouldRefreshOnResume = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_man_comment)

        // 获取商家ID
        businessId = intent.getStringExtra("business_id") ?: ""
        if (businessId.isEmpty()) {
            showToast("未获取到商家ID")
            finish()
            return
        }

        // 初始化DAO
        userDao = AppDatabase.getDatabase(this).userDao()
        commentDao =AppDatabase.getDatabase(this).commentDao()
        replyDao =AppDatabase.getDatabase(this).replyDao()
        // 初始化UI
        initViews()

        // 加载评论数据
        loadComments()
    }

    private fun initViews() {
        commentRecyclerview= findViewById(R.id.man_manage_comment_recyclerView)

        // 创建适配器
        adapter = CommentAdapter(
            context = this,
            userDao = userDao,
        )
        commentRecyclerview.adapter = adapter


        commentRecyclerview.adapter = adapter
        adapter.setOnItemClickListener { comment ->
            showCommentDetail(comment)
        }
    }

    private fun loadComments() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 获取商家评论
                val comments = commentDao.getCommentsByBusinessId(businessId).first()

                // 在 loadComments 方法中
                withContext(Dispatchers.Main) {
                    adapter.submitList(comments) // 替换 adapter.setCommentList(comments)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("加载评论失败: ${e.message}")
                }
            }
        }
    }

    private fun deleteComment(position: Int, commentId: Long) {
        AlertDialog.Builder(this)
            .setTitle("删除评论")
            .setMessage("确定要删除这条评论吗？")
            .setPositiveButton("确定") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        commentDao.deleteCommentById(commentId)

                        withContext(Dispatchers.Main) {
                            // 优化：直接从适配器中移除项目，避免重新加载全部数据
                            val currentList = adapter.currentList.toMutableList()
                            currentList.removeIf { it.commentId == commentId }
                            adapter.submitList(currentList)

                            showToast("评论已删除")
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showToast("删除失败: ${e.message}")
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }


    private suspend fun saveReply(commentId: Long, content: String) {
        withContext(Dispatchers.IO) {
            try {
                // 创建回复对象
                val reply = Reply(
                    commentId = commentId,
                    content = content
                )

                val replyId = replyDao.insertReply(reply)

                withContext(Dispatchers.Main) {
                    showToast("回复成功")

                    // 刷新评论详情或列表（示例：重新加载评论）
                    loadComments() // 假设存在加载评论的方法
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("回复失败: ${e.message}")
                }
            }
        }
    }


    private fun formatCommentTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        return dateFormat.format(Date(timestamp))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    override fun onResume() {
        super.onResume()
        // 当从后台返回时刷新数据
        if (shouldRefreshOnResume) {
            loadComments()
            shouldRefreshOnResume = false
        }
    }
    override fun onPause() {
        super.onPause()
        // 标记需要刷新（避免立即刷新不可见界面）
        shouldRefreshOnResume = true

        // 关闭所有弹窗（防止内存泄漏）
        dismissAllDialogs()
    }
    private fun dismissAllDialogs() {
        if (::commentDetailDialog.isInitialized && commentDetailDialog.isShowing) {
            commentDetailDialog.dismiss()
        }
        // 添加其他需要管理的弹窗
    }
    private lateinit var commentDetailDialog: AlertDialog

    private fun showCommentDetail(comment: Comment) {
        commentDetailDialog.show()
    }
}