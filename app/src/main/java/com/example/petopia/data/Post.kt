package com.example.petopia.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val authorId: String,
    val authorName: String,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val postType: PostType,
    val hashtags: List<String> = emptyList()
)

enum class PostType {
    RESCUE,
    KNOWLEDGE,
    SUPPLIES
}

