package com.example.petopia.ui.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.example.petopia.data.model.User
import com.example.petopia.data.remote.storage.StorageModel
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val storageModel: StorageModel = StorageModel()
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _saveResult = MutableLiveData<Result<User>?>()
    val saveResult: LiveData<Result<User>?> = _saveResult

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _profileImageBitmap = MutableLiveData<Bitmap?>(null)
    val profileImageBitmap: LiveData<Bitmap?> = _profileImageBitmap

    private var profileImageChanged = false

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _user.value = userRepository.getCurrentUser().getOrNull()
        }
    }

    fun setProfileImageBitmap(bitmap: Bitmap?) {
        _profileImageBitmap.value = bitmap
        profileImageChanged = true
    }

    fun hasImageChanged(): Boolean = profileImageChanged

    fun saveProfile(username: String, petsCount: Int, petOwnerSince: String?, context: Context) {
        val current = _user.value ?: return
        val bitmap = _profileImageBitmap.value

        viewModelScope.launch {
            _isSaving.value = true

            if (bitmap != null && profileImageChanged) {
                val path = "profile_pictures/${current.id}_${System.currentTimeMillis()}"
                storageModel.uploadImage(context, bitmap, path) { url ->
                    viewModelScope.launch {
                        val updated = current.copy(
                            username = username,
                            petsCount = petsCount,
                            petOwnerSince = petOwnerSince,
                            profileImageUrl = url ?: current.profileImageUrl
                        )
                        val result = userRepository.updateUser(updated)
                        _saveResult.postValue(result)
                        if (result.isSuccess) {
                            _user.postValue(updated)
                        } else {
                            _isSaving.postValue(false)
                        }
                    }
                }
            } else {
                val updated = current.copy(
                    username = username,
                    petsCount = petsCount,
                    petOwnerSince = petOwnerSince
                )
                val result = userRepository.updateUser(updated)
                _saveResult.value = result
                if (result.isSuccess) {
                    _user.value = updated
                } else {
                    _isSaving.value = false
                }
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
