package com.example.petopia.ui.home

import android.graphics.Bitmap
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petopia.data.remote.storage.StorageModel
import com.example.petopia.types.PostType
import com.example.petopia.data.model.Post
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val storageModel: StorageModel = StorageModel()
) : ViewModel() {

    private val _postType = MutableLiveData<PostType>(PostType.RESCUE)
    val postType: LiveData<PostType> = _postType

    private val _title = MutableLiveData<String>("")
    val title: LiveData<String> = _title

    private val _content = MutableLiveData<String>("")
    val content: LiveData<String> = _content

    private val _imageBitmap = MutableLiveData<Bitmap?>(null)
    val imageBitmap: LiveData<Bitmap?> = _imageBitmap

    private val _imageUrl = MutableLiveData<String?>(null)
    val imageUrl: LiveData<String?> = _imageUrl

    private val _hashtags = MutableLiveData<String>("")

    private val _postCreated = MutableLiveData<Boolean>()
    val postCreated: LiveData<Boolean> = _postCreated

    private val _isUploading = MutableLiveData<Boolean>(false)
    val isUploading: LiveData<Boolean> = _isUploading

    private val _titleError = MutableLiveData<String?>(null)
    val titleError: LiveData<String?> = _titleError

    private val _contentError = MutableLiveData<String?>(null)
    val contentError: LiveData<String?> = _contentError

    fun setPostType(type: PostType) {
        _postType.value = type
    }

    fun setTitle(text: String) {
        _title.value = text
        _titleError.value = null
    }

    fun setContent(text: String) {
        _content.value = text
        _contentError.value = null
    }

    fun setImageBitmap(bitmap: Bitmap?) {
        _imageBitmap.value = bitmap
    }

    fun setImageUrl(url: String?) {
        _imageUrl.value = url
    }

    fun setHashtags(text: String) {
        _hashtags.value = text
    }

    fun submitPost(context: Context) {
        val currentTitle = _title.value ?: ""
        val currentContent = _content.value ?: ""
        val currentType = _postType.value ?: PostType.RESCUE
        val currentBitmap = _imageBitmap.value
        
        var hasError = false
        if (currentTitle.isBlank()) {
            _titleError.value = "Title is required"
            hasError = true
        }
        if (currentContent.isBlank()) {
            _contentError.value = "Description is required"
            hasError = true
        }
        
        if (hasError) return

        val currentHashtags = _hashtags.value
            ?.split(Regex("\\s+"))
            ?.filter { it.isNotBlank() }
            ?: emptyList()
            
        _isUploading.value = true
        
        if (currentBitmap != null) {
            val path = "posts/${System.currentTimeMillis()}"
            storageModel.uploadImage(context, currentBitmap, path) { url ->
                if (url != null) {
                    _imageUrl.value = url
                    createAndSavePost(currentTitle, currentContent, currentType, currentHashtags, url)
                } else {
                    _isUploading.value = false
                }
            }
        } else {
            createAndSavePost(currentTitle, currentContent, currentType, currentHashtags, _imageUrl.value)
        }
    }

    private fun createAndSavePost(
        title: String,
        content: String,
        type: PostType,
        hashtags: List<String>,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            
            val newPost = Post(
                id = System.currentTimeMillis().toString(),
                title = title,
                content = content,
                imageUrl = imageUrl,
                authorId = user.id,
                createdAt = System.currentTimeMillis(),
                postType = type,
                hashtags = hashtags,
                likes = emptyList()
            )
            
            postRepository.insertPosts(listOf(newPost))
            _isUploading.postValue(false)
            _postCreated.postValue(true)
        }
    }
}
