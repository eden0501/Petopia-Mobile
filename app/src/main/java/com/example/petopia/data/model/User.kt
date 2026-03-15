package com.example.petopia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String? = null, // TODO: implement the profile image upload portion later
    val petsCount: Int = 0,
    val petOwnerSince: String? = null
)
