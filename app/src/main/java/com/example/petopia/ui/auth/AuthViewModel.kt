package com.example.petopia.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    val authStatus = MutableLiveData<Boolean?>()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val user = repository.login(email, pass)
            authStatus.postValue(user != null)
        }
    }

    fun signup(email: String, pass: String, username: String) {
        viewModelScope.launch {
            val success = repository.signup(email, pass, username)
            authStatus.postValue(success)
        }
    }

    fun logout() {
        repository.logout()
    }
}