package com.example.petopia.data.repository

import com.example.petopia.dao.UserDao
import com.example.petopia.data.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDao) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(email: String, pass: String): User? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // In a real app, you might want to fetch the full user profile from Firestore or local DB
                // For now, we'll try to get it from local Room DB
                userDao.getUserById(firebaseUser.uid)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}