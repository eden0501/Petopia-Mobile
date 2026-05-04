package com.example.petopia.ui.profile

import androidx.lifecycle.*
import com.example.petopia.R
import com.example.petopia.types.CommentPreview
import com.example.petopia.types.PostDisplayItem
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

    private val _error = MutableLiveData<Int?>(null)
    val error: LiveData<Int?> = _error

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userResult = userRepository.getCurrentUser()
                if (userResult.isFailure) {
                    _error.postValue(R.string.error_loading_posts)
                    return@launch
                }
                val currentUser = userResult.getOrNull() ?: return@launch
                _user.value = currentUser

                val userId = currentUser.id

                loadUserData(userId)

                val refreshResult = postRepository.refreshUserPosts(userId)
                if (refreshResult.isFailure) {
                    _error.postValue(R.string.error_loading_posts)
                }
                loadUserData(userId)
            } catch (e: Exception) {
                _error.postValue(R.string.error_loading_posts)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadUserData(userId: String) {
        val postsResult = postRepository.getPostsByUser(userId, userId)
        if (postsResult.isFailure) {
            _error.postValue(R.string.error_loading_posts)
        }
        val userPosts = postsResult.getOrDefault(emptyList())
        _posts.value = userPosts
        _postCount.value = userPosts.size
        _likesCount.value = postRepository.getTotalLikesReceived(userId).getOrDefault(0)
        _commentsCount.value = postRepository.getTotalCommentsReceived(userId).getOrDefault(0)
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch

            val result = postRepository.toggleLike(userId, postId)
            if (result.isFailure) {
                _error.postValue(R.string.error_toggling_like)
            }

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

            _likesCount.value = _posts.value?.sumOf { it.post.likes.size } ?: 0
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
            val userResult = userRepository.getCurrentUser()
            if (userResult.isFailure) {
                _error.postValue(R.string.error_loading_posts)
                return@launch
            }
            val currentUser = userResult.getOrNull() ?: return@launch

            val result = postRepository.addComment(postId, currentUser.id, text)
            if (result.isFailure) {
                _error.postValue(R.string.error_adding_comment)
            } else {
                _posts.value = _posts.value?.map { item ->
                    if (item.post.id == postId) {
                        val newCommentPreview =
                            CommentPreview(currentUser.username, text, System.currentTimeMillis())
                        val updatedPreviews = item.previewComments.toMutableList()
                        updatedPreviews.add(newCommentPreview)
                        item.copy(
                            commentCount = item.commentCount + 1,
                            previewComments = updatedPreviews
                        )
                    } else {
                        item
                    }
                }
            }

            _commentsCount.value = _posts.value?.sumOf { it.commentCount } ?: 0
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userRepository.getCurrentUserId() ?: return@launch
                val postResult = postRepository.getPostById(postId)
                if (postResult.isFailure) {
                    _error.postValue(R.string.error_loading_posts)
                    return@launch
                }
                val post = postResult.getOrNull() ?: return@launch

                val result = postRepository.deletePost(post)
                if (result.isFailure) {
                    _error.postValue(R.string.error_deleting_post)
                } else {
                    _posts.value = _posts.value?.filter { it.post.id != postId }
                    _postCount.value = _posts.value?.size ?: 0
                    _likesCount.value = _posts.value?.sumOf { it.post.likes.size } ?: 0
                    _commentsCount.value = _posts.value?.sumOf { it.commentCount } ?: 0
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            postRepository.resetSyncTimestamp()
            userRepository.logout()
        }
    }
}
