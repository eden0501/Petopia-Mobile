package com.example.petopia.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.petopia.data.local.dao.AppLocalDB
import com.example.petopia.types.CommentPreview
import com.example.petopia.types.PostDisplayItem
import com.example.petopia.data.model.Comment
import com.example.petopia.data.model.Post
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

    suspend fun getAllPostsWithPreviews(currentUserId: String?): List<PostDisplayItem> = withContext(Dispatchers.IO) {
        val posts = postDao.getAllPosts()
        val authorCache = mutableMapOf<String, com.example.petopia.data.model.User?>()
        posts.map { post ->
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
    }

    suspend fun refreshAllPosts() = withContext(Dispatchers.IO) {
        val posts = FirebaseModel.getAllPosts(0)

        postDao.deleteAllPosts()
        commentDao.deleteAllComments()

        var time = 0L
        for (post in posts) {
            if (post.isDeleted) {
                postDao.deletePost(post)
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
        hasCompletedFullSync = true
    }

    suspend fun deletePost(post: Post) = withContext(Dispatchers.IO) {
        commentDao.deleteCommentsByPostId(post.id)
        postDao.deletePost(post)
        FirebaseModel.deletePost(post)
    }

    suspend fun updatePost(post: Post) = withContext(Dispatchers.IO) {
        postDao.insertPosts(post)
        FirebaseModel.addPost(post)
    }

    suspend fun refreshPostsIncremental() = withContext(Dispatchers.IO) {
        val posts = FirebaseModel.getAllPosts(lastUpdatedPosts)
        var time = lastUpdatedPosts
        for (post in posts) {
            if (post.isDeleted) {
                postDao.deletePost(post)
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
    }

    suspend fun insertPosts(posts: List<Post>) = withContext(Dispatchers.IO) {
        postDao.insertPosts(*posts.toTypedArray())
        posts.forEach { post ->
            FirebaseModel.addPost(post)
        }
    }

    suspend fun addComment(postId: String, authorId: String, content: String) = withContext(Dispatchers.IO) {
        val comment = Comment(
            id = System.currentTimeMillis().toString(),
            postId = postId,
            authorId = authorId,
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
        val authorCache = mutableMapOf<String, com.example.petopia.data.model.User?>()
        posts.map { post ->
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
    }

    suspend fun refreshUserPosts(userId: String) = withContext(Dispatchers.IO) {
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
    }

    suspend fun getTotalLikesReceived(userId: String): Int = withContext(Dispatchers.IO) {
        val posts = postDao.getRawPostsByUserId(userId)
        posts.sumOf { it.likes.size }
    }

    suspend fun getTotalCommentsReceived(userId: String): Int = withContext(Dispatchers.IO) {
        commentDao.getReceivedCommentsCount(userId)
    }

    private suspend fun resolveAuthor(authorId: String): com.example.petopia.data.model.User? {
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

    private suspend fun resolveAuthorName(authorId: String): String {
        return resolveAuthor(authorId)?.username ?: "Unknown"
    }
}
