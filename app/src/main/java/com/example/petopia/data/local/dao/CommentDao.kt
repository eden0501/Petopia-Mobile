package com.example.petopia.data.local.dao

import androidx.room.*
import com.example.petopia.data.model.Comment

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    suspend fun getCommentsByPostId(postId: String): List<Comment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(vararg comments: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)

    @Query("SELECT COUNT(*) FROM comments WHERE postId IN (SELECT id FROM posts WHERE authorId = :userId)")
    suspend fun getReceivedCommentsCount(userId: String): Int
    
    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()

    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: String)
}
