package com.example.petopia.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.petopia.data.local.dao.AppDatabase
import com.example.petopia.types.CommentPreview
import com.example.petopia.types.PostDisplayItem
import com.example.petopia.data.model.Comment
import com.example.petopia.data.model.Post
import com.example.petopia.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.petopia.base.Constants
import com.example.petopia.data.remote.FirebaseModel
import com.example.petopia.data.remote.FirebaseAuthModel
import android.util.Log

class PostRepository private constructor(context: Context) {
    private val postDao = AppDatabase.getDatabase(context).postDao()
    private val commentDao = AppDatabase.getDatabase(context).commentDao()
    private val userDao = AppDatabase.getDatabase(context).userDao()

    private val sharedPrefs = context.getSharedPreferences(Constants.SharedPrefs.PREFS_NAME, Context.MODE_PRIVATE)

    var lastUpdatedPosts: Long
        get() = sharedPrefs.getLong(Constants.SharedPrefs.LAST_UPDATED_POSTS, 0)
        set(value) = sharedPrefs.edit { putLong(Constants.SharedPrefs.LAST_UPDATED_POSTS, value) }

    var hasCompletedFullSync: Boolean
        get() = sharedPrefs.getBoolean(Constants.SharedPrefs.HAS_COMPLETED_FULL_SYNC, false)
        private set(value) = sharedPrefs.edit { putBoolean(Constants.SharedPrefs.HAS_COMPLETED_FULL_SYNC, value) }

    fun resetSyncTimestamp() {
        lastUpdatedPosts = 0
        hasCompletedFullSync = false
    }

    companion object {
        @Volatile
        private var instance: PostRepository? = null

        fun getInstance(context: Context): PostRepository {
            return instance ?: synchronized(this) {
                instance ?: PostRepository(context).also { instance = it }
            }
        }
    }

    suspend fun getAllPostsWithPreviews(currentUserId: String?): Result<List<PostDisplayItem>> = withContext(Dispatchers.IO) {
        try {
            val posts = postDao.getAllPosts()
            val authorCache = mutableMapOf<String, User?>()
            val items = posts.map { post ->
                val author = authorCache.getOrPut(post.authorId) { resolveAuthor(post.authorId) }
                val comments = commentDao.getCommentsByPostId(post.id)
                val previews = comments.map {
                    val cachedAuthor = authorCache.getOrPut(it.authorId) { resolveAuthor(it.authorId) }
                    CommentPreview(cachedAuthor?.username ?: "Unknown", it.content, it.createdAt)
                }
                PostDisplayItem(
                    post = post,
                    authorName = author?.username ?: "Unknown",
                    authorProfileImageUrl = author?.profileImageUrl,
                    commentCount = comments.size,
                    previewComments = previews,
                    isLiked = currentUserId != null && post.likes.contains(currentUserId),
                    isCommentsVisible = false
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAllPosts(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val posts = FirebaseModel.getAllPosts(0)

            postDao.deleteAllPosts()
            commentDao.deleteAllComments()

            var time = 0L
            for (post in posts) {
                if (!post.isDeleted) {
                    postDao.insertPosts(post)
                    val comments = FirebaseModel.getAllComments(post.id)
                    for (comment in comments) {
                        commentDao.insertComments(comment)
                    }
                }
                post.lastUpdated?.let { if (time < it) time = it }
            }
            lastUpdatedPosts = time
            hasCompletedFullSync = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(post: Post): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            commentDao.deleteCommentsByPostId(post.id)
            postDao.deletePost(post)
            FirebaseModel.deletePost(post)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error deleting post ${post.id}", e)
            Result.failure(e)
        }
    }

    suspend fun refreshPostsIncremental(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val posts = FirebaseModel.getAllPosts(lastUpdatedPosts)
            var time = lastUpdatedPosts
            for (post in posts) {
                if (post.isDeleted) {
                    postDao.deletePost(post)
                    commentDao.deleteCommentsByPostId(post.id)
                } else {
                    postDao.insertPosts(post)
                    val comments = FirebaseModel.getAllComments(post.id)
                    for (comment in comments) {
                        commentDao.insertComments(comment)
                    }
                }
                post.lastUpdated?.let { if (time < it) time = it }
            }
            lastUpdatedPosts = time
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertPosts(posts: List<Post>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            postDao.insertPosts(*posts.toTypedArray())
            posts.forEach { post ->
                FirebaseModel.addPost(post)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, authorId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val comment = Comment(
                id = System.currentTimeMillis().toString(),
                postId = postId,
                authorId = authorId,
                content = content,
                createdAt = System.currentTimeMillis()
            )
            commentDao.insertComments(comment)
            FirebaseModel.addComment(comment)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error adding comment to post $postId", e)
            Result.failure(e)
        }
    }

    suspend fun toggleLike(userId: String, postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val post = postDao.getPostById(postId) ?: throw Exception("Post not found")
            val likes = post.likes.toMutableList()
            
            if (likes.contains(userId)) {
                likes.remove(userId)
            } else {
                likes.add(userId)
            }
            
            post.likes = likes
            postDao.insertPosts(post)
            FirebaseModel.updatePostLikes(postId, userId, likes.contains(userId))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error toggling like for post $postId", e)
            Result.failure(e)
        }
    }

    suspend fun getPostById(postId: String): Result<Post?> = withContext(Dispatchers.IO) {
        try {
            Result.success(postDao.getPostById(postId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostsByUser(userId: String, currentUserId: String?): Result<List<PostDisplayItem>> = withContext(Dispatchers.IO) {
        try {
            val posts = postDao.getPostsByUserId(userId)
            val authorCache = mutableMapOf<String, User?>()
            val items = posts.map { post ->
                val author = authorCache.getOrPut(post.authorId) { resolveAuthor(post.authorId) }
                val comments = commentDao.getCommentsByPostId(post.id)
                val previews = comments.map {
                    val cachedAuthor = authorCache.getOrPut(it.authorId) { resolveAuthor(it.authorId) }
                    CommentPreview(cachedAuthor?.username ?: "Unknown", it.content, it.createdAt)
                }
                PostDisplayItem(
                    post = post,
                    authorName = author?.username ?: "Unknown",
                    authorProfileImageUrl = author?.profileImageUrl,
                    commentCount = comments.size,
                    previewComments = previews,
                    isLiked = currentUserId != null && post.likes.contains(currentUserId),
                    isCommentsVisible = false
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshUserPosts(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remotePosts = FirebaseModel.getPostsByAuthor(userId)
            for (post in remotePosts) {
                if (post.isDeleted) {
                    postDao.deletePost(post)
                    commentDao.deleteCommentsByPostId(post.id)
                } else {
                    postDao.insertPosts(post)
                    val remoteComments = FirebaseModel.getAllComments(post.id)
                    for (comment in remoteComments) {
                        commentDao.insertComments(comment)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalLikesReceived(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val posts = postDao.getRawPostsByUserId(userId)
            Result.success(posts.sumOf { it.likes.size })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalCommentsReceived(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Result.success(commentDao.getReceivedCommentsCount(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveAuthor(authorId: String): User? {
        val localUser = userDao.getUserById(authorId)
        if (localUser != null) return localUser

        return try {
            val remoteUser = FirebaseAuthModel.getUser(authorId)
            if (remoteUser != null) {
                userDao.registerUser(remoteUser)
            }
            remoteUser
        } catch (_: Exception) {
            null
        }
    }
}
