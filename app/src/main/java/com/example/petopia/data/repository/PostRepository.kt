package com.example.petopia.data.repository

import android.content.Context
import com.example.petopia.dao.AppLocalDB
import com.example.petopia.data.CommentPreview
import com.example.petopia.data.model.Comment
import com.example.petopia.data.model.Post
import com.example.petopia.data.PostDisplayItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRepository(context: Context) {
    private val postDao = AppLocalDB.getDatabase(context).postDao()
    private val commentDao = AppLocalDB.getDatabase(context).commentDao()

    suspend fun getAllPostsWithPreviews(): List<PostDisplayItem> = withContext(Dispatchers.IO) {
        val posts = postDao.getAllPosts()
        posts.map { post ->
            val comments = commentDao.getCommentsByPostId(post.id)
            val previews = comments.map { CommentPreview(it.authorName, it.content, it.createdAt) }
            PostDisplayItem(
                post = post,
                commentCount = comments.size,
                previewComments = previews,
                isLiked = false,
                isCommentsVisible = false
            )
        }
    }

    suspend fun insertPosts(posts: List<Post>) = withContext(Dispatchers.IO) {
        postDao.insertPosts(*posts.toTypedArray())
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
    }


    suspend fun getPostById(postId: String): Post? = withContext(Dispatchers.IO) {
        postDao.getPostById(postId)
    }
}
