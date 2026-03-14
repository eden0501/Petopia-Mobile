package com.example.petopia.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.petopia.data.model.User

@Database(entities = [User::class], version = 20, exportSchema = false)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun userDao(): UserDao
}