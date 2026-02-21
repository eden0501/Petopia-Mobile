package com.example.petopia.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petopia.data.CommentPreview
import com.example.petopia.data.Post
import com.example.petopia.data.PostDisplayItem
import com.example.petopia.data.PostType

class HomeViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<PostDisplayItem>>(dummyPostItems())
    val posts: LiveData<List<PostDisplayItem>> = _posts

    private val _selectedFilter = MutableLiveData(PostFilter.ALL)
    val selectedFilter: LiveData<PostFilter> = _selectedFilter

    fun setFilter(filter: PostFilter) {
        _selectedFilter.value = filter
    }

    private fun dummyPostItems(): List<PostDisplayItem> {
        return listOf(
            PostDisplayItem(
                post = Post(
                    id = "post1",
                    title = "URGENT: Injured Dog Found Near Highway",
                    description = "Found a limping dog near Route 40. Appears to have injured leg. Need immediate help with transport to vet! Located near the gas station.",
                    imageUrl = "stub",
                    authorName = "Danielle_Volunteer",
                    authorId = "user1",
                    postType = PostType.RESCUE,
                    hashtags = listOf("#urgent", "#rescue", "#doginjury"),
                    createdAt = System.currentTimeMillis()
                ),
                likeCount = 2,
                commentCount = 2,
                previewComments = listOf(
                    CommentPreview("Ruth_DogMom", "I can help! DMing you now with my contact info.", "about 2 months ago"),
                    CommentPreview("PetRescuer_Mike", "There is an emergency vet clinic nearby. I can meet you there if needed!", "about 2 months ago")
                )
            ),
            PostDisplayItem(
                post = Post(
                    id = "post2",
                    title = "Donating gently used carrier",
                    description = "Medium-sized carrier in great condition, used only twice. Free for pickup in city center.",
                    imageUrl = "stub",
                    authorName = "Ruth_DogMom",
                    authorId = "user2",
                    postType = PostType.SUPPLIES,
                    hashtags = listOf("#donation", "#carrier"),
                    createdAt = System.currentTimeMillis()
                ),
                likeCount = 0,
                commentCount = 0,
                previewComments = emptyList()
            )
        )
    }
}

enum class PostFilter {
    ALL,
    RESCUE,
    CARE_TIPS,
    SUPPLIES
}
