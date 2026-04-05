package com.example.petopia.data.remote

import android.util.Log
import com.example.petopia.base.Constants
import com.example.petopia.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseAuthModel {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun signup(email: String, pass: String): String? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            result.user?.uid
        } catch (e: Exception) {
            Log.e("FirebaseAuthModel", "Signup failed", e)
            throw e
        }
    }

    suspend fun login(email: String, pass: String): String? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            result.user?.uid
        } catch (e: Exception) {
            Log.e("FirebaseAuthModel", "Login failed", e)
            throw e
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun addUser(user: User) {
        try {
            db.collection(Constants.USERS_COLLECTION)
                .document(user.id)
                .set(user)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseAuthModel", "Error adding user", e)
            throw e
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            val document = db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthModel", "Error getting user", e)
            null
        }
    }
}
