package com.example.petopia.data.local.dao

import android.content.Context
import androidx.room.Room

object AppLocalDB {
    @Volatile
    private var instance: AppLocalDbRepository? = null

    fun getDatabase(context: Context): AppLocalDbRepository {
        return instance ?: synchronized(this) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppLocalDbRepository::class.java,
                "petopia_db"
            ).fallbackToDestructiveMigration().build()
            instance = db
            db
        }
    }
}
