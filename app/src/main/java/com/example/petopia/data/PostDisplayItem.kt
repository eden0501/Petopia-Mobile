package com.example.petopia.data

import com.example.petopia.data.model.Post

data class CommentPreview(
    val authorName: String,
    val content: String,
    val createdAt: Long
)

data class PostDisplayItem(
    val post: Post,
    val commentCount: Int,
    val previewComments: List<CommentPreview>,
    val isLiked: Boolean = false,
    val isCommentsVisible: Boolean = false
)
