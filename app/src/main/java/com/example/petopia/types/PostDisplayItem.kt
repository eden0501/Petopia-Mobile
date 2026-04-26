package com.example.petopia.types

import com.example.petopia.data.model.Post

data class CommentPreview(
    val authorName: String,
    val content: String,
    val createdAt: Long
)

data class PostDisplayItem(
    val post: Post,
    val authorName: String,
    val authorProfileImageUrl: String? = null,
    val commentCount: Int,
    val previewComments: List<CommentPreview>,
    val isLiked: Boolean = false,
    val isCommentsVisible: Boolean = false
)
