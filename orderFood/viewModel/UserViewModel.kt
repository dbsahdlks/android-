package com.example.orderfood.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val database: AppDatabase) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    // 加载用户信息
    fun loadUser(userId: String) {
        viewModelScope.launch {
            val user = database.userDao().getUserById(userId)
            _user.value = user
        }
    }

    suspend fun getUserById(userId: String): User? {
        return database.userDao().getUserById(userId)
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.userDao().deleteByUserId(userId)
        }
    }

    suspend fun checkPassword(userId: String, inputPassword: String): Boolean {
        val user = getUserById(userId) ?: return false
        return BCrypt.verifyer().verify(inputPassword.toCharArray(), user.password).verified
    }
}