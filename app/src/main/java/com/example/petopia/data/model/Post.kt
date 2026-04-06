package com.example.petopia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.petopia.data.model.PostType

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val authorId: String,
    val createdAt: Long,
    val postType: PostType,
    val hashtags: List<String> = emptyList(),
    var likes: List<String> = emptyList(),
    var lastUpdated: Long? = null
) {
    val likeCount: Int
        get() = likes.size

    companion object {
        const val ID_KEY = "id"
        const val TITLE_KEY = "title"
        const val CONTENT_KEY = "content"
        const val IMAGE_URL_KEY = "imageUrl"
        const val AUTHOR_ID_KEY = "authorId"
        const val CREATED_AT_KEY = "createdAt"
        const val POST_TYPE_KEY = "postType"
        const val HASHTAGS_KEY = "hashtags"
        const val LIKES_KEY = "likes"
        const val LAST_UPDATED_KEY = "lastUpdated"

        fun fromJson(json: Map<String, Any?>): Post {
            val timestamp = json[LAST_UPDATED_KEY] as? Timestamp
            return Post(
                id = json[ID_KEY] as? String ?: "",
                title = json[TITLE_KEY] as? String ?: "",
                content = json[CONTENT_KEY] as? String ?: "",
                imageUrl = json[IMAGE_URL_KEY] as? String,
                authorId = json[AUTHOR_ID_KEY] as? String ?: "",
                createdAt = json[CREATED_AT_KEY] as? Long ?: 0L,
                postType = PostType.valueOf(json[POST_TYPE_KEY] as? String ?: PostType.RESCUE.name),
                hashtags = (json[HASHTAGS_KEY] as? List<*>)?.filterIsInstance<String>()
                    ?: emptyList(),
                likes = (json[LIKES_KEY] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                lastUpdated = timestamp?.toDate()?.time
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            ID_KEY to id,
            TITLE_KEY to title,
            CONTENT_KEY to content,
            IMAGE_URL_KEY to imageUrl,
            AUTHOR_ID_KEY to authorId,
            CREATED_AT_KEY to createdAt,
            POST_TYPE_KEY to postType.name,
            HASHTAGS_KEY to hashtags,
            LIKES_KEY to likes,
            LAST_UPDATED_KEY to FieldValue.serverTimestamp()
        )
}
