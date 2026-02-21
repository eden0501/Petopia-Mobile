package com.example.petopia.data

data class CommentPreview(
    val authorName: String,
    val text: String,
    val timeAgo: String
)

data class PostDisplayItem(
    val post: Post,
    val likeCount: Int,
    val commentCount: Int,
    val previewComments: List<CommentPreview>
)
