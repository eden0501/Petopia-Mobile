package com.example.petopia.ui.auth

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petopia.data.model.User
import com.example.petopia.data.remote.storage.StorageModel
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: UserRepository,
    private val storageModel: StorageModel = StorageModel()
) : ViewModel() {
    val authStatus = MutableLiveData<Result<User>?>()

    private val _profileImageBitmap = MutableLiveData<Bitmap?>(null)
    val profileImageBitmap: LiveData<Bitmap?> = _profileImageBitmap

    private val _isUploading = MutableLiveData(false)
    val isUploading: LiveData<Boolean> = _isUploading

    fun setProfileImageBitmap(bitmap: Bitmap?) {
        _profileImageBitmap.value = bitmap
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = repository.login(email, pass)
            authStatus.postValue(result)
        }
    }

    fun signup(user: User, pass: String, context: Context) {
        val bitmap = _profileImageBitmap.value
        _isUploading.value = true

        if (bitmap != null) {
            val path = "profile_pictures/${System.currentTimeMillis()}"
            storageModel.uploadImage(context, bitmap, path) { url ->
                viewModelScope.launch {
                    val userWithImage = user.copy(profileImageUrl = url)
                    val result = repository.signup(userWithImage, pass)
                    _isUploading.postValue(false)
                    authStatus.postValue(result)
                }
            }
        } else {
            viewModelScope.launch {
                val result = repository.signup(user, pass)
                _isUploading.postValue(false)
                authStatus.postValue(result)
            }
        }
    }
}
