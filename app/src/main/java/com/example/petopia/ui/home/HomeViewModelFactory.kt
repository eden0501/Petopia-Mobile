package com.example.petopia.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petopia.R
import com.example.petopia.data.local.dao.AppDatabase
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val defaultFact = context.getString(R.string.default_fact)
            return HomeViewModel(
                PostRepository.getInstance(context),
                UserRepository.getInstance(context),
                defaultFact
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

