package com.example.petopia.dao

import androidx.room.*
import com.example.petopia.data.model.Comment
import com.example.petopia.data.model.Post
import com.example.petopia.data.model.User
import com.example.petopia.util.Converters

@Database(entities = [User::class, Post::class, Comment::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
}
