package com.example.petopia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String, // Using username as ID for now
    val passwordHashed: String,
    val profileImageUrl: String?,
    val dateOfBirth: String?,
    val petsCount: Int = 0,
    val seniority: String? = "Newcomer"
)