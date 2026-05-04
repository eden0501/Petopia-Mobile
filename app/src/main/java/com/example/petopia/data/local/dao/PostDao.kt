package com.example.petopia.data.local.dao

import androidx.room.*
import com.example.petopia.data.model.Post

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    suspend fun getAllPosts(): List<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(vararg posts: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): Post?

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    @Query("SELECT * FROM posts WHERE authorId = :userId ORDER BY createdAt DESC")
    suspend fun getPostsByUserId(userId: String): List<Post>

    @Query("SELECT * FROM posts WHERE authorId = :userId")
    suspend fun getRawPostsByUserId(userId: String): List<Post>
}
