package com.example.petopia.data.repository

import com.example.petopia.dao.UserDao
import com.example.petopia.data.model.User

class UserRepository(private val userDao: UserDao) {
    // In a real app, this would also interact with Firebase/Remote API
    
    suspend fun signup(user: User) {
        userDao.registerUser(user)
    }

    suspend fun getUser(id: String): User? {
        return userDao.getUserById(id)
    }
}
