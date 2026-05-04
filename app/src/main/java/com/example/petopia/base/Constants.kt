package com.example.petopia.base

object Constants {
    const val POSTS_PER_FACT = 3
    
    object SharedPrefs {
        const val PREFS_NAME = "PETOPIA_PREFS"
        const val LAST_UPDATED_POSTS = "LAST_UPDATED_POSTS"
        const val HAS_COMPLETED_FULL_SYNC = "HAS_COMPLETED_FULL_SYNC"
    }

    object ResultKeys {
        const val CREATE_POST_RESULT = "create_post_result"
        const val SUCCESS = "success"
    }

    object Collections {
        const val POSTS = "posts"
        const val COMMENTS = "comments"
        const val USERS = "users"
    }
}
