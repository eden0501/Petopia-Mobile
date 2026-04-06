package com.example.petopia.data.repository

import com.example.petopia.data.local.dao.UserDao
import com.example.petopia.data.model.User
import com.example.petopia.data.remote.FirebaseAuthModel

class UserRepository(private val userDao: UserDao) {

    suspend fun signup(user: User, pass: String): Result<User> {
        return try {
            val uid = FirebaseAuthModel.signup(user.email, pass)
                ?: return Result.failure(Exception("Signup failed - UID is null"))

            val newUser = user.copy(id = uid)
            FirebaseAuthModel.addUser(newUser)
            userDao.registerUser(newUser)

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val uid = FirebaseAuthModel.login(email, pass)
                ?: return Result.failure(Exception("Login failed - UID is null"))

            var localUser = userDao.getUserById(uid)

            if (localUser == null) {
                localUser = FirebaseAuthModel.getUser(uid)
                if (localUser != null) {
                    userDao.registerUser(localUser)
                }
            }

            if (localUser != null) {
                Result.success(localUser)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(id: String): User? {
        val localUser = userDao.getUserById(id)
        if (localUser != null) return localUser

        return try {
            val remoteUser = FirebaseAuthModel.getUser(id)
            if (remoteUser != null) {
                userDao.registerUser(remoteUser)
            }
            remoteUser
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(): String? = FirebaseAuthModel.getCurrentUserId()

    suspend fun getCurrentUser(): User? {
        val userId = getCurrentUserId() ?: return null
        return getUser(userId)
    }

    fun logout() {
        FirebaseAuthModel.logout()
    }

    suspend fun updateUser(user: User): Result<User> {
        return try {
            FirebaseAuthModel.addUser(user)
            userDao.registerUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            FirebaseAuthModel.deleteUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

