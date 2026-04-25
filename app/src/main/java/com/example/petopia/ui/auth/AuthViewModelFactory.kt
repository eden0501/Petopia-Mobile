package com.example.petopia.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petopia.data.local.dao.AppLocalDB
import com.example.petopia.data.repository.UserRepository

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val db = AppLocalDB.getDatabase(context)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(UserRepository(db.userDao(), db)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
