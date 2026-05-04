package com.example.petopia.data.remote

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
        val result = db.collection(Constants.Collections.POSTS)
            .whereGreaterThanOrEqualTo(Post.LAST_UPDATED_KEY, Timestamp(since / 1000, 0))
            .get()
            .await()
        return result.map { Post.fromJson(it.data) }
    }

    suspend fun addPost(post: Post) {
        db.collection(Constants.Collections.POSTS)
            .document(post.id)
            .set(post.toJson)
            .await()
    }
    
    suspend fun getAllComments(postId: String): List<Comment> {
        val task = db.collection(Constants.Collections.COMMENTS)
            .whereEqualTo(Comment.POST_ID_KEY, postId)
            .get()
            .await()
        return task.documents.mapNotNull { it.data?.let { data -> Comment.fromJson(data) } }
    }

    suspend fun addComment(comment: Comment) {
        db.collection(Constants.Collections.COMMENTS).document(comment.id)
            .set(comment.toJson)
            .await()
    }

    suspend fun updatePostLikes(postId: String, userId: String, isLiked: Boolean) {
        val docRef = db.collection(Constants.Collections.POSTS).document(postId)
        val update = if (isLiked) {
            FieldValue.arrayUnion(userId)
        } else {
            FieldValue.arrayRemove(userId)
        }
        docRef.update(Post.LIKES_KEY, update, Post.LAST_UPDATED_KEY, FieldValue.serverTimestamp()).await()
    }

    suspend fun deletePost(post: Post) {
        db.collection(Constants.Collections.POSTS)
            .document(post.id)
            .update(Post.IS_DELETED_KEY, true, Post.LAST_UPDATED_KEY, FieldValue.serverTimestamp())
            .await()
    }

    suspend fun getPostsByAuthor(authorId: String): List<Post> {
        val result = db.collection(Constants.Collections.POSTS)
            .whereEqualTo(Post.AUTHOR_ID_KEY, authorId)
            .get()
            .await()
        return result.map { Post.fromJson(it.data) }
    }
}
