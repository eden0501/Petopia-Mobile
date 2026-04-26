package com.example.petopia.ui.home

import android.graphics.Bitmap
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petopia.R
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

    private val _titleError = MutableLiveData<Int?>(null)
    val titleError: LiveData<Int?> = _titleError

    private val _contentError = MutableLiveData<Int?>(null)
    val contentError: LiveData<Int?> = _contentError

    // Edit mode
    private var editingPost: Post? = null
    val isEditMode: Boolean get() = editingPost != null

    fun initEditMode(post: Post) {
        editingPost = post
        _postType.value = post.postType
        _title.value = post.title
        _content.value = post.content
        _imageUrl.value = post.imageUrl
        _hashtags.value = post.hashtags.joinToString(" ")
    }

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
            _titleError.value = R.string.title_required_error
            hasError = true
        }
        if (currentContent.isBlank()) {
            _contentError.value = R.string.description_required_error
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
                    savePost(currentTitle, currentContent, currentType, currentHashtags, url)
                } else {
                    _isUploading.value = false
                }
            }
        } else {
            savePost(currentTitle, currentContent, currentType, currentHashtags, _imageUrl.value)
        }
    }

    private fun savePost(
        title: String,
        content: String,
        type: PostType,
        hashtags: List<String>,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            val existing = editingPost
            if (existing != null) {
                val updated = existing.copy(
                    title = title,
                    content = content,
                    imageUrl = imageUrl,
                    postType = type,
                    hashtags = hashtags
                )
                postRepository.updatePost(updated)
            } else {
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
            }
            _isUploading.postValue(false)
            _postCreated.postValue(true)
        }
    }
}
