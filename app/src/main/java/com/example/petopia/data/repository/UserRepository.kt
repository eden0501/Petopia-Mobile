package com.example.petopia.data.repository

import com.example.petopia.dao.UserDao
import com.example.petopia.data.model.User

class UserRepository(private val userDao: UserDao) {
    suspend fun signup(user: User): Boolean {
        return try {
            userDao.registerUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(username: String, passwordHash: String): User? {
        val user = userDao.getUserByUsername(username)
        return if (user?.passwordHashed == passwordHash) user else null
    }
}