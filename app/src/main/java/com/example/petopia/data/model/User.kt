package com.example.petopia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // Firebase UID
    val email: String,
    val username: String,
    val profileImageUrl: String? = null,
    val dateOfBirth: String? = null,
    val petsCount: Int = 0,
    val seniority: String? = "Newcomer"
) {
    companion object {
        fun fromJson(json: Map<String, Any>): User {
            return User(
                id = json["id"] as? String ?: "",
                email = json["email"] as? String ?: "",
                username = json["username"] as? String ?: "",
                profileImageUrl = json["profileImageUrl"] as? String,
                dateOfBirth = json["dateOfBirth"] as? String,
                petsCount = (json["petsCount"] as? Long)?.toInt() ?: 0,
                seniority = json["seniority"] as? String ?: "Newcomer"
            )
        }
    }

    fun toJson(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "email" to email,
            "username" to username,
            "profileImageUrl" to profileImageUrl,
            "dateOfBirth" to dateOfBirth,
            "petsCount" to petsCount,
            "seniority" to seniority
        )
    }
}
