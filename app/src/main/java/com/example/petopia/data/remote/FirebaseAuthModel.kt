package com.example.petopia.data.remote

import android.util.Log
import com.example.petopia.base.Constants
import com.example.petopia.data.model.User
import com.example.petopia.data.model.Post
import com.example.petopia.data.model.Comment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseAuthModel {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun signup(email: String, pass: String): String? {
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        return result.user?.uid
    }

    suspend fun login(email: String, pass: String): String? {
        val result = auth.signInWithEmailAndPassword(email, pass).await()
        return result.user?.uid
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun addUser(user: User) {
        db.collection(Constants.Collections.USERS)
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun deleteUser(password: String) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        val uid = user.uid

        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential).await()

        val posts = db.collection(Constants.Collections.POSTS)
            .whereEqualTo(Post.AUTHOR_ID_KEY, uid)
            .get().await()
        for (doc in posts.documents) {
            doc.reference.delete().await()
        }

        val comments = db.collection(Constants.Collections.COMMENTS)
            .whereEqualTo(Comment.AUTHOR_ID_KEY, uid)
            .get().await()
        for (doc in comments.documents) {
            doc.reference.delete().await()
        }

        val likedPosts = db.collection(Constants.Collections.POSTS)
            .whereArrayContains(Post.LIKES_KEY, uid)
            .get().await()
        for (doc in likedPosts.documents) {
            doc.reference.update(Post.LIKES_KEY, FieldValue.arrayRemove(uid)).await()
        }

        db.collection(Constants.Collections.USERS).document(uid).delete().await()

        user.delete().await()
    }

    suspend fun getUser(userId: String): User? {
        val document = db.collection(Constants.Collections.USERS)
            .document(userId)
            .get()
            .await()
        return if (document.exists()) {
            document.toObject(User::class.java)
        } else {
            null
        }
    }
}
