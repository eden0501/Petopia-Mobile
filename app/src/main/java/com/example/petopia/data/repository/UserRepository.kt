package com.example.petopia.data.repository

import android.util.Log
import com.example.petopia.dao.UserDao
import com.example.petopia.data.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDao) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "UserRepository"

    suspend fun signup(email: String, pass: String, username: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    username = username
                )
                userDao.registerUser(user)
                Log.d(TAG, "Signup success: ${firebaseUser.uid}")
                true
            } else {
                Log.e(TAG, "Signup failed: firebaseUser is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Signup error", e)
            false
        }
    }

    suspend fun login(email: String, pass: String): User? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val localUser = userDao.getUserById(firebaseUser.uid)
                if (localUser == null) {
                    // Fallback if local DB doesn't have the user yet
                    Log.w(TAG, "Login success but user not in local DB: ${firebaseUser.uid}")
                    User(id = firebaseUser.uid, email = firebaseUser.email ?: email, username = "")
                } else {
                    Log.d(TAG, "Login success: ${localUser.id}")
                    localUser
                }
            } else {
                Log.e(TAG, "Login failed: firebaseUser is null")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}
