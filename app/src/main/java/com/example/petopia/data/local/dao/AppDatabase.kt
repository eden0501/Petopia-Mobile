package com.example.petopia.data.local.dao

import android.content.Context
import androidx.room.*
import com.example.petopia.data.model.Comment
import com.example.petopia.data.model.Post
import com.example.petopia.data.model.User
import com.example.petopia.util.Converters

@Database(entities = [User::class, Post::class, Comment::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "petopia_db"
                ).fallbackToDestructiveMigration().build()
                instance = db
                db
            }
        }
    }
}
