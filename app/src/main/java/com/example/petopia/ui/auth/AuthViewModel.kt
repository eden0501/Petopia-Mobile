package com.example.petopia.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petopia.data.model.User
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    val authStatus = MutableLiveData<Result<User>?>()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = repository.login(email, pass)
            authStatus.postValue(result)
        }
    }

    fun signup(user: User, pass: String) {
        viewModelScope.launch {
            val result = repository.signup(user, pass)
            authStatus.postValue(result)
        }
    }
}
