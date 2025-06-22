package com.example.orderfood.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KClass

object PreferenceHelper {
    // 定义所有可能的键
    const val BUSINESS_ID = "business_id"
    const val USER_ID = "user_id"
    const val USER_NAME = "user_name"
    const val USER_AVATAR = "user_avatar"
    const val LOGIN_TOKEN = "login_token"
    const val REMEMBER_ME = "remember_me"
    const val LAST_LOGIN = "last_login"
    const val APP_THEME = "app_theme"

    // 安全的获取值（带默认值） - 修复类型处理
    inline fun <reified T> get(context: Context, key: String, defaultValue: T): T {
        val prefs = context.getSharedPreferences("order_food_prefs", Context.MODE_PRIVATE)
        return when (T::class) {
            String::class -> prefs.getString(key, defaultValue as? String) as? T ?: defaultValue
            Int::class -> prefs.getInt(key, defaultValue as? Int ?: 0) as T
            Boolean::class -> prefs.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> prefs.getFloat(key, defaultValue as? Float ?: 0f) as T
            Long::class -> prefs.getLong(key, defaultValue as? Long ?: 0L) as T
            else -> defaultValue
        }
    }

    // 设置值 - 修复类型处理
    fun <T> set(context: Context, key: String, value: T) {
        val prefs = context.getSharedPreferences("order_food_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> {
                val typeName = value?.let { it::class.simpleName } ?: "null"
                throw IllegalArgumentException("Unsupported type: $typeName")
            }
        }
        editor.apply()
    }

    // 检查是否包含某个键
    fun contains(context: Context, key: String): Boolean {
        return context.getSharedPreferences("order_food_prefs", Context.MODE_PRIVATE).contains(key)
    }

    // 删除某个键值对
    fun remove(context: Context, key: String) {
        val prefs = context.getSharedPreferences("order_food_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove(key).apply()
    }

    // 清空所有数据
    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences("order_food_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    // 业务特定的快捷方法
    fun getBusinessId(context: Context): Long {
        return get(context, BUSINESS_ID, 0L)
    }

    fun setBusinessId(context: Context, businessId: Long) {
        set(context, BUSINESS_ID, businessId)
    }

    fun getUserId(context: Context): Long {
        return get(context, USER_ID, 0L)
    }

    fun setUserId(context: Context, userId: Long) {
        set(context, USER_ID, userId)
    }

    fun getUserName(context: Context): String {
        return get(context, USER_NAME, "")
    }

    fun setUserName(context: Context, userName: String) {
        set(context, USER_NAME, userName)
    }

    // 获取和设置登录令牌
    fun getLoginToken(context: Context): String {
        return get(context, LOGIN_TOKEN, "")
    }

    fun setLoginToken(context: Context, token: String) {
        set(context, LOGIN_TOKEN, token)
    }

    // 获取和设置记住我状态
    fun getRememberMe(context: Context): Boolean {
        return get(context, REMEMBER_ME, false)
    }

    fun setRememberMe(context: Context, remember: Boolean) {
        set(context, REMEMBER_ME, remember)
    }

    // 获取和设置主题
    fun getAppTheme(context: Context): String {
        return get(context, APP_THEME, "system")
    }

    fun setAppTheme(context: Context, theme: String) {
        set(context, APP_THEME, theme)
    }

    // 新增：获取当前登录商家ID（安全方式）
    fun getCurrentBusinessId(context: Context): Long {
        return getBusinessId(context).takeIf { it != 0L } ?: run {
            // 处理未登录情况
            throw IllegalStateException("No business ID found. User not logged in?")
        }
    }
    // 在 PreferenceHelper 对象末尾添加：
    fun defaultPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("order_food_prefs", Context.MODE_PRIVATE)
    }

    // SharedPreferences 扩展函数
    operator fun <T> SharedPreferences.get(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> getString(key, defaultValue) as T
            is Int -> getInt(key, defaultValue) as T
            is Boolean -> getBoolean(key, defaultValue) as T
            is Float -> getFloat(key, defaultValue) as T
            is Long -> getLong(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}