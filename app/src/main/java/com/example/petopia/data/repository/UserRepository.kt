package com.example.petopia.data.repository

import com.example.petopia.dao.UserDao
import com.example.petopia.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDao) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    suspend fun signup(user: User, pass: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(user.email, pass).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Signup failed"))
            
            val newUser = user.copy(id = firebaseUser.uid)
            
            // Save to Firestore
            usersCollection.document(newUser.id).set(newUser).await()
            
            // Save to Local DB
            userDao.registerUser(newUser)
            
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Login failed"))
            
            var localUser = userDao.getUserById(firebaseUser.uid)
            
            if (localUser == null) {
                // Fetch from Firestore if not in Local DB
                val document = usersCollection.document(firebaseUser.uid).get().await()
                localUser = document.toObject(User::class.java)
                
                if (localUser != null) {
                    // Cache in local DB
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
        // Try local first
        val localUser = userDao.getUserById(id)
        if (localUser != null) return localUser
        
        // Then try remote
        return try {
            val document = usersCollection.document(id).get().await()
            val remoteUser = document.toObject(User::class.java)
            if (remoteUser != null) {
                userDao.registerUser(remoteUser)
            }
            remoteUser
        } catch (e: Exception) {
            null
        }
    }
}
