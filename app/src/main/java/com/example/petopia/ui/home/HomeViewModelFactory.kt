package com.example.petopia.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petopia.dao.AppLocalDB
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val db = AppLocalDB.getDatabase(context)
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                PostRepository(context),
                UserRepository(db.userDao())
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

