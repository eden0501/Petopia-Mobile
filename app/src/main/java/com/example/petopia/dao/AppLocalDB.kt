package com.example.petopia.dao

import android.content.Context
import androidx.room.Room
import com.example.petopia.base.MyApplication

object AppLocalDB {

    @Volatile
    private var instance: AppLocalDbRepository? = null

    fun getDatabase(context: Context): AppLocalDbRepository {
        return instance ?: synchronized(this) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppLocalDbRepository::class.java,
                "petopia_database"
            )
            .fallbackToDestructiveMigration()
            .build()
            instance = db
            db
        }
    }

    val db: AppLocalDbRepository by lazy {
        val context = MyApplication.appContext
            ?: throw IllegalStateException("Context is null. Make sure to call AppLocalDB.getDatabase(context) first or ensure MyApplication is initialized.")
        getDatabase(context)
    }
}
