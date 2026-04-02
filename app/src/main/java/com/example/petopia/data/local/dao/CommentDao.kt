package com.example.petopia.data.local.dao

import androidx.room.*
import com.example.petopia.data.model.Comment

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsByPostId(postId: String): List<Comment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertComments(vararg comments: Comment)

    @Delete
    fun deleteComment(comment: Comment)
}
