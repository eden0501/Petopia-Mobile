package com.example.petopia.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petopia.data.model.User
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    val authStatus = MutableLiveData<Boolean?>()

    fun login(email: String, pass: String) {
        // Mock login logic - in a real app, use Firebase or API
        viewModelScope.launch {
            authStatus.postValue(true)
        }
    }

    fun signup(user: User) {
        viewModelScope.launch {
            repository.signup(user)
            authStatus.postValue(true)
        }
    }
}
