package com.example.petopia.util

import androidx.room.TypeConverter
import com.example.petopia.data.PostType
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",") ?: emptyList()
    }

    @TypeConverter
    fun fromPostType(value: PostType): String {
        return value.name
    }

    @TypeConverter
    fun toPostType(value: String): PostType {
        return PostType.valueOf(value)
    }
}
