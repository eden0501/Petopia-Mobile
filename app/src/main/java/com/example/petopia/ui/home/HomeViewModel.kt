package com.example.petopia.ui.home

import androidx.lifecycle.*
import com.example.petopia.R
import com.example.petopia.base.Constants
import com.example.petopia.types.PostDisplayItem
import com.example.petopia.types.HomeItem
import com.example.petopia.data.networking.Networking
import com.example.petopia.types.PostFilter
import com.example.petopia.types.CommentPreview
import com.example.petopia.data.repository.PostRepository
import com.example.petopia.data.repository.UserRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PostRepository,
    private val userRepository: UserRepository,
    private val defaultFact: String
) : ViewModel() {
    private val _posts = MutableLiveData<List<PostDisplayItem>>(emptyList())

    private val _selectedFilter = MutableLiveData(PostFilter.ALL)
    val selectedFilter: LiveData<PostFilter> = _selectedFilter

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

<<<<<<< HEAD
    private val _factUpdatedTrigger = MutableLiveData<Unit>()

    val filteredHomeItems: LiveData<List<HomeItem>> = MediatorLiveData<List<HomeItem>>().apply {
        addSource(_posts) { posts -> value = applyFilterAndInterleave(posts, _selectedFilter.value) }
        addSource(_selectedFilter) { filter -> value = applyFilterAndInterleave(_posts.value ?: emptyList(), filter) }
        addSource(_factUpdatedTrigger) { value = applyFilterAndInterleave(_posts.value ?: emptyList(), _selectedFilter.value) }
    }

    init {
        loadPosts()
    }

    private val loadedFacts = mutableMapOf<String, HomeItem.FactItem>()
    private val fetchingIds = mutableSetOf<String>()

    fun loadPosts() {
        viewModelScope.launch {
            val needsFullSync = !PostRepository.hasCompletedFullSync

            if (needsFullSync) {
                _isLoading.value = true
                repository.refreshAllPosts()
            } else {
                val userId = userRepository.getCurrentUserId()
                val list = repository.getAllPostsWithPreviews(userId)
                _posts.value = list
                repository.refreshPostsIncremental()
            }

            val userId = userRepository.getCurrentUserId()
            val refreshedList = repository.getAllPostsWithPreviews(userId)
            _posts.value = refreshedList
            _isLoading.value = false
        }
    }

    private fun applyFilterAndInterleave(posts: List<PostDisplayItem>, filter: PostFilter?): List<HomeItem> {
        val currentFilter = filter ?: PostFilter.ALL
        
        val filtered = if (currentFilter == PostFilter.ALL) {
            posts
        } else {
            posts.filter { item ->
                when (currentFilter) {
                    PostFilter.RESCUE -> item.post.postType == com.example.petopia.types.PostType.RESCUE
                    PostFilter.CARE_TIPS -> item.post.postType == com.example.petopia.types.PostType.KNOWLEDGE
                    PostFilter.SUPPLIES -> item.post.postType == com.example.petopia.types.PostType.SUPPLIES
                    else -> true
                }
            }
        }

        val result = mutableListOf<HomeItem>()
        filtered.forEachIndexed { index, post ->
            result.add(HomeItem.PostItem(post))
            if ((index + 1) % Constants.POSTS_PER_FACT == 0) {
                val factId = "fact_${index + 1}"
                val fact = loadedFacts[factId] ?: HomeItem.FactItem("Loading fact...", true, isLoading = true, factId)
                result.add(fact)
            }
        }
        return result
    }

    fun loadFact(factId: String) {
        if (loadedFacts.containsKey(factId) || fetchingIds.contains(factId)) return

        fetchingIds.add(factId)
        viewModelScope.launch {
            try {
                val isDog = (0..1).random() == 0
                val factContent = if (isDog) {
                    Networking.dogService.getDogFact().data?.firstOrNull()?.attributes?.body
                } else {
                    Networking.catService.getCatFact().fact
                }
                
                val fact = HomeItem.FactItem(factContent ?: defaultFact, isDog, isLoading = false, factId)
                loadedFacts[factId] = fact
                _factUpdatedTrigger.value = Unit
            } catch (e: Exception) {
                e.printStackTrace()
                val fallbackFact = HomeItem.FactItem(defaultFact, true, isLoading = false, factId)
                loadedFacts[factId] = fallbackFact
                _factUpdatedTrigger.value = Unit
            } finally {
                fetchingIds.remove(factId)
            }
        }
    }

    fun refreshFact() {
        loadedFacts.clear()
        fetchingIds.clear()
        loadPosts()
    }

    fun setFilter(filter: PostFilter) {
        _selectedFilter.value = filter
    }

    fun getCurrentUserId(): String? = userRepository.getCurrentUserId()

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

            repository.addComment(postId, user.id, text)

            _posts.value = _posts.value?.map { item ->
                if (item.post.id == postId) {
                    val newCommentPreview =
                        CommentPreview(user.username, text, System.currentTimeMillis())
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
    }
}

