package com.example.petopia.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petopia.R
import com.example.petopia.data.local.dao.AppLocalDB
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val db = AppLocalDB.getDatabase(context)
            @Suppress("UNCHECKED_CAST")
            val defaultFact = context.getString(R.string.default_fact)
            return HomeViewModel(
                PostRepository.getInstance(context),
                UserRepository(db.userDao(), db),
                defaultFact
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

