package com.example.orderfood.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.orderfood.dao.UserDao
import com.example.orderfood.entity.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    // 使用安全哈希验证用户登录
    suspend fun authenticate(userId: String, password: String): User? {
        val user = userDao.getUserById(userId)
        return if (user != null && verifyPassword(password, user.password)) {
            user
        } else {
            null
        }
    }

    // 插入用户时自动哈希密码
    suspend fun registerUser(user: User, rawPassword: String) {
        val hashedUser = user.copy(
            password = hashPassword(rawPassword)
        )
        userDao.insert(hashedUser) // 修正为 userDao
    }

    // 检查用户ID是否已存在
    suspend fun userIdExists(userId: String): Boolean {
        return userDao.userIdExists(userId) > 0
    }

    // 密码哈希方法
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    // 密码验证方法
    private fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        return BCrypt.verifyer().verify(inputPassword.toCharArray(), storedHash).verified
    }

    // 获取用户信息
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }
}