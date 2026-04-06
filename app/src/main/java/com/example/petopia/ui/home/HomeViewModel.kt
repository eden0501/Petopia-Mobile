package com.example.petopia.ui.home

import androidx.lifecycle.*
import com.example.petopia.data.model.CommentPreview
import com.example.petopia.data.model.PostDisplayItem
import com.example.petopia.data.model.Post
import com.example.petopia.data.model.PostType
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _posts = MutableLiveData<List<PostDisplayItem>>(emptyList())
    val posts: LiveData<List<PostDisplayItem>> = _posts

    private val _selectedFilter = MutableLiveData(PostFilter.ALL)
    val selectedFilter: LiveData<PostFilter> = _selectedFilter

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true

            val userId = userRepository.getCurrentUserId()
            val list = repository.getAllPostsWithPreviews(userId)
            _posts.value = list
            
            repository.refreshAllPosts()
            val refreshedList = repository.getAllPostsWithPreviews(userId)
            _posts.value = refreshedList

            _isLoading.value = false
        }
    }

    fun setFilter(filter: PostFilter) {
        _selectedFilter.value = filter
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            
            repository.toggleLike(userId, postId)
            
            _posts.value = _posts.value?.map { item ->
                if (item.post.id == postId) {
                    val currentLikes = item.post.likes.toMutableList()
                    if (currentLikes.contains(userId)) {
                        currentLikes.remove(userId)
                    } else {
                        currentLikes.add(userId)
                    }
                    val updatedPost = item.post.copy(likes = currentLikes)
                    item.copy(post = updatedPost, isLiked = currentLikes.contains(userId))
                } else {
                    item
                }
            }
        }
    }

    fun toggleComments(postId: String) {
        _posts.value = _posts.value?.map { item ->
            if (item.post.id == postId) {
                item.copy(isCommentsVisible = !item.isCommentsVisible)
            } else {
                item
            }
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            
            // 1. Update Room & Firestore
            repository.addComment(postId, user.id, text)
            
            // 2. Update LiveData manually
            _posts.value = _posts.value?.map { item ->
                if (item.post.id == postId) {
                    val newCommentPreview = CommentPreview(user.username, text, System.currentTimeMillis())
                    val updatedPreviews = item.previewComments.toMutableList()
                    updatedPreviews.add(0, newCommentPreview) // Newest first
                    item.copy(
                        commentCount = item.commentCount + 1,
                        previewComments = updatedPreviews
                    )
                } else {
                    item
                }
            }
        }
    }
}

enum class PostFilter {
    ALL,
    RESCUE,
    CARE_TIPS,
    SUPPLIES
}
