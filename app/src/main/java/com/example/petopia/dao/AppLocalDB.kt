package com.example.petopia.dao

import androidx.room.Room
import com.example.petopia.base.MyApplication

object AppLocalDB {

    val db: AppLocalDbRepository by lazy {

        val context = MyApplication.appContext
            ?: throw IllegalStateException("Context is null")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "petopia_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
}