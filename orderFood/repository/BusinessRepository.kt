package com.example.orderfood.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.orderfood.dao.BusinessDao
import com.example.orderfood.entity.Business
import javax.inject.Inject

class BusinessRepository @Inject constructor(
    private val businessDao: BusinessDao
) {
    // 使用安全哈希验证登录
    suspend fun authenticate(businessId: String, password: String): Business? {
        val business = businessDao.getBusinessById(businessId)
        return if (business != null && verifyPassword(password, business.password)) {
            business
        } else {
            null
        }
    }

    // 插入商家时自动哈希密码
    suspend fun insertBusiness(business: Business, rawPassword: String) {
        val hashedBusiness = business.copy(
            password = hashPassword(rawPassword)
        )
        businessDao.insert(hashedBusiness)
    }

    // 仅用于注册前检查
    suspend fun businessIdExists(businessId: String): Boolean {
        return businessDao.businessIdExists(businessId) > 0
    }

    // 密码哈希方法
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    // 密码验证方法
    private fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        return BCrypt.verifyer().verify(inputPassword.toCharArray(), storedHash).verified
    }

    // 添加获取商家信息的方法
    suspend fun getBusinessById(businessId: String): Business? {
        return businessDao.getBusinessById(businessId)
    }
}