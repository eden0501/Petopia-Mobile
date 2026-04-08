package com.example.petopia.data.remote

import android.util.Log
import com.example.petopia.base.Constants
import com.example.petopia.data.model.Comment
import com.example.petopia.data.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseModel {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getAllPosts(since: Long): List<Post> {
        return try {
            val result = db.collection(Constants.POSTS_COLLECTION)
                .whereGreaterThanOrEqualTo(Post.LAST_UPDATED_KEY, Timestamp(since / 1000, 0))
                .get()
                .await()
            result.map { Post.fromJson(it.data) }
        } catch (e: Exception) {
            Log.e("FirebaseModel", "Error getting documents.", e)
            emptyList()
        }
    }

    suspend fun addPost(post: Post) {
        try {
            db.collection(Constants.POSTS_COLLECTION)
                .document(post.id)
                .set(post.toJson)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseModel", "Error adding document", e)
        }
    }
    
    suspend fun getAllComments(postId: String): List<Comment> {
        return try {
            val task = db.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("postId", postId)
                .get()
                .await()
            val comments = mutableListOf<Comment>()
            for (document in task.documents) {
                try {
                    val comment = document.toObject(Comment::class.java)
                    if (comment != null) comments.add(comment)
                } catch (e: Exception) {
                    Log.e("FirebaseModel", "Error parsing comment", e)
                }
            }
            comments
        } catch (e: Exception) {
            Log.e("FirebaseModel", "Error getting comments.", e)
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment) {
        try {
            db.collection(Constants.COMMENTS_COLLECTION).document(comment.id)
                .set(comment)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseModel", "Error adding comment", e)
        }
    }

    // Soft delete approach
    suspend fun deletePost(post: Post) {
        try {
            db.collection(Constants.POSTS_COLLECTION)
                .document(post.id)
                .update(Post.IS_DELETED_KEY, true, Post.LAST_UPDATED_KEY, FieldValue.serverTimestamp())
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseModel", "Error deleting document", e)
        }
    }
}
