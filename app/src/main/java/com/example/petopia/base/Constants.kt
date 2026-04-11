package com.example.petopia.base

import com.example.petopia.data.model.Post
import com.example.petopia.data.model.User

typealias EmptyCallback = () -> Unit
typealias PostsCompletion = (List<Post>) -> Unit
typealias PostCompletion = (Post?) -> Unit
typealias UsersCompletion = (List<User>) -> Unit
typealias UserCompletion = (User?) -> Unit
typealias StringCompletion = (String?) -> Unit

object Constants {
    const val POSTS_COLLECTION = "posts"
    const val COMMENTS_COLLECTION = "comments"
    const val USERS_COLLECTION = "users"
    const val POSTS_PER_FACT = 3
    
    object SharedPrefs {
        const val PREFS_NAME = "PETOPIA_PREFS"
        const val LAST_UPDATED_POSTS = "LAST_UPDATED_POSTS"
    }
}
