package com.example.petopia.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petopia.data.model.PostType

class CreatePostViewModel : ViewModel() {

    private val _postType = MutableLiveData<PostType>(PostType.RESCUE)
    val postType: LiveData<PostType> = _postType

    private val _title = MutableLiveData<String>("")
    val title: LiveData<String> = _title

    private val _content = MutableLiveData<String>("")
    val content: LiveData<String> = _content

    private val _imageUrl = MutableLiveData<String?>(null)
    val imageUrl: LiveData<String?> = _imageUrl

    private val _hashtags = MutableLiveData<String>("")
    val hashtags: LiveData<String> = _hashtags

    fun setPostType(type: PostType) {
        _postType.value = type
    }

    fun setTitle(text: String) {
        _title.value = text
    }

    fun setContent(text: String) {
        _content.value = text
    }


    fun setImageUrl(url: String?) {
        _imageUrl.value = url
    }

    fun setHashtags(text: String) {
        _hashtags.value = text
    }

    fun submitPost() {
        // Stub for form submission
        val currentTitle = _title.value ?: ""
        val currentContent = _content.value ?: ""
        val currentType = _postType.value ?: PostType.RESCUE
        val currentHashtags = _hashtags.value ?: ""
        
        println("Submitting post: Title=$currentTitle, Type=$currentType, Hashtags=$currentHashtags")
    }
}
