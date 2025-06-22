package com.example.orderfood.util

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verifyPassword(rawPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(rawPassword, hashedPassword)
    }
}