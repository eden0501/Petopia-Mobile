package com.example.petopia.data.repository

import android.content.Context
import com.example.petopia.data.local.dao.AppLocalDB
import com.example.petopia.data.model.CommentPreview
import com.example.petopia.data.model.Comment
import com.example.petopia.data.model.Post
import com.example.petopia.data.model.PostDisplayItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.petopia.base.Constants
import com.example.petopia.data.remote.FirebaseModel
import com.example.petopia.data.remote.FirebaseAuthModel

class PostRepository private constructor(context: Context) {
    private val postDao = AppLocalDB.getDatabase(context).postDao()
    private val commentDao = AppLocalDB.getDatabase(context).commentDao()
    private val userDao = AppLocalDB.getDatabase(context).userDao()

    private val sharedPrefs = context.getSharedPreferences(Constants.SharedPrefs.PREFS_NAME, Context.MODE_PRIVATE)

    var lastUpdatedPosts: Long
        get() = sharedPrefs.getLong(Constants.SharedPrefs.LAST_UPDATED_POSTS, 0)
        set(value) = sharedPrefs.edit().putLong(Constants.SharedPrefs.LAST_UPDATED_POSTS, value).apply()

    companion object {
        @Volatile
        private var instance: PostRepository? = null

        fun getInstance(context: Context): PostRepository {
            return instance ?: synchronized(this) {
                instance ?: PostRepository(context).also { instance = it }
            }
        }
    }

    suspend fun getAllPostsWithPreviews(currentUserId: String?): List<PostDisplayItem> = withContext(Dispatchers.IO) {
        val posts = postDao.getAllPosts()
        posts.map { post ->
            val resolvedAuthorName = resolveAuthorName(post.authorId)
            val comments = commentDao.getCommentsByPostId(post.id)
            val previews = comments.map {
                val commentAuthor = resolveAuthorName(it.authorId)
                CommentPreview(commentAuthor, it.content, it.createdAt)
            }
            PostDisplayItem(
                post = post,
                authorName = resolvedAuthorName,
                commentCount = comments.size,
                previewComments = previews,
                isLiked = currentUserId != null && post.likes.contains(currentUserId),
                isCommentsVisible = false
            )
        }
    }

    suspend fun refreshAllPosts() = withContext(Dispatchers.IO) {
        val lastUpdated = lastUpdatedPosts
        val posts = FirebaseModel.getAllPosts(lastUpdated)
        var time = lastUpdated
        for (post in posts) {
            postDao.insertPosts(post)
            post.lastUpdated?.let { postLastUpdated ->
                if (time < postLastUpdated) {
                    time = postLastUpdated
                }
            }
        }
        lastUpdatedPosts = time
    }

    suspend fun insertPosts(posts: List<Post>) = withContext(Dispatchers.IO) {
        postDao.insertPosts(*posts.toTypedArray())
        posts.forEach { post ->
            FirebaseModel.addPost(post)
        }
    }

    suspend fun addComment(postId: String, authorId: String, authorName: String, content: String) = withContext(Dispatchers.IO) {
        val comment = Comment(
            id = System.currentTimeMillis().toString(),
            postId = postId,
            authorId = authorId,
            authorName = authorName,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        commentDao.insertComments(comment)
        FirebaseModel.addComment(comment)
    }

    suspend fun toggleLike(userId: String, postId: String) = withContext(Dispatchers.IO) {
        val post = postDao.getPostById(postId) ?: return@withContext
        val likes = post.likes.toMutableList()
        
        if (likes.contains(userId)) {
            likes.remove(userId)
        } else {
            likes.add(userId)
        }
        
        post.likes = likes
        postDao.insertPosts(post)
        FirebaseModel.addPost(post)
    }

    suspend fun getPostById(postId: String): Post? = withContext(Dispatchers.IO) {
        postDao.getPostById(postId)
    }

    suspend fun getPostsByUser(userId: String, currentUserId: String?): List<PostDisplayItem> = withContext(Dispatchers.IO) {
        val posts = postDao.getPostsByUserId(userId)
        posts.map { post ->
            val resolvedAuthorName = resolveAuthorName(post.authorId)
            val comments = commentDao.getCommentsByPostId(post.id)
            val previews = comments.map {
                val commentAuthor = resolveAuthorName(it.authorId)
                CommentPreview(commentAuthor, it.content, it.createdAt)
            }
            PostDisplayItem(
                post = post,
                authorName = resolvedAuthorName,
                commentCount = comments.size,
                previewComments = previews,
                isLiked = currentUserId != null && post.likes.contains(currentUserId),
                isCommentsVisible = false
            )
        }
    }

    suspend fun refreshUserPosts(userId: String) = withContext(Dispatchers.IO) {
        val remotePosts = FirebaseModel.getPostsByAuthor(userId)
        for (post in remotePosts) {
            postDao.insertPosts(post)
            // Also sync comments for each post
            val remoteComments = FirebaseModel.getAllComments(post.id)
            for (comment in remoteComments) {
                commentDao.insertComments(comment)
            }
        }
    }

    suspend fun getTotalLikesForUser(userId: String): Int = withContext(Dispatchers.IO) {
        val posts = postDao.getPostsByUserId(userId)
        posts.sumOf { it.likes.size }
    }

    suspend fun getTotalCommentsForUser(userId: String): Int = withContext(Dispatchers.IO) {
        commentDao.getCommentCountByUserId(userId)
    }

    private suspend fun resolveAuthorName(authorId: String): String {
        // Try local DB first
        val localUser = userDao.getUserById(authorId)
        if (localUser != null) return localUser.username

        // Fallback to Firebase
        return try {
            val remoteUser = FirebaseAuthModel.getUser(authorId)
            if (remoteUser != null) {
                userDao.registerUser(remoteUser)
                remoteUser.username
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
