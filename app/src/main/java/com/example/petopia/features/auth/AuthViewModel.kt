package com.example.petopia.features.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petopia.data.model.User
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    val authStatus = MutableLiveData<Boolean?>()

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            val user = repository.login(username, pass)
            authStatus.postValue(user != null)
        }
    }

    fun signup(username: String, pass: String) {
        viewModelScope.launch {
            val success = repository.signup(User(username, pass, null, null))
            authStatus.postValue(success)
        }
    }
}