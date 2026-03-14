package com.example.petopia.data.repository

import android.util.Log
import com.example.petopia.dao.UserDao
import com.example.petopia.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDao) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
                // 1. Save to Remote Firestore
                db.collection("users").document(user.id).set(user.toJson()).await()
                
                // 2. Cache to Local Room
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
                // 1. Check Remote Firestore
                val snapshot = db.collection("users").document(firebaseUser.uid).get().await()
                val userData = snapshot.data
                
                if (userData != null) {
                    val user = User.fromJson(userData)
                    // 2. Cache/Update Local Room
                    userDao.registerUser(user)
                    Log.d(TAG, "Login success: ${user.id}")
                    user
                } else {
                    Log.e(TAG, "Login success but user data missing in Firestore: ${firebaseUser.uid}")
                    null
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
