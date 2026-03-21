package com.example.petopia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.petopia.data.PostType

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val authorId: String,
    val authorName: String,
    val createdAt: Long,
    val postType: PostType,
    val hashtags: List<String> = emptyList(),
    var likeCount: Int = 0
)
