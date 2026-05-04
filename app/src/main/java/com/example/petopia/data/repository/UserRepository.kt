package com.example.petopia.data.repository

import android.content.Context
import com.example.petopia.data.local.dao.UserDao
import com.example.petopia.data.local.dao.AppDatabase
import com.example.petopia.data.model.User
import com.example.petopia.data.remote.FirebaseAuthModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository private constructor(context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val database = AppDatabase.getDatabase(context)

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(context: Context): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(context).also { instance = it }
            }
        }
    }

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

    suspend fun getUser(id: String): Result<User?> {
        return try {
            val localUser = userDao.getUserById(id)
            if (localUser != null) return Result.success(localUser)

            val remoteUser = FirebaseAuthModel.getUser(id)
            if (remoteUser != null) {
                userDao.registerUser(remoteUser)
            }
            Result.success(remoteUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = FirebaseAuthModel.getCurrentUserId()

    suspend fun getCurrentUser(): Result<User?> {
        val userId = getCurrentUserId() ?: return Result.success(null)
        return getUser(userId)
    }

    suspend fun logout(): Result<Unit> {
        return try {
            FirebaseAuthModel.logout()
            withContext(Dispatchers.IO) {
                database?.clearAllTables()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            FirebaseAuthModel.deleteUser(password)
            withContext(Dispatchers.IO) {
                database?.clearAllTables()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

