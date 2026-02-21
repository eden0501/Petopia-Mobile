package com.example.petopia.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petopia.data.Post
import com.example.petopia.data.PostType

class HomeViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>(dummyPosts())
    val posts: LiveData<List<Post>> = _posts

    private fun dummyPosts(): List<Post> {
        return listOf(
            Post(
                id = "post1",
                title = "Urgent: Dog found near park",
                description = "Small brown dog found near the central park, seems injured and scared. Looking for volunteers to assist.",
                imageUrl = null,
                authorName = "Danielle the Volunteer",
                authorId = "user1",
                postType = PostType.RESCUE,
                hashtags = listOf("#rescue", "#dog", "#urgent"),
                createdAt = System.currentTimeMillis()
            ),
            Post(
                id = "post2",
                title = "Donating gently used carrier",
                description = "Medium-sized carrier in great condition, used only twice. Free for pickup in city center.",
                imageUrl = null,
                authorName = "Ruth the Dog Owner",
                authorId = "user2",
                postType = PostType.SUPPLIES,
                hashtags = listOf("#donation", "#carrier"),
                createdAt = System.currentTimeMillis()
            )
        )
    }
}

