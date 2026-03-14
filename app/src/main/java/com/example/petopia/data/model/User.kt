package com.example.petopia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "", // Firebase UID
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String? = null,
    val petsCount: Int = 0,
    val petOwnerSince: String? = null
)
