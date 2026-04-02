package com.example.petopia.util

import androidx.room.TypeConverter
import com.example.petopia.data.model.PostType
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",")
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
