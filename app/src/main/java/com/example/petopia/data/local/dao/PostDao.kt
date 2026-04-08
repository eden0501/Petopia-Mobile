package com.example.petopia.data.local.dao

import androidx.room.*
import com.example.petopia.data.model.Post

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): List<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(vararg posts: Post)

    @Delete
    fun deletePost(post: Post)

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): Post?

    @Query("DELETE FROM posts")
    fun deleteAllPosts()

    @Query("SELECT * FROM posts WHERE authorId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserId(userId: String): List<Post>

    @Query("SELECT COUNT(*) FROM posts WHERE likes LIKE '%' || :userId || '%'")
    fun getLikesGivenByUser(userId: String): Int
}
