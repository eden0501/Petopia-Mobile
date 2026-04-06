package com.example.petopia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: String,
    val content: String,
    val createdAt: Long
)
