package com.example.petopia.types


sealed class HomeItem {
    data class PostItem(val displayItem: PostDisplayItem) : HomeItem()
    data class FactItem(val content: String, val isDog: Boolean, val isLoading: Boolean = false, val uniqueId: String = "") : HomeItem()
}
