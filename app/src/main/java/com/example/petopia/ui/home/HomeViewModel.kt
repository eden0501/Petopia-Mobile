package com.example.petopia.ui.home

import androidx.lifecycle.*
import com.example.petopia.data.CommentPreview
import com.example.petopia.data.PostDisplayItem
import com.example.petopia.data.model.Post
import com.example.petopia.data.PostType
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

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            val list = repository.getAllPostsWithPreviews()
            if (list.isEmpty()) {
                // For first run, let's insert some dummy data if DB is empty
                repository.insertPosts(dummyPosts())
                _posts.value = repository.getAllPostsWithPreviews()
            } else {
                _posts.value = list
            }
        }
    }

    fun setFilter(filter: PostFilter) {
        _selectedFilter.value = filter
    }

    private fun dummyPosts(): List<Post> {
        return listOf(
            Post(
                id = "post1",
                title = "URGENT: Injured Dog Found Near Highway",
                content = "Found a limping dog near Route 40. Appears to have injured leg. Need immediate help with transport to vet!",
                imageUrl = "stub",
                authorName = "Danielle_Volunteer",
                authorId = "user1",
                postType = PostType.RESCUE,
                hashtags = listOf("#urgent", "#rescue", "#doginjury"),
                createdAt = System.currentTimeMillis(),
                likeCount = 2
            ),
            Post(
                id = "post2",
                title = "Donating gently used carrier",
                content = "Medium-sized carrier in great condition, used only twice. Free for pickup in city center.",
                imageUrl = "stub",
                authorName = "Ruth_DogMom",
                authorId = "user2",
                postType = PostType.SUPPLIES,
                hashtags = listOf("#donation", "#carrier"),
                createdAt = System.currentTimeMillis(),
                likeCount = 0
            )
        )
    }

    fun toggleLike(postId: String) {
        val currentPosts = _posts.value ?: return
        val updatedPosts = currentPosts.map { item ->
            if (item.post.id == postId) {
                val newIsLiked = !item.isLiked
                val newLikeCount = if (newIsLiked) item.post.likeCount + 1 else item.post.likeCount - 1
                val updatedPost = item.post.copy(likeCount = newLikeCount)
                item.copy(isLiked = newIsLiked, post = updatedPost)
            } else {
                item
            }
        }
        _posts.value = updatedPosts
    }

    fun toggleComments(postId: String) {
        val currentPosts = _posts.value ?: return
        val updatedPosts = currentPosts.map { item ->
            if (item.post.id == postId) {
                item.copy(isCommentsVisible = !item.isCommentsVisible)
            } else {
                item
            }
        }
        _posts.value = updatedPosts
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            if (user != null) {
                repository.addComment(postId, user.id, user.username, text)
                // Reload posts to reflect new comment counts
                _posts.value = repository.getAllPostsWithPreviews()
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
