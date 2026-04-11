package com.example.petopia.data.repository

import android.content.Context
import com.example.petopia.data.local.dao.AppLocalDB
import com.example.petopia.types.CommentPreview
import com.example.petopia.types.PostDisplayItem
import com.example.petopia.data.model.Comment
import com.example.petopia.data.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.petopia.base.Constants
import com.example.petopia.data.remote.FirebaseModel

class PostRepository private constructor(context: Context) {
    private val postDao = AppLocalDB.getDatabase(context).postDao()
    private val commentDao = AppLocalDB.getDatabase(context).commentDao()

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
            val comments = commentDao.getCommentsByPostId(post.id)
            val previews = comments.map { CommentPreview(it.authorName, it.content, it.createdAt) }
            PostDisplayItem(
                post = post,
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
            if (post.isDeleted) {
                postDao.deletePost(post)
            } else {
                postDao.insertPosts(post)
            }
            post.lastUpdated?.let { postLastUpdated ->
                if (time < postLastUpdated) {
                    time = postLastUpdated
                }
            }
        }
        lastUpdatedPosts = time
    }

    suspend fun deletePost(post: Post) = withContext(Dispatchers.IO) {
        postDao.deletePost(post)
        FirebaseModel.deletePost(post)
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
}
