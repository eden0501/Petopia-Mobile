package com.example.petopia.ui.profile

import androidx.lifecycle.*
import com.example.petopia.data.model.PostDisplayItem
import com.example.petopia.data.model.User
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _posts = MutableLiveData<List<PostDisplayItem>>(emptyList())
    val posts: LiveData<List<PostDisplayItem>> = _posts

    private val _postCount = MutableLiveData(0)
    val postCount: LiveData<Int> = _postCount

    private val _likesCount = MutableLiveData(0)
    val likesCount: LiveData<Int> = _likesCount

    private val _commentsCount = MutableLiveData(0)
    val commentsCount: LiveData<Int> = _commentsCount

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            val currentUser = userRepository.getCurrentUser()
            if (currentUser == null) {
                _isLoading.value = false
                return@launch
            }
            _user.value = currentUser

            val userId = currentUser.id

            // Load from local first
            loadUserData(userId)

            // Fetch from remote then reload
            postRepository.refreshUserPosts(userId)
            loadUserData(userId)

            _isLoading.value = false
        }
    }

    private suspend fun loadUserData(userId: String) {
        val userPosts = postRepository.getPostsByUser(userId, userId)
        _posts.value = userPosts
        _postCount.value = userPosts.size
        _likesCount.value = postRepository.getTotalLikesForUser(userId)
        _commentsCount.value = postRepository.getTotalCommentsForUser(userId)
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            postRepository.toggleLike(userId, postId)
            loadUserData(userId)
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
            val currentUser = userRepository.getCurrentUser() ?: return@launch
            postRepository.addComment(postId, currentUser.id, text)
            loadUserData(currentUser.id)
        }
    }

    fun logout() {
        viewModelScope.launch {
            postRepository.resetSyncTimestamp()
            userRepository.logout()
        }
    }}
