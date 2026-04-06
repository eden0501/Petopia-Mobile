package com.example.petopia.ui.profile

import androidx.lifecycle.*
import com.example.petopia.data.model.User
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _saveResult = MutableLiveData<Result<User>?>()
    val saveResult: LiveData<Result<User>?> = _saveResult

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _user.value = userRepository.getCurrentUser()
        }
    }

    fun saveProfile(username: String, petsCount: Int, petOwnerSince: String?) {
        viewModelScope.launch {
            val current = _user.value ?: return@launch
            val updated = current.copy(
                username = username,
                petsCount = petsCount,
                petOwnerSince = petOwnerSince
            )
            val result = userRepository.updateUser(updated)
            _saveResult.value = result
            if (result.isSuccess) {
                _user.value = updated
            }
        }
    }

    fun logout() {
        userRepository.logout()
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val result = userRepository.deleteAccount()
            _deleteResult.value = result
        }
    }
}
