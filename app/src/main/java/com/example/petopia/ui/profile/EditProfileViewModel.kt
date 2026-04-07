package com.example.petopia.ui.profile

import androidx.lifecycle.*
import com.example.petopia.data.model.User
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _saveResult = MutableLiveData<Result<User>?>()
    val saveResult: LiveData<Result<User>?> = _saveResult

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

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
            _isSaving.value = true
            val current = _user.value ?: return@launch
            val updated = current.copy(
                username = username,
                petsCount = petsCount,
                petOwnerSince = petOwnerSince
            )
            val result = userRepository.updateUser(updated)
            _isSaving.value = false
            _saveResult.value = result
            if (result.isSuccess) {
                _user.value = updated
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            postRepository.resetSyncTimestamp()
            userRepository.logout()
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            _isSaving.value = true
            postRepository.resetSyncTimestamp()
            val result = userRepository.deleteAccount(password)
            _isSaving.value = false
            _deleteResult.value = result
        }
    }
}
