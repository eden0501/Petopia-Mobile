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
) {
    companion object {
        const val ID_KEY = "id"
        const val POST_ID_KEY = "postId"
        const val AUTHOR_ID_KEY = "authorId"
        const val CONTENT_KEY = "content"
        const val CREATED_AT_KEY = "createdAt"

        fun fromJson(json: Map<String, Any?>): Comment {
            return Comment(
                id = json[ID_KEY] as? String ?: "",
                postId = json[POST_ID_KEY] as? String ?: "",
                authorId = json[AUTHOR_ID_KEY] as? String ?: "",
                content = json[CONTENT_KEY] as? String ?: "",
                createdAt = json[CREATED_AT_KEY] as? Long ?: 0L
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            ID_KEY to id,
            POST_ID_KEY to postId,
            AUTHOR_ID_KEY to authorId,
            CONTENT_KEY to content,
            CREATED_AT_KEY to createdAt
        )
}
