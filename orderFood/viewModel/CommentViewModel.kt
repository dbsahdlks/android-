package com.example.orderfood.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.orderfood.dao.AppDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.example.orderfood.entity.Comment

class CommentViewModel(application: Application) : AndroidViewModel(application) {

    val businessComments: MutableLiveData<List<Comment>> = MutableLiveData()
    val deleteSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    init {
        val commentDao = AppDatabase.getDatabase(application).commentDao()

    }

    // 明确使用String类型的businessId
    fun loadBusinessComments(businessId: String) {
        viewModelScope.launch {
            try {
                // 确保Repository方法返回Flow<List<Comment>>

            } catch (e: Exception) {
                errorMessage.postValue("加载评论失败: ${e.message}")
            }
        }
    }

    // 添加新评论
    fun addNewComment(comment: Comment) {
        viewModelScope.launch {
            try {

                // 刷新评论列表，使用Comment中的businessId（应为String类型）
                loadBusinessComments(comment.businessId)
            } catch (e: Exception) {
                errorMessage.postValue("添加评论失败: ${e.message}")
            }
        }
    }

    // 删除评论
    fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            try {

                deleteSuccess.postValue(true)
                // 刷新评论列表
                loadBusinessComments(comment.businessId)
            } catch (e: Exception) {
                deleteSuccess.postValue(false)
                errorMessage.postValue("删除评论失败: ${e.message}")
            }
        }
    }
}


