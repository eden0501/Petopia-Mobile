package com.example.petopia.dao

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
}
